/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         SalatDAO.scala
 * Last modified: 2016-07-10 23:49:08 EDT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package com.novus.salat.dao

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCursorBase
import com.mongodb.casbah.commons.{Logging, MongoDBObject}
import com.mongodb.{DBObject, WriteConcern}
import com.novus.salat._

/**
 * Sample DAO implementation.
 *
 *  @param collection MongoDB collection
 *  @param mot implicit manifest for ObjectType
 *  @param mid implicit manifest for ID
 *  @param ctx implicit [[com.novus.salat.Context]]
 *  @tparam ObjectType class to be persisted
 *  @tparam ID _id type
 */
abstract class SalatDAO[ObjectType <: AnyRef, ID <: Any](val collection: MongoCollection)(implicit
  mot: Manifest[ObjectType],
                                                                                          mid: Manifest[ID], ctx: Context)
    extends com.novus.salat.dao.DAO[ObjectType, ID] with Logging {

  dao =>

  /** Supplies the [[com.novus.salat.Grater]] from the implicit [[com.novus.salat.Context]] and `ObjectType` manifest */
  val _grater = grater[ObjectType](ctx, mot)

  /**
   * Force type hints when objects are persisted.  Used to support a DAO typed to an abstract superclass or trait.
   *  Should be overriden and forced to true when you want to select
   */
  val forceTypeHints = {
    val isProxy = _grater.isInstanceOf[ProxyGrater[_]]
    // safety check - if you never type hint, then deserializing using a proxy grater is impossible
    require(
      !isProxy || ctx.typeHintStrategy.when != TypeHintFrequency.Never,
      "Abstract class hierarchies cannot be deserialized when the context '%s' type hint strategy is NeverTypeHint".format(ctx.name)
    )
    isProxy
  }

  /**
   * If you are mixing and matching abstract and concrete DAOs, turn this on in the concrete DAOs to ensure that querying on a
   *  mixed collection will only yield results in the child collection.
   */
  val appendTypeHintToQueries = false

  /**
   * A central place to modify find, count and update queries before executing them.
   *
   *  @param query query to decorate
   *  @return decorated query for execution
   */
  def decorateQuery(query: DBObject) = {
    if (appendTypeHintToQueries) {
      query(ctx.typeHintStrategy.typeHint) = ctx.typeHintStrategy.encode(_grater.clazz.getName).asInstanceOf[AnyRef]
    }
    query
  }

  /**
   * A central place to modify DBOs before inserting, saving, or updating.
   *
   *  @param toPersist object to be serialized
   *  @return decorated DBO for persisting
   */
  def decorateDBO(toPersist: ObjectType) = {
    val dbo = _grater.asDBObject(toPersist)
    if (forceTypeHints) {
      // take advantage of the mutability of DBObject by cramming in a type hint
      dbo(ctx.typeHintStrategy.typeHint) = ctx.typeHintStrategy.encode(toPersist.getClass.getName).asInstanceOf[AnyRef]
    }
    dbo
  }

  /**
   * Inner abstract class to facilitate working with child collections using a typed parent id -
   *  no cascading support will be offered, but you can override saves and deletes in the parent DAO
   *  to manually cascade children as you like.
   *
   *  Given parent class `Foo` and child class `Bar`:
   *  {{{
   *  case class Foo(_id: ObjectId, //  etc )
   *  case class Bar(_id: ObjectId,
   *                 parentId: ObjectId, // this refers back to a parent in Foo collection
   *                 //  etc )
   *
   *  object FooDAO extends SalatDAO[Foo, ObjectId](collection = MongoConnection()("db")("fooCollection")) {
   *
   *  // and here is a child DAO you can use within FooDAO to work with children of type Bar whose parentId field matches
   *  // the supplied parent id of an instance of Foo
   *   val bar = new ChildCollection[Bar, ObjectId](collection = MongoConnection()("db")("barCollection"),
   *   parentIdField = "parentId") { }
   *
   *  }
   *  }}}
   *
   *  @param collection MongoDB collection
   *  @param parentIdField parent id field key
   *  @param mct implicit manifest for `ChildType`
   *  @param mcid implicit manifest for `ChildID`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam ChildType type of child object
   *  @tparam ChildID type of child _id field
   */
  abstract class ChildCollection[ChildType <: AnyRef, ChildID <: Any](
    override val collection: MongoCollection,
    val parentIdField:       String
  )(implicit
    mct: Manifest[ChildType],
    mcid: Manifest[ChildID], ctx: Context)
      extends SalatDAO[ChildType, ChildID](collection) {

    childDao =>

    override lazy val description = "SalatDAO[%s,%s](%s) -> ChildCollection[%s,%s](%s)".format(
      mot.runtimeClass.getSimpleName, mid.runtimeClass.getSimpleName, dao.collection.name,
      mct.runtimeClass.getSimpleName, mcid.runtimeClass.getSimpleName, childDao.collection.name
    )

    /**
     * @param parentId parent id
     *  @return base query object for a single parent id
     */
    def parentIdQuery(parentId: ID): DBObject = {
      decorateQuery(MongoDBObject(parentIdField -> parentId))
    }

    /**
     * @param parentIds list of parent ids
     *  @return base query object for a list of parent ids
     *  TODO - replace list with traversable
     */
    def parentIdsQuery(parentIds: List[ID]): DBObject = {
      MongoDBObject(parentIdField -> MongoDBObject("$in" -> parentIds))
    }

    /**
     * Count the number of documents matching the parent id.
     *
     *  @param parentId parent id
     *  @param query object for which to search
     *  @param fieldsThatMustExist list of field keys that must exist
     *  @param fieldsThatMustNotExist list of field keys that must not exist
     *  @return count of documents matching the search criteria
     */
    def countByParentId(parentId: ID, query: DBObject = MongoDBObject.empty, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil): Long = {
      childDao.count(parentIdQuery(parentId) ++ query, fieldsThatMustExist, fieldsThatMustNotExist)
    }

    /**
     * @param parentId parent id
     *  @param query object for which to search
     *  @return list of child ids matching parent id and search criteria
     */
    def idsForParentId(parentId: ID, query: DBObject = MongoDBObject.empty): List[ChildID] = {
      childDao.collection.find(parentIdQuery(parentId) ++ query, MongoDBObject("_id" -> 1)).map(_.expand[ChildID]("_id").get).toList
    }

    /**
     * @param parentIds list of parent ids
     *  @param query object for which to search
     *  @return list of child ids matching parent ids and search criteria
     */
    def idsForParentIds(parentIds: List[ID], query: DBObject = MongoDBObject.empty): List[ChildID] = {
      childDao.collection.find(parentIdsQuery(parentIds) ++ query, MongoDBObject("_id" -> 1)).map(_.expand[ChildID]("_id").get).toList
    }

    /**
     * @param parentId parent id
     *  @param query object for which to search
     *  @return list of child objects matching parent id and search criteria
     */
    def findByParentId(parentId: ID, query: DBObject = MongoDBObject.empty): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdQuery(parentId) ++ query)
    }

    /**
     * @param parentIds list of parent ids
     *  @param query object for which to search
     *  @return list of child objects matching parent ids and search criteria
     */
    def findByParentIds(parentIds: List[ID], query: DBObject = MongoDBObject.empty): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdsQuery(parentIds) ++ query)
    }

    /**
     * @param parentId parent id
     *  @param query object for which to search
     *  @return list of child objects matching parent id and search criteria
     */
    def findByParentId(parentId: ID, query: DBObject, keys: DBObject): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdQuery(parentId) ++ query, keys)
    }

    /**
     * @param parentIds parent ids
     *  @param query object for which to search
     *  @return list of child objects matching parent ids and search criteria
     */
    def findByParentIds(parentIds: List[ID], query: DBObject, keys: DBObject): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdsQuery(parentIds) ++ query, keys)
    }

    /**
     * @param parentId parent id
     *  @param o object with which to update the document(s) matching `parentId`
     *  @param upsert if the database should create the element if it does not exist
     *  @param multi if the update should be applied to all objects matching
     *  @param wc write concern
     *  @tparam A type view bound to DBObject
     */
    def updateByParentId[A <% DBObject](parentId: ID, o: A, upsert: Boolean, multi: Boolean, wc: WriteConcern = collection.writeConcern) {
      childDao.update(parentIdQuery(parentId), o, upsert, multi, wc)
    }

    /**
     * @param parentIds parent ids
     *  @param o object with which to update the document(s) matching `parentIds`
     *  @param upsert if the database should create the element if it does not exist
     *  @param multi if the update should be applied to all objects matching
     *  @param wc write concern
     *  @tparam A type view bound to DBObject
     */
    def updateByParentIds[A <% DBObject](parentIds: List[ID], o: A, upsert: Boolean, multi: Boolean, wc: WriteConcern = collection.writeConcern) {
      childDao.update(parentIdsQuery(parentIds), o, upsert, multi, wc)
    }

    /**
     * Remove documents matching parent id
     *  @param parentId parent id
     *  @param wc write concern
     */
    def removeByParentId(parentId: ID, wc: WriteConcern = collection.writeConcern) {
      childDao.remove(parentIdQuery(parentId), wc)
    }

    /**
     * Remove documents matching parent ids
     *  @param parentIds parent ids
     *  @param wc write concern
     */
    def removeByParentIds(parentIds: List[ID], wc: WriteConcern = collection.writeConcern) {
      childDao.remove(parentIdsQuery(parentIds), wc)
    }

    /**
     * Projection typed to a case class, trait or abstract superclass.
     *  @param parentId parent id
     *  @param field field to project on
     *  @param query (optional) object for which to search
     *  @param mr implicit manifest typed to `R`
     *  @param ctx implicit [[com.novus.salat.Context]]
     *  @tparam R type of projected field
     *  @return (List[R]) of the objects found
     */
    def projectionsByParentId[R <: CaseClass](parentId: ID, field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.projections(parentIdQuery(parentId) ++ query, field)(mr, ctx)
    }

    /**
     * Projection typed to a case class, trait or abstract superclass.
     *  @param parentIds parent ids
     *  @param field field to project on
     *  @param query (optional) object for which to search
     *  @param mr implicit manifest typed to `R`
     *  @param ctx implicit [[com.novus.salat.Context]]
     *  @tparam R type of projected field
     *  @return (List[R]) of the objects found
     */
    def projectionsByParentIds[R <: CaseClass](parentIds: List[ID], field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.projections(parentIdsQuery(parentIds) ++ query, field)(mr, ctx)
    }

    /**
     * Projection typed to a type for which Casbah or mongo-java-driver handles conversion
     *  @param parentId parent id
     *  @param field field to project on
     *  @param query (optional) object for which to search
     *  @param mr implicit manifest typed to `R`
     *  @param ctx implicit [[com.novus.salat.Context]]
     *  @tparam R type of projected field
     *  @return (List[R]) of the objects found
     */
    def primitiveProjectionsByParentId[R <: Any](parentId: ID, field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.primitiveProjections(parentIdQuery(parentId) ++ query, field)(mr, ctx)
    }

    /**
     * Projection typed to a type for which Casbah or mongo-java-driver handles conversion
     *  @param parentIds parent ids
     *  @param field field to project on
     *  @param query (optional) object for which to search
     *  @param mr implicit manifest typed to `R`
     *  @param ctx implicit [[com.novus.salat.Context]]
     *  @tparam R type of projected field
     *  @return (List[R]) of the objects found
     */
    def primitiveProjectionsByParentIds[R <: Any](parentIds: List[ID], field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.primitiveProjections(parentIdsQuery(parentIds) ++ query, field)(mr, ctx)
    }
  }

  /**
   * Default description is the case class simple name and the collection.
   */
  override lazy val description = "SalatDAO[%s,%s](%s)".format(mot.runtimeClass.getSimpleName, mid.runtimeClass.getSimpleName, collection.name)

  /**
   * Checks the "err" field in the write result, or the cached last error.
   * "There should be no reason to use getError" - we found one.
   */
  private def defaultWriteResultErrorCheck(wr: WriteResult) = wr.getError != null || {
    val lastError = wr.getCachedLastError
    lastError != null && !lastError.ok()
  }
  /**
   * Safety net for legacy code that is still using MongoConnection instead
   * of MongoClient, and so will not reliably throw a MongoException.
   */
  private def handleLegacyErrors[T](wr: WriteResult, hasError: WriteResult => Boolean = defaultWriteResultErrorCheck _)(failure: => Nothing)(success: => T) = {
    if (hasError(wr)) failure else success
  }

  /**
   * @param t instance of ObjectType
   *  @param wc write concern
   *  @return if insert succeeds, ID of inserted object
   */
  def insert(t: ObjectType, wc: WriteConcern) = {
    val dbo = decorateDBO(t)
    try {
      val wr = collection.insert(dbo, wc)
      handleLegacyErrors(wr) {
        throw SalatInsertError(description, collection, wc, wr, List(dbo))
      } {
        dbo.getAs[ID]("_id")
      }
    }
    catch {
      case mex: MongoException =>
        throw SalatInsertError(description, collection, wc, Right(mex), List(dbo))
    }
  }

  /**
   * @param docs collection of `ObjectType` instances to insert
   *  @param wc write concern
   *  @return list of object ids
   *  TODO: flatten list of IDs - why on earth didn't I do that in the first place?
   */
  def insert(docs: Traversable[ObjectType], wc: WriteConcern = defaultWriteConcern) = if (docs.nonEmpty) {
    val dbos = docs.map(decorateDBO(_)).toList
    try {
      val wr = collection.insert(dbos: _*)
      handleLegacyErrors(wr) {
        throw SalatInsertError(description, collection, wc, wr, dbos)
      } {
        dbos.map {
          dbo =>
            dbo.getAs[ID]("_id") orElse collection.findOne(dbo).flatMap(_.getAs[ID]("_id"))
        }
      }
    }
    catch {
      case mex: MongoException => throw SalatInsertError(description, collection, wc, Right(mex), dbos)
    }
  }
  else Nil

  /**
   * @param query query
   *  @tparam A type view bound to DBObject
   *  @return list of IDs
   */
  def ids[A <% DBObject](query: A): List[ID] = {
    collection.find(decorateQuery(query), MongoDBObject("_id" -> 1)).map(_.expand[ID]("_id").get).toList
  }

  /**
   * @param t object for which to search
   *  @param rp the ReadPreference used for this find
   *  @tparam A type view bound to DBObject
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOne[A <% DBObject](t: A, rp: ReadPreference) = collection.findOne(o = decorateQuery(t), fields = null, readPrefs = rp).map(_grater.asObject(_))

  /**
   * @param id identifier
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOneById(id: ID) = collection.findOneByID(id.asInstanceOf[AnyRef]).map(_grater.asObject(_))

  /**
   * @param t object to remove from the collection
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  def remove(t: ObjectType, wc: WriteConcern) = {
    val dbo = decorateDBO(t)
    try {
      val wr = collection.remove(dbo, wc)

      handleLegacyErrors(wr) {
        throw SalatRemoveError(description, collection, wc, wr, List(dbo))
      } {
        wr
      }
    }
    catch {
      case mex: MongoException =>
        throw SalatRemoveError(description, collection, wc, Right(mex), List(dbo))
    }
  }

  /**
   * @param q the object that documents to be removed must match
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  def remove[A <% DBObject](q: A, wc: WriteConcern) = {
    try {
      val wr = collection.remove(q, wc)

      handleLegacyErrors(wr) {
        throw SalatRemoveQueryError(description, collection, q, wc, wr)
      } {
        wr
      }
    }
    catch {
      case mex: MongoException =>
        throw SalatRemoveQueryError(description, collection, q, wc, Right(mex))
    }
  }

  /**
   * @param id the ID of the document to be removed
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  def removeById(id: ID, wc: WriteConcern = defaultWriteConcern) = {
    remove(MongoDBObject("_id" -> id), wc)
  }

  /**
   * @param ids the list of IDs identifying the list of documents to be removed
   *  @param wc wrote concern
   *  @return (WriteResult) result of write operation
   */
  def removeByIds(ids: List[ID], wc: WriteConcern) = {
    remove(MongoDBObject("_id" -> MongoDBObject("$in" -> MongoDBList(ids: _*))), wc)
  }

  /**
   * @param t object to save
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  def save(t: ObjectType, wc: WriteConcern) = {
    val dbo = decorateDBO(t)

    try {
      val wr = collection.save(dbo, wc)

      handleLegacyErrors(wr) {
        throw SalatSaveError(description, collection, wc, wr, List(dbo))
      } {
        wr
      }
    }
    catch {
      case mex: MongoException => throw SalatSaveError(description, collection, wc, Right(mex), List(dbo))
    }
  }

  /**
   * @param q search query for old object to update
   *  @param o object with which to update <tt>q</tt>
   *  @param upsert if the database should create the element if it does not exist
   *  @param multi if the update should be applied to all objects matching
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  def update(q: DBObject, o: DBObject, upsert: Boolean = false, multi: Boolean = false, wc: WriteConcern = defaultWriteConcern): WriteResult = {
    try {
      val wr = collection.update(decorateQuery(q), o, upsert, multi, wc)

      handleLegacyErrors(wr) {
        throw SalatDAOUpdateError(description, collection, q, o, wc, wr, upsert, multi)
      } {
        wr
      }
    }
    catch {
      case mex: MongoException =>
        throw SalatDAOUpdateError(description, collection, q, o, wc, Right(mex), upsert, multi)
    }
  }

  /**
   * @param ref object for which to search
   *  @param keys fields to return
   *  @param rp sets the desired ReadPreference on the cursor
   *  @tparam A type view bound to DBObject
   *  @tparam B type view bound to DBObject
   *  @return a typed cursor to iterate over results
   */
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B, rp: ReadPreference) = SalatMongoCursor[ObjectType](
    _grater,
    collection.find(decorateQuery(ref), keys).asInstanceOf[MongoCursorBase].underlying.setReadPreference(rp)
  )

  /**
   * @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (Option[P]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P] = {
    collection.findOne(decorateQuery(query), MongoDBObject(field -> 1)).flatMap {
      dbo =>
        dbo.expand[DBObject](field).map(grater[P].asObject(_))
    }
  }

  /**
   * @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (Option[P]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P] = {
    collection.findOne(decorateQuery(query), MongoDBObject(field -> 1)).flatMap {
      dbo =>
        dbo.expand[P](field)
    }
  }

  /**
   * @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (List[P]) of the objects found
   */
  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P] =
    collection.find(decorateQuery(query), MongoDBObject(field -> 1)).toList.flatMap {
      r =>
        r.expand[DBObject](field).map(grater[P].asObject(_))
    }

  /**
   * @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (List[P]) of the objects found
   */
  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P] = {
    collection.find(query, MongoDBObject(field -> 1)).toList.flatMap(_.expand[P](field))
  }

  /**
   * @param q object for which to search
   *  @param fieldsThatMustExist list of field keys that must exist
   *  @param fieldsThatMustNotExist list of field keys that must not exist
   *  @return count of documents matching the search criteria
   */
  def count(q: DBObject = MongoDBObject.empty, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil, rp: ReadPreference = defaultReadPreference): Long = {
    val query = {
      val builder = MongoDBObject.newBuilder
      builder ++= q
      for (field <- fieldsThatMustExist) {
        builder += field -> MongoDBObject("$exists" -> true)
      }
      for (field <- fieldsThatMustNotExist) {
        builder += field -> MongoDBObject("$exists" -> false)
      }
      builder.result()
    }
    collection.count(decorateQuery(query), readPrefs = rp)
  }
}

/**
 * When you use a single collection to contain an entire type hierarchy, then use this trait to make sure that type hints
 *  are appended to find, count and update queries.  (Please note you need to make sure your indexes on this shared collection
 *  take your type hint fields into account!)
 *
 *  In addition, when you use the concrete subclass DAO to insert, update and save objects, a type hint will be appended to
 *  the serialized object.
 *
 */
trait ConcreteSubclassDAO {
  self: SalatDAO[_, _] =>
  override val forceTypeHints = true
  override val appendTypeHintToQueries = true
  require(_grater.ctx.typeHintStrategy.when != TypeHintFrequency.Never, "Concrete subclass DAO must support type hinting!")
}
