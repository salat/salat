/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         FooDAO.scala
 * Last modified: 2012-12-05 12:31:49 EST
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */

package com.novus.salat.test.dao

import com.novus.salat.annotations._
import org.bson.types.ObjectId
import com.novus.salat.{TypeHintFrequency, StringTypeHintStrategy, Context}
import com.novus.salat.dao.{ConcreteSubclassDAO, SalatDAO}
import com.mongodb.casbah.Imports._
import com.novus.salat.StringTypeHintStrategy
import com.novus.salat.test._
import com.novus.salat.StringTypeHintStrategy

package object when_necessary {
  implicit val ctx = new Context {
    val name = "when_necessary"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
  }
}

import com.novus.salat.test.dao.when_necessary._

@Salat
sealed trait Foo {
  def _id: ObjectId
  def x: Int
  def y: String
}
case class Bar(_id: ObjectId, x: Int, y: String, z: Double) extends Foo
case class Baz(_id: ObjectId, x: Int, y: String, n: String) extends Foo

object FooDAO extends SalatDAO[Foo, ObjectId](MongoConnection()(SalatSpecDb)(ToValidateColl))
object BarDAO extends SalatDAO[Bar, ObjectId](MongoConnection()(SalatSpecDb)(ToValidateColl)) with ConcreteSubclassDAO
object BazDAO extends SalatDAO[Baz, ObjectId](MongoConnection()(SalatSpecDb)(ToValidateColl)) with ConcreteSubclassDAO

case class Qux(_id: ObjectId, x: String)
object QuxDAO extends SalatDAO[Qux, ObjectId](MongoConnection()(SalatSpecDb)(QuxColl))

