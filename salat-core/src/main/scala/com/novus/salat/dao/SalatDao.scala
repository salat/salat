/**
* Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
* For questions and comments about this product, please see the project page at:
*
* http://github.com/novus/salat
*
*/
package com.novus.salat.dao

import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.MongoCursorBase
import com.mongodb.{DBObject, CommandResult}

/**
 * Base DAO class.
 * @type ObjectType case class type
 * @type ID _id type
 */
trait DAO[ObjectType <: CaseClass, ID <: Any] {

  val collection: MongoCollection

  val _grater: Grater[ObjectType]

  lazy val description: String = "DAO"

  def insert(t: ObjectType): Option[ID]
  def insert(docs: ObjectType*): List[Option[ID]]

  def ids[A <% DBObject](query: A): List[ID]

  def find[A <% DBObject](ref: A): SalatMongoCursor[ObjectType]
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B): SalatMongoCursor[ObjectType]

  def findOne[A <% DBObject](t: A): Option[ObjectType]
  def findOneByID(id: ID): Option[ObjectType]

  def save(t: ObjectType): CommandResult

  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean, multi: Boolean): CommandResult

  // type erasure sucks.  why doesn't anyone else believe priority one is to go back and eradicate type erasure in the JVM? (closures, forsooth!)
  def update[A <% DBObject](q: A, o: ObjectType, upsert: Boolean, multi: Boolean): CommandResult

  def remove(t: ObjectType): CommandResult

  def remove[A <% DBObject](q: A): CommandResult

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]

  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]
}


abstract class SalatDAO[ObjectType <: CaseClass : Manifest, ID <: Any : Manifest] extends com.novus.salat.dao.DAO[ObjectType, ID] with Logging {

  dao =>

  private lazy val _idMs = manifest[ID]

  /**
    *  Inner trait to facilitate working with child collections - no cascading support will be offered, but you
    *  can override saves and deletes to manually cascade children from parents as you like.
    *
    *  Given parent class Foo and child class Bar:
    *  case class Foo(_id: ObjectId, //  etc )
    *  case class Bar(_id: ObjectId,
    *                 parentId: ObjectId, // this refers back to a parent in Foo collection
    *                 //  etc )
    *
    *
    *  object FooDAO extends SalatDAO[Foo, ObjectId] {
    *
    *   val _grater = grater[Foo]
    *   val collection = MongoConnection()("db")("fooCollection")
    *
    *  // and here is a child DAO you can use within FooDAO to work with children of type Bar whose parentId field matches
    *  // the supplied parent id of an instance of Foo
    *   val bar = new ChildCollection[Bar, ObjectId]("parentId") {
    *     val _grater = grater[Bar]
    *     val collection = MongoConnection()("db")("barCollection")
    *   }
    *
    * }
   */
  abstract class ChildCollection[ChildType <: CaseClass : Manifest, ChildId <: Any : Manifest](val parentIdField: String) extends SalatDAO[ChildType, ChildId] {

    childDao =>

    override lazy val description = "SalatDAO[%s,%s](%s) -> ChildCollection[%s,%s](%s)".
      format(manifest[ObjectType].erasure.getSimpleName, dao._idMs.erasure.getSimpleName, dao.collection.name,
      manifest[ChildType].erasure.getSimpleName, childDao._idMs.erasure.getSimpleName, childDao.collection.name)

    def idsForParentId[A <% DBObject](parentId: ID): List[ChildId] = {
      collection.find(MongoDBObject(parentIdField -> parentId), MongoDBObject("_id" -> 1)).map(_.expand[ChildId]("_id")(_idMs).get).toList
    }

    def findByParentId(parentId: ID): SalatMongoCursor[ChildType] = {
      find(MongoDBObject(parentIdField -> parentId))
    }

    def findByParentId[A <% DBObject](parentId: ID, keys: A): SalatMongoCursor[ChildType] = {
      find(MongoDBObject(parentIdField -> parentId), keys)
    }

    def updateByParentId[A <% DBObject](parentId: ID, o: A, upsert: Boolean, multi: Boolean): CommandResult = {
      val cr = try {
        collection.db.requestStart()
        val wc = new WriteConcern()
        val wr = collection.update(MongoDBObject(parentIdField -> parentId), o, upsert, multi, wc)
        wr.getLastError(wc)
      }
      finally {
        collection.db.requestDone()
      }
      cr
    }

