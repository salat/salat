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
import com.mongodb.DBCursor
import com.mongodb.casbah.{MongoCursorBase, CursorExplanation, Imports}

trait DAO[T <: CaseClass] {

  val collection: MongoCollection

  val _grater: Grater[T]

  def insert(t: T): WriteResult
  def insert(docs: T*): WriteResult

  def find[A <% DBObject](ref: A): SalatMongoCursor[T]
  def find[A <% DBObject](ref: A, keys: A): SalatMongoCursor[T]

  def findOne[A <% DBObject](t: A): Option[T]
  def findOneByID(id: AnyRef): Option[T]

  def save(t: T): WriteResult

  def remove(t: T): WriteResult
}


abstract class SalatDAO[T <: CaseClass : Manifest] extends com.novus.salat.dao.DAO[T] {

  def insert(t: T) = collection.insert(_grater.asDBObject(t))
  def insert(docs: T*) = collection.insert(docs.map(t => _grater.asDBObject(t)): _*)

  def findOne[A <% DBObject](t: A) = collection.findOne(t).map(_grater.asObject(_))

  def findOneByID(id: AnyRef) = collection.findOneByID(id).map(_grater.asObject(_))

  def remove(t: T) = collection.remove(_grater.asDBObject(t))

  def save(t: T) = collection.save(_grater.asDBObject(t))

  def update[A <% DBObject](q: A, o: A) = collection.update(q, o)

  def find[A <% DBObject](ref: A, keys: A) = SalatMongoCursor[T](_grater,
    collection.find(ref, keys).asInstanceOf[MongoCursorBase].underlying)

  def find[A <% DBObject](ref: A) = find(ref.asInstanceOf[DBObject], MongoDBObject())

}