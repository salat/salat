/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.dao

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCursorBase
import com.novus.salat._
import com.mongodb.casbah.commons.{ MongoDBObject, Logging }
import com.mongodb.{ WriteConcern, DBObject, CommandResult }

/** Base DAO class.
 *  @type ObjectType case class type
 *  @type ID _id type
 */
trait DAO[ObjectType <: CaseClass, ID <: Any] {

  val collection: MongoCollection

  val _grater: Grater[ObjectType]

  lazy val description: String = "DAO"

  def insert(t: ObjectType): Option[ID]
  def insert(t: ObjectType, wc: WriteConcern): Option[ID]

  def insert(docs: ObjectType*)(implicit wc: WriteConcern): List[Option[ID]]

  def ids[A <% DBObject](query: A): List[ID]

  def find[A <% DBObject](ref: A): SalatMongoCursor[ObjectType]
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B): SalatMongoCursor[ObjectType]

  def findOne[A <% DBObject](t: A): Option[ObjectType]

  def findOneByID(id: ID): Option[ObjectType]

  def save(t: ObjectType)
  def save(t: ObjectType, wc: WriteConcern)

  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean, multi: Boolean, wc: WriteConcern)

  // type erasure sucks.  why doesn't anyone else believe priority one is to go back and eradicate type erasure in the JVM? (closures, forsooth!)
  // also, not accepting default args on overloaded methods with different param types is a cop-out
  def update[A <% DBObject](q: A, o: ObjectType, upsert: Boolean, multi: Boolean, wc: WriteConcern)

  def remove(t: ObjectType)
  def remove(t: ObjectType, wc: WriteConcern)

  def remove[A <% DBObject](q: A)
  def remove[A <% DBObject](q: A, wc: WriteConcern)

  def removeById(id: ID, wc: WriteConcern = collection.writeConcern)
  def removeByIds(ids: List[ID], wc: WriteConcern = collection.writeConcern)

  def count(q: DBObject = MongoDBObject.empty, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil): Long

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]

  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]
}

