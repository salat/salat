/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ModelCompanion.scala
 * Last modified: 2012-06-28 15:37:34 EDT
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
import com.novus.salat.util.Logging
import net.liftweb.json._
import net.liftweb.json.JsonAST.JObject

/** Play framework style model companion
 *  <p/>
 *  {{{
 *   package model
 *
 *  import com.novus.salat.annotations._
 *  import com.mongodb.casbah.Imports._
 *  import com.novus.salat.dao.{ SalatDAO, ModelCompanion }
 *
 *  object MyModel extends ModelCompanion[MyModel, ObjectId] {
 *    val collection = MongoConnection()("my_db")("my_model_coll")
 *    val dao = new SalatDAO[MyModel, ObjectId](collection = collection) {}
 *  }
 *
 *  case class MyModel(@Key("_id") id: ObjectId,
 *                     x: String,
 *                     y: Int,
 *                     z: List[Double])
 *  }}}
 *
 *  @tparam ObjectType type of object to be serialized
 *  @tparam ID type of object id to be serialized
 */
trait ModelCompanion[ObjectType <: AnyRef, ID <: Any] extends BaseDAOMethods[ObjectType, ID] with Logging {

  def dao: DAO[ObjectType, ID]

  /** In the absence of a specified write concern, supplies a default write concern.
   *  @return default write concern to use for insert, update, save and remove operations
   */
  def defaultWriteConcern = dao.defaultWriteConcern

  //
  // convenient access to methods on Grater
  //

  /** @param t object to be serialized
   *  @return object serialized as `DBObject`
   */
  def toDBObject(t: ObjectType): DBObject = dao._grater.asDBObject(t)

  /** @param dbo `DBObject` to be deserialized
   *  @return `DBObject` deserialized to object
   *  TODO - bring back view bound...  assuming it could possibly be worth the bother.
   */
  def toObject(dbo: DBObject): ObjectType = dao._grater.asObject(dbo)

  /** @param t object to be serialized
   *  @return object serialized to `JObject`
   *  @see http://www.assembla.com/spaces/liftweb/wiki/JSON_Support
   */
  def toJson(t: ObjectType): JObject = dao._grater.toJSON(t)

  /** @param t object to be serialized
   *  @return object serialized as pretty JSON to a String
   *  @see http://www.assembla.com/spaces/liftweb/wiki/JSON_Support
   */
  def toPrettyJson(t: ObjectType): String = dao._grater.toPrettyJSON(t)

  /** @param t object to be serialized
   *  @return object serialized as pretty JSON to a String
   *  @see http://www.assembla.com/spaces/liftweb/wiki/JSON_Support
   */
  def toCompactJson(t: ObjectType): String = dao._grater.toCompactJSON(t)

  /** @param t collection to be serialized
   *  @return collection of model objects serialized as a JSON array of `JObject`
   */
  def toJSONArray(t: Traversable[ObjectType]) = dao._grater.toJSONArray(t)

  /** @param t collection to be serialized
   *  @return collection of model objects serialized to a JSON array and rendered as pretty JSON
   */
  def toPrettyJSONArray(t: Traversable[ObjectType]) = dao._grater.toPrettyJSONArray(t)

  /** @param t collection to be serialized
   *  @return collection of model objects serialized to a JSON array and rendered as compact JSON
   */
  def toCompactJSONArray(t: Traversable[ObjectType]) = dao._grater.toCompactJSONArray(t)

  /** @param j `JObject` to be deserialized
   *  @return `JObject` deserialized to a model object
   */
  def fromJSON(j: JObject): ObjectType = dao._grater.fromJSON(j)

  /** @param s string representing a valid JSON object
   *  @return JSON deserialized to a model object
   */
  def fromJSON(s: String): ObjectType = dao._grater.fromJSON(s)

  /** @param j JSON array of valid `JObject`s
   *  @return deserialized list of model objects
   */
  def fromJSONArray(j: JArray) = dao._grater.fromJSONArray(j)

  /** @param s string representing a JSON array of valid `JObject`s
   *  @return deserialized list of model objects
   */
  def fromJSONArray(s: String) = dao._grater.fromJSONArray(s)

