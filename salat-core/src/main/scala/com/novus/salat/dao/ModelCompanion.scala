/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         ModelCompanion.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */

package com.novus.salat.dao

import com.mongodb.casbah.Imports._
import com.novus.salat._
import net.liftweb.json.JsonAST.JObject
import java.lang.reflect.{ Type, ParameterizedType }
import com.novus.salat.util.Logging

/** Play framework style model companion
 *  @tparam ObjectType type of object to be serialized
 *  @tparam ID type of object id to be serialized
 */
trait ModelCompanion[ObjectType <: AnyRef, ID <: Any] extends Logging {

  def dao: DAO[ObjectType, ID]

  //
  // convenient access to methods on Grater
  //

  def toDBObject(t: ObjectType): DBObject = dao._grater.asDBObject(t)

  // TODO: bring back view bound...  assuming it could possibly be worth the bother.
  def toObject(dbo: DBObject): ObjectType = dao._grater.asObject(dbo)

  def toJson(t: ObjectType): JObject = dao._grater.toJSON(t)

  def toPrettyJson(t: ObjectType): String = dao._grater.toPrettyJSON(t)

  def toCompactJson(t: ObjectType): String = dao._grater.toCompactJSON(t)

  //
  // convenient access to methods on SalatDAO
  //

  def count(q: DBObject, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil) = dao.count(q, fieldsThatMustExist, fieldsThatMustExist)

  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = dao.find(ref, keys)

  def find[A <% DBObject](ref: A) = dao.find(ref)

  def findOne[A <% DBObject](t: A) = dao.findOne(t)

  def findOneByID(id: ID) = dao.findOneByID(id)

  def ids[A <% DBObject](query: A) = dao.ids(query)

  def insert(docs: ObjectType*)(implicit wc: WriteConcern) = dao.insert(docs: _*)(wc)

  def insert(t: ObjectType) = dao.insert(t)

  def insert(t: ObjectType, wc: WriteConcern = dao.collection.writeConcern) = dao.insert(t, wc)

  def primitiveProjection[P](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.primitiveProjection[P](query, field)

  def primitiveProjections[P](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.primitiveProjections[P](query, field)

  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.projection[P](query, field)

  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.projections[P](query, field)

  def remove(t: ObjectType) {
    dao.remove(t)
  }

  def remove(t: ObjectType, wc: WriteConcern = dao.collection.writeConcern) {
    dao.remove(t, wc)
  }

  def remove[A <% DBObject](q: A) {
    dao.remove(q)
  }

  def remove[A <% DBObject](q: A, wc: WriteConcern) {
    dao.remove(q, wc)
  }

  def removeById(id: ID, wc: WriteConcern = dao.collection.writeConcern) {
    dao.removeById(id, wc)
  }

  def removeByIds(ids: List[ID], wc: WriteConcern = dao.collection.writeConcern) {
    dao.removeByIds(ids, wc)
  }

  def save(t: ObjectType) {
    dao.save(t)
  }

  def save(t: ObjectType, wc: WriteConcern = dao.collection.writeConcern) {
    dao.save(t, wc)
  }

  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean, multi: Boolean, wc: WriteConcern) {
    dao.update(q, o, upsert, multi, wc)
  }

  def update[A <% DBObject](q: A, o: ObjectType, upsert: Boolean = false, multi: Boolean = false, wc: WriteConcern = dao.collection.writeConcern) {
    dao.update(q, o, upsert, multi, wc)
  }

  //
  // methods I can't see the point of personally, but which pay some distant obeisance to the Platonic DAO carried
  // forward like pyramid blocks by my predecessors
  //

  def findAll(): Iterator[ObjectType] = dao.find(MongoDBObject.empty).toIterator
}