abstract class SalatDAO[ObjectType <: CaseClass, ID <: Any](val collection: MongoCollection)(implicit mot: Manifest[ObjectType],
                                                                                             mid: Manifest[ID], ctx: Context)
    extends com.novus.salat.dao.DAO[ObjectType, ID] with Logging {

  dao =>

  // get the grater from the implicit context and object type erasure
  val _grater = grater[ObjectType](ctx, mot)

  /** Inner abstract class to facilitate working with child collections using a typed parent id -
   *  no cascading support will be offered, but you can override saves and deletes in the parent DAO
   *  to manually cascade children as you like.
   *
   *  Given parent class Foo and child class Bar:
   *  case class Foo(_id: ObjectId, //  etc )
   *  case class Bar(_id: ObjectId,
   *                 parentId: ObjectId, // this refers back to a parent in Foo collection
   *                 //  etc )
   *
   *
   *  object FooDAO extends SalatDAO[Foo, ObjectId](collection = MongoConnection()("db")("fooCollection")) {
   *
   *  // and here is a child DAO you can use within FooDAO to work with children of type Bar whose parentId field matches
   *  // the supplied parent id of an instance of Foo
   *   val bar = new ChildCollection[Bar, ObjectId](collection = MongoConnection()("db")("barCollection"),
   *   parentIdField = "parentId") { }
   *
   *  }
   */
  abstract class ChildCollection[ChildType <: CaseClass, ChildId <: Any](override val collection: MongoCollection,
                                                                         val parentIdField: String)(implicit mct: Manifest[ChildType],
                                                                                                    mcid: Manifest[ChildId], ctx: Context)
      extends SalatDAO[ChildType, ChildId](collection) {

    childDao =>

    override lazy val description = "SalatDAO[%s,%s](%s) -> ChildCollection[%s,%s](%s)".format(
      mot.erasure.getSimpleName, mid.erasure.getSimpleName, dao.collection.name,
      mct.erasure.getSimpleName, mcid.erasure.getSimpleName, childDao.collection.name)

    def parentIdQuery(parentId: ID): DBObject = {
      MongoDBObject(parentIdField -> parentId)
    }

    def parentIdsQuery(parentIds: List[ID]): DBObject = {
      MongoDBObject(parentIdField -> MongoDBObject("$in" -> parentIds))
    }

    def countByParentId(parentId: ID, query: DBObject = MongoDBObject.empty, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil): Long = {
      childDao.count(parentIdQuery(parentId) ++ query, fieldsThatMustExist, fieldsThatMustNotExist)
    }

    def idsForParentId(parentId: ID, query: DBObject = MongoDBObject.empty): List[ChildId] = {
      childDao.collection.find(parentIdQuery(parentId) ++ query, MongoDBObject("_id" -> 1)).map(_.expand[ChildId]("_id")(mcid).get).toList
    }

    def idsForParentIds(parentIds: List[ID], query: DBObject = MongoDBObject.empty): List[ChildId] = {
      childDao.collection.find(parentIdsQuery(parentIds) ++ query, MongoDBObject("_id" -> 1)).map(_.expand[ChildId]("_id")(mcid).get).toList
    }

    def findByParentId(parentId: ID, query: DBObject = MongoDBObject.empty): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdQuery(parentId) ++ query)
    }

    def findByParentIds(parentIds: List[ID], query: DBObject = MongoDBObject.empty): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdsQuery(parentIds) ++ query)
    }

    def findByParentId(parentId: ID, query: DBObject, keys: DBObject): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdQuery(parentId) ++ query, keys)
    }

    def findByParentIds(parentIds: List[ID], query: DBObject, keys: DBObject): SalatMongoCursor[ChildType] = {
      childDao.find(parentIdsQuery(parentIds) ++ query, keys)
    }

    def updateByParentId[A <% DBObject](parentId: ID, o: A, upsert: Boolean, multi: Boolean, wc: WriteConcern = collection.writeConcern) {
      childDao.update(parentIdQuery(parentId), o, upsert, multi, wc)
    }

    def updateByParentIds[A <% DBObject](parentIds: List[ID], o: A, upsert: Boolean, multi: Boolean, wc: WriteConcern = collection.writeConcern) {
      childDao.update(parentIdsQuery(parentIds), o, upsert, multi, wc)
    }

    def removeByParentId(parentId: ID, wc: WriteConcern = collection.writeConcern) {
      childDao.remove(parentIdQuery(parentId), wc)
    }

    def removeByParentIds(parentIds: List[ID], wc: WriteConcern = collection.writeConcern) {
      childDao.remove(parentIdsQuery(parentIds), wc)
    }

    def projectionsByParentId[R <: CaseClass](parentId: ID, field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.projections(parentIdQuery(parentId) ++ query, field)(mr, ctx)
    }

    def projectionsByParentIds[R <: CaseClass](parentIds: List[ID], field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.projections(parentIdsQuery(parentIds) ++ query, field)(mr, ctx)
    }

    def primitiveProjectionsByParentId[R <: Any](parentId: ID, field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.primitiveProjections(parentIdQuery(parentId) ++ query, field)(mr, ctx)
    }

    def primitiveProjectionsByParentIds[R <: Any](parentIds: List[ID], field: String, query: DBObject = MongoDBObject.empty)(implicit mr: Manifest[R], ctx: Context): List[R] = {
      childDao.primitiveProjections(parentIdsQuery(parentIds) ++ query, field)(mr, ctx)
    }
  }

  /** Default description is the case class simple name and the collection.
   */
  override lazy val description = "SalatDAO[%s,%s](%s)".format(mot.erasure.getSimpleName, mid.erasure.getSimpleName, collection.name)

  def insert(t: ObjectType) = insert(t, collection.writeConcern)

  def insert(t: ObjectType, wc: WriteConcern) = {
    val _id = try {
      val dbo = _grater.asDBObject(t)
      val wr = collection.insert(dbo, wc)
      val error = wr.getCachedLastError
      if (error == null || (error != null && error.ok())) {
        dbo.getAs[ID]("_id")
      }
      else {
        throw SalatInsertError(description, collection, wc, wr, List(dbo))
      }
    }

    _id
  }

  def insert(docs: ObjectType*)(implicit wc: WriteConcern = collection.writeConcern) = if (docs.nonEmpty) {
    val _ids = try {
      val dbos = docs.map(t => _grater.asDBObject(t))
      val wr = collection.insert(dbos, wc)
      val lastError = wr.getCachedLastError
      if (lastError == null || (lastError != null && lastError.ok())) {
        val builder = List.newBuilder[Option[ID]]
        for (dbo <- dbos) {
          builder += dbo.getAs[ID]("_id") orElse {
            collection.findOne(dbo) match {
              case Some(dbo: DBObject) => dbo.getAs[ID]("_id")
              case _                   => None
            }
          }
        }
        builder.result()
      }
      else {
        throw SalatInsertError(description, collection, wc, wr, dbos.toList)
      }
    }

    _ids
  }
  else Nil

  def ids[A <% DBObject](query: A): List[ID] = {
    collection.find(query, MongoDBObject("_id" -> 1)).map(_.expand[ID]("_id")(mid).get).toList
  }

  def findOne[A <% DBObject](t: A) = collection.findOne(t).map(_grater.asObject(_))

  def findOneByID(id: ID) = collection.findOneByID(id.asInstanceOf[AnyRef]).map(_grater.asObject(_))

  def remove(t: ObjectType) {
    remove(t, collection.writeConcern)
  }

  def remove(t: ObjectType, wc: WriteConcern) {
    try {
      val dbo = _grater.asDBObject(t)
      val wr = collection.remove(dbo, wc)
      val lastError = wr.getCachedLastError
      if (lastError != null && !lastError.ok()) {
        throw SalatRemoveError(description, collection, wc, wr, List(dbo))
      }
    }
  }

  def remove[A <% DBObject](q: A) {
    remove(q, collection.writeConcern)
  }

  def remove[A <% DBObject](q: A, wc: WriteConcern) {
    try {
      val wr = collection.remove(q, wc)
      val lastError = wr.getCachedLastError
      if (lastError != null && !lastError.ok()) {
        throw SalatRemoveQueryError(description, collection, q, wc, wr)
      }
    }
  }

  def removeById(id: ID, wc: WriteConcern = collection.writeConcern) {
    remove(MongoDBObject("_id" -> id), wc)
  }

  def removeByIds(ids: List[ID], wc: WriteConcern) {
    remove(MongoDBObject("_id" -> MongoDBObject("$in" -> MongoDBList(ids: _*))), wc)
  }

  def save(t: ObjectType) {
    save(t, collection.writeConcern)
  }

  def save(t: ObjectType, wc: WriteConcern) {
    try {
      val dbo = _grater.asDBObject(t)
      val wr = collection.save(dbo, wc)
      val lastError = wr.getCachedLastError
      if (lastError != null && !lastError.ok()) {
        throw SalatSaveError(description, collection, wc, wr, List(dbo))
      }
    }
  }

  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean = false, multi: Boolean = false, wc: WriteConcern = collection.writeConcern) {
    try {
      val wr = collection.update(q, o, upsert, multi, wc)
      val lastError = wr.getCachedLastError
      if (lastError != null && !lastError.ok()) {
        throw SalatDAOUpdateError(description, collection, q, o, wc, wr, upsert, multi)
      }
    }
  }

  def update[A <% DBObject](q: A, t: ObjectType, upsert: Boolean, multi: Boolean, wc: WriteConcern) {
    update(q, _grater.asDBObject(t), upsert, multi, wc)
  }

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = SalatMongoCursor[ObjectType](_grater,
    collection.find(ref, keys).asInstanceOf[MongoCursorBase].underlying)

  def find[A <% DBObject](ref: A) = find(ref.asInstanceOf[DBObject], MongoDBObject.empty)

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P] = {
    collection.findOne(query, MongoDBObject(field -> 1)).map {
      dbo =>
        dbo.expand[DBObject](field).map(grater[P].asObject(_))
    }.getOrElse(None)
  }

  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P] = {
    collection.findOne(query, MongoDBObject(field -> 1)).map {
      dbo =>
        dbo.expand[P](field)
    }.getOrElse(None)
  }

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P] = {

    // Casbah hiccup - needs to be cast to MongoCursor
    val results = collection.find(query, MongoDBObject(field -> 1)).asInstanceOf[MongoCursor].toList

    val builder = List.newBuilder[P]
    results.foreach {
      r =>
        r.expand[DBObject](field).map(grater[P].asObject(_)).foreach(builder += _)
    }

    builder.result()
  }

  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P] = {

    // Casbah hiccup - needs to be cast to MongoCursor
    val results = collection.find(query, MongoDBObject(field -> 1)).asInstanceOf[MongoCursor].toList

    val builder = List.newBuilder[P]
    results.foreach {
      r =>
        r.expand[P](field)(m).foreach(builder += _)
    }

    builder.result()
  }

  /** Convenience method on collection.count
   *  @q optional query with default argument of empty query
   *  @fieldsThatMustExist list of field names to append to the query with "fieldName" -> { "$exists" -> true }
   *  @fieldsThatMustNotExist list of field names to append to the query with "fieldName" -> { "$exists" -> false }
   */
  def count(q: DBObject = MongoDBObject.empty, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil): Long = {
    // convenience method - don't personally enjoy writing these queries
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
    collection.count(query)
  }
}