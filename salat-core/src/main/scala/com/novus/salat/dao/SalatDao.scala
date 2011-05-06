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
 * @type T case class type
 * @type S _id type
 */
trait DAO[T <: CaseClass, S <: Any] {

  val collection: MongoCollection

  val _grater: Grater[T]

  lazy val description: String = "DAO"

  def insert(t: T): Option[S]
  def insert(docs: T*): List[Option[S]]

  def ids[A <% DBObject](query: A): List[S]

  def find[A <% DBObject](ref: A): SalatMongoCursor[T]
  def find[A <% DBObject](ref: A, keys: A): SalatMongoCursor[T]

  def findOne[A <% DBObject](t: A): Option[T]
  def findOneByID(id: S): Option[T]

  def save(t: T): CommandResult

  def update[A <% DBObject](q: A, o: A): CommandResult
  def update[A <% DBObject](q: A, o: T): CommandResult

  def remove(t: T): CommandResult

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]

  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]
}


abstract class SalatDAO[T <: CaseClass : Manifest, S <: Any : Manifest] extends com.novus.salat.dao.DAO[T, S] with Logging {

  private lazy val _idMs = manifest[S]

  /**
   * Default description is the case class simple name and the collection.
   */
  override lazy val description = "SalatDAO[%s,%s](%s)".format(manifest[T].erasure.getSimpleName, _idMs.erasure.getSimpleName, collection.name)

  def insert(t: T) = {
    val _id = try {
      val dbo = _grater.asDBObject(t)
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.insert(dbo, wc)
      if (wr.getLastError(wc).ok()) {
        val _id = collection.findOne(dbo) match {
          case Some(dbo: DBObject) => dbo.getAs[S]("_id")
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

        """.format(manifest[T].getClass.getName, collection.getName(), wc, wr, dbo))
      }
    }
    finally {
      collection.db.requestDone()
    }

    _id
  }

  def insert(docs: T*) = {
    val _ids = try {
      val dbos = docs.map(t => _grater.asDBObject(t))
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.insert(dbos, wc)
      if (wr.getLastError(wc).ok()) {
        val builder = List.newBuilder[Option[S]]
        for (dbo <- dbos) {
          builder += {
            collection.findOne(dbo) match {
              case Some(dbo: DBObject) => dbo.getAs[S]("_id")
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

        """.format(manifest[T].getClass.getName, collection.getName(), wc, wr, dbos.mkString("\n")))
      }
    }
    finally {
//      log.trace("insert: collection=%s request done", collection.getName())
      collection.db.requestDone()
    }

    _ids
  }

  def ids[A <% DBObject](query: A): List[S] = {
    collection.find(query, MongoDBObject("_id" -> 1)).map(_.expand[S]("_id")(_idMs).get).toList
  }

  def findOne[A <% DBObject](t: A) = collection.findOne(t).map(_grater.asObject(_))

  def findOneByID(id: S) = collection.findOneByID(id.asInstanceOf[AnyRef]).map(_grater.asObject(_))

  def remove(t: T) = {
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

  def save(t: T) = {
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

  def update[A <% DBObject](q: A, t: T) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.update(q, _grater.asDBObject(t))
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }

  def update[A <% DBObject](q: A, o: A) = {
    val cr = try {
      collection.db.requestStart()
      val wc = new WriteConcern()
      val wr = collection.update(q, o)
      wr.getLastError(wc)
    }
    finally {
      collection.db.requestDone()
    }
    cr
  }

  def find[A <% DBObject](ref: A, keys: A) = SalatMongoCursor[T](_grater,
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