    def removeByParentId(parentId: ID): CommandResult = {
      remove(MongoDBObject(parentIdField -> parentId))
    }
  }

  /**
   * Default description is the case class simple name and the collection.
   */
  override lazy val description = "SalatDAO[%s,%s](%s)".format(manifest[ObjectType].erasure.getSimpleName, _idMs.erasure.getSimpleName, collection.name)

  def insert(t: ObjectType) = {
    val _id = try {
      val dbo = _grater.asDBObject(t)
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.insert(dbo, wc)
      if (wr.getLastError(wc).ok()) {
        val _id = collection.findOne(dbo) match {
          case Some(dbo: DBObject) => dbo.getAs[ID]("_id")
          case _ => None
        }
        _id
      }
      else {
        throw new Error("""

        SalatDAO: insert failed!

        Class: %s
        Collection: %s
        WriteConcern: %s
        WriteResult: %s

        FAILED TO INSERT DBO
        %s

        """.format(manifest[ObjectType].getClass.getName, collection.getName(), wc, wr, dbo))
      }
    }
    finally {
      collection.db.requestDone()
    }

    _id
  }

  def insert(docs: ObjectType*) = {
    val _ids = try {
      val dbos = docs.map(t => _grater.asDBObject(t))
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.insert(dbos, wc)
      if (wr.getLastError(wc).ok()) {
        val builder = List.newBuilder[Option[ID]]
        for (dbo <- dbos) {
          builder += {
            collection.findOne(dbo) match {
              case Some(dbo: DBObject) => dbo.getAs[ID]("_id")
              case _ => None
            }
          }
        }
        builder.result()
      }
      else {
        throw new Error("""

        SalatDAO: insert failed on a collection of docs!

        Class: %s
        Collection: %s
        WriteConcern: %s
        WriteResult: %s

        FAILED TO INSERT DBOS
        %s

        """.format(manifest[ObjectType].getClass.getName, collection.getName(), wc, wr, dbos.mkString("\n")))
      }
    }
    finally {
//      log.trace("insert: collection=%s request done", collection.getName())
      collection.db.requestDone()
    }

    _ids
  }

  def ids[A <% DBObject](query: A): List[ID] = {
    collection.find(query, MongoDBObject("_id" -> 1)).map(_.expand[ID]("_id")(_idMs).get).toList
  }

  def findOne[A <% DBObject](t: A) = collection.findOne(t).map(_grater.asObject(_))

  def findOneByID(id: ID) = collection.findOneByID(id.asInstanceOf[AnyRef]).map(_grater.asObject(_))

  def remove(t: ObjectType) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.remove(_grater.asDBObject(t), wc)
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }


  def remove[A <% DBObject](q: A) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.remove(q, wc)
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }

  def save(t: ObjectType) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.save(_grater.asDBObject(t), wc)
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }

  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean = false, multi: Boolean = false) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.update(q, o, upsert, multi, wc)
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }

  def update[A <% DBObject](q: A, t: ObjectType, upsert: Boolean, multi: Boolean) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.update(q, _grater.asDBObject(t), upsert, multi, wc)
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = SalatMongoCursor[ObjectType](_grater,
    collection.find(ref, keys).asInstanceOf[MongoCursorBase].underlying)

  def find[A <% DBObject](ref: A) = find(ref.asInstanceOf[DBObject], MongoDBObject())

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) = {
    collection.findOne(query, MongoDBObject(field -> 1)).map {
      dbo =>
        dbo.expand[DBObject](field).map(grater[P].asObject(_))
    }.getOrElse(None)
  }

  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) = {
    collection.findOne(query, MongoDBObject(field -> 1)).map {
      dbo =>
        dbo.expand[P](field)
    }.getOrElse(None)
  }

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context)  = {

    // Casbah hiccup - needs to be cast to MongoCursor
    val results = collection.find(query, MongoDBObject(field -> 1)).asInstanceOf[MongoCursor].toList

    val builder = List.newBuilder[P]
    results.foreach {
      r =>
        r.expand[DBObject](field).map(grater[P].asObject(_)).foreach(builder += _)
    }

    builder.result()
  }

  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context)  = {

    // Casbah hiccup - needs to be cast to MongoCursor
    val results = collection.find(query, MongoDBObject(field -> 1)).asInstanceOf[MongoCursor].toList

    val builder = List.newBuilder[P]
    results.foreach {
      r =>
        r.expand[P](field).foreach(builder += _)
    }

    builder.result()
  }
}