  /** @param t model object instance
   *  @return a map populated with the field names and values of the model object
   */
  def toMap(t: ObjectType): Map[String, Any] = dao._grater.toMap(t)

  /** @param m a map populated with the field names and values of the model object
   *  @return model object instance
   */
  def fromMap(m: Map[String, Any]): ObjectType = dao._grater.fromMap(m)

  //
  // convenient access to methods on SalatDAO
  //

  /** Count the number of documents matching the search criteria.
   *  @param q object for which to search
   *  @param fieldsThatMustExist list of field keys that must exist
   *  @param fieldsThatMustNotExist list of field keys that must not exist
   *  @return count of documents matching the search criteria
   */
  def count(q: DBObject, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil) =
    dao.count(q, fieldsThatMustExist, fieldsThatMustExist)

  /** @param ref object for which to search
   *  @param keys fields to return
   *  @tparam A type view bound to DBObject
   *  @tparam B type view bound to DBObject
   *  @return a typed cursor to iterate over results
   */
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B) = dao.find(ref, keys)

  /** @param t object for which to search
   *  @tparam A type view bound to DBObject
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOne[A <% DBObject](t: A) = dao.findOne(t)

  /** @param id identifier
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOneById(id: ID) = dao.findOneById(id)

  /** @param query query
   *  @tparam A type view bound to DBObject
   *  @return list of IDs
   */
  def ids[A <% DBObject](query: A) = dao.ids(query)

  /** @param docs collection of `ObjectType` instances to insert
   *  @param wc write concern
   *  @return list of object ids
   *         TODO: flatten list of IDs - why on earth didn't I do that in the first place?
   */
  def insert(docs: Traversable[ObjectType], wc: WriteConcern) = dao.insert(docs, wc)

  /** @param t instance of ObjectType
   *  @param wc write concern
   *  @return if insert succeeds, ID of inserted object
   */
  def insert(t: ObjectType, wc: WriteConcern) = dao.insert(t, wc)

  /** @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (Option[P]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def primitiveProjection[P](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.primitiveProjection[P](query, field)

  /** @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (List[P]) of the objects found
   */
  def primitiveProjections[P](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.primitiveProjections[P](query, field)

  /** @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (Option[P]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.projection[P](query, field)

  /** @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (List[P]) of the objects found
   */
  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context) =
    dao.projections[P](query, field)

  /** @param t object to remove from the collection
   *  @param wc write concern
   */
  def remove(t: ObjectType, wc: WriteConcern = defaultWriteConcern) {
    dao.remove(t, wc)
  }

  /** @param q the object that documents to be removed must match
   *  @param wc write concern
   *  @tparam A
   */
  def remove[A <% DBObject](q: A, wc: WriteConcern) {
    dao.remove(q, wc)
  }

  /** @param id the ID of the document to be removed
   *  @param wc write concern
   */
  def removeById(id: ID, wc: WriteConcern = defaultWriteConcern) {
    dao.removeById(id, wc)
  }

  /** @param ids the list of IDs identifying the list of documents to be removed
   *  @param wc wrote concern
   */
  def removeByIds(ids: List[ID], wc: WriteConcern = defaultWriteConcern) {
    dao.removeByIds(ids, wc)
  }

  /** @param t object to save
   *  @param wc write concern
   */
  def save(t: ObjectType, wc: WriteConcern = defaultWriteConcern) {
    dao.save(t, wc)
  }

  def update(q: DBObject, o: DBObject, upsert: Boolean, multi: Boolean, wc: WriteConcern = defaultWriteConcern): WriteResult = {
    dao.update(q, o, upsert, multi, wc)
  }

  //
  // methods I can't see the point of personally, but which pay some distant obeisance to the Platonic DAO carried
  // forward like pyramid blocks by my predecessors
  //

  /** @return (Iterator[ObjectType]) iterable result cursor of everything in the collection
   */
  def findAll(): SalatMongoCursor[ObjectType] = dao.find(MongoDBObject.empty)
}
