/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         DAO.scala
 * Last modified: 2012-06-04 12:12:46 EDT
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
import com.mongodb.{DBObject, WriteConcern}
import com.novus.salat._

/** Base DAO class.
 *  <p/>
 *  Where `WriteConcern` is not specified as a parameter on an operation which modifies or removes documents from the
 *  collection, then the default write concern of the collection is assumed.
 *
 *  @tparam ObjectType class to be persisted
 *  @tparam ID _id type
 */
trait DAO[ObjectType <: AnyRef, ID <: Any] {

  // TODO: replace requirement for lists to traversables

  /** @return MongoDB collection
   */
  def collection: MongoCollection

  /** @return [[com.novus.salat.Grater]] to serialize and deserialize `ObjectType`
   */
  def _grater: Grater[ObjectType]

  /** @return DAO description for logging
   */
  def description: String = "DAO"

  /** Inserts a document into the database.
   *  @param t instance of ObjectType
   *  @return if insert succeeds, ID of inserted object
   */
  def insert(t: ObjectType): Option[ID] = insert(t = t, wc = collection.writeConcern)

  /** Inserts a document into the database.
   *  @param t instance of ObjectType
   *  @param wc write concern
   *  @return if insert succeeds, ID of inserted object
   */
  def insert(t: ObjectType, wc: WriteConcern): Option[ID]

  /** Inserts a group of documents into the database.
   *  @param docs variable length argument of ObjectType instances
   *  @param wc write concern
   *  @return if write concern succeeds, a list of object IDs
   *  TODO: replace vararg with traversable
   *  TODO: flatten list of IDs - why on earth didn't I do that in the first place?
   */
  def insert(docs: ObjectType*)(implicit wc: WriteConcern = collection.writeConcern): List[Option[ID]]

  /** Queries for a list of identifiers.
   *  @param query query
   *  @tparam A type view bound to DBObject
   *  @return list of IDs
   */
  def ids[A <% DBObject](query: A): List[ID]

  /** Queries for an object in this collection.
   *  @param ref object for which to search
   *  @tparam A type view bound to DBObject
   *  @return a typed cursor to iterate over results
   */
  def find[A <% DBObject](ref: A): SalatMongoCursor[ObjectType] = find(ref = ref, keys = MongoDBObject.empty)

  /** Queries for an object in this collection.
   *  @param ref object for which to search
   *  @param keys fields to return
   *  @tparam A type view bound to DBObject
   *  @tparam B type view bound to DBObject
   *  @return a typed cursor to iterate over results
   */
  def find[A <% DBObject, B <% DBObject](ref: A, keys: B): SalatMongoCursor[ObjectType]

  /** Returns a single object from this collection.
   *  @param t object for which to search
   *  @tparam A type view bound to DBObject
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOne[A <% DBObject](t: A): Option[ObjectType]

  /** Find an object by its ID.
   *  @param id identifier
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   *  @deprecated since 0.0.8
   */
  @deprecated("Use findOneById instead") def findOneByID(id: ID): Option[ObjectType] = findOneById(id)

  /** Find an object by its ID.
   *  @param id identifier
   *  @return (Option[ObjectType]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def findOneById(id: ID): Option[ObjectType]

  /** Saves an object to this collection.
   *  @param t object to save
   *  @param wc write concern
   */
  def save(t: ObjectType, wc: WriteConcern)

  /** Saves an object to this collection.
   *  @param t object to save
   */
  def save(t: ObjectType) {
    save(t = t, wc = collection.writeConcern)
  }

  /** Performs an update operation.
   *  @param q search query for old object to update
   *  @param o object with which to update <tt>q</tt>
   *  @param upsert if the database should create the element if it does not exist
   *  @param multi if the update should be applied to all objects matching
   *  @param wc write concern
   *  @tparam A type view bound to DBObject
   *  @tparam B type view bound to DBObject
   */
  def update[A <% DBObject, B <% DBObject](q: A, o: B, upsert: Boolean, multi: Boolean, wc: WriteConcern)

  /** Performs an update operation.
   *  @param q search query for old object to update
   *  @param o object with which to update <tt>q</tt>
   *  @param upsert if the database should create the element if it does not exist
   *  @param multi if the update should be applied to all objects matching
   *  @param wc write concern
   *  @tparam A type view bound to DBObject
   */
  def update[A <% DBObject](q: A, o: ObjectType, upsert: Boolean, multi: Boolean, wc: WriteConcern) {
    update(q, _grater.asDBObject(o), upsert, multi, wc)
  }

  /** Remove a matching object from the collection
   *  @param t object to remove from the collection
   */
  def remove(t: ObjectType) {
    remove(t = t, wc = collection.writeConcern)
  }

  /** Remove a matching object from the collection
   *  @param t object to remove from the collection
   *  @param wc write concern
   */
  def remove(t: ObjectType, wc: WriteConcern)

  /** Removes objects from the database collection.
   *  @param q the object that documents to be removed must match
   */
  def remove[A <% DBObject](q: A) {
    remove(q = q, wc = collection.writeConcern)
  }

  /** Removes objects from the database collection.
   *  @param q the object that documents to be removed must match
   *  @param wc write concern
   */
  def remove[A <% DBObject](q: A, wc: WriteConcern)

  /** Remove document identified by this ID.
   *  @param id the ID of the document to be removed
   *  @param wc write concern
   */
  def removeById(id: ID, wc: WriteConcern = collection.writeConcern)

  /** Remove documents matching any of the supplied list of IDs.
   *  @param ids the list of IDs identifying the list of documents to be removed
   *  @param wc wrote concern
   */
  def removeByIds(ids: List[ID], wc: WriteConcern = collection.writeConcern)

  /** Count the number of documents matching the search criteria.
   *  @param q object for which to search
   *  @param fieldsThatMustExist list of field keys that must exist
   *  @param fieldsThatMustNotExist list of field keys that must not exist
   *  @return count of documents matching the search criteria
   */
  def count(q: DBObject = MongoDBObject.empty, fieldsThatMustExist: List[String] = Nil, fieldsThatMustNotExist: List[String] = Nil): Long

  /** Projection typed to a case class, trait or abstract superclass.
   *  @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (Option[P]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def projection[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  /** Projection typed to a type for which Casbah or mongo-java-driver handles conversion
   *  @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (Option[P]) Some() of the object found, or <code>None</code> if no such object exists
   */
  def primitiveProjection[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): Option[P]

  /** Projection typed to a case class, trait or abstract superclass.
   *  @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (List[P]) of the objects found
   */
  def projections[P <: CaseClass](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]

  /** Projection typed to a type for which Casbah or mongo-java-driver handles conversion
   *  @param query object for which to search
   *  @param field field to project on
   *  @param m implicit manifest typed to `P`
   *  @param ctx implicit [[com.novus.salat.Context]]
   *  @tparam P type of projected field
   *  @return (List[P]) of the objects found
   */
  def primitiveProjections[P <: Any](query: DBObject, field: String)(implicit m: Manifest[P], ctx: Context): List[P]
}
