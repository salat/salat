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
package com.novus.salat.test.dao

import com.novus.salat._
import com.novus.salat.test._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._
import org.scala_tools.time.Imports._
import com.novus.salat.dao.SalatDAO

@Salat
trait Beta {
   val y: String
}
case class Gamma(y: String) extends Beta
case class Delta(y: String, z: String) extends Beta
case class Alpha(@Key("_id") id: Int, beta: List[Beta] = Nil)


//
// Instant Salat DAO in under two minutes:
// 1.  extend com.novus.salat.dao.SalatDAO
// 2.  define a grater for the class
// 3.  specify a collection
//
object AlphaDAO extends SalatDAO[Alpha, Int](collection = MongoConnection()(SalatSpecDb)(AlphaColl))

case class Epsilon(@Key("_id") id: ObjectId = new ObjectId, notes: String)

object EpsilonDAO extends SalatDAO[Epsilon, ObjectId](collection = MongoConnection()(SalatSpecDb)(EpsilonColl))

case class Theta(@Key("_id") id: ObjectId = new ObjectId, x: String, y: String)
case class Xi(@Key("_id") id: ObjectId = new ObjectId, x: String, y: Option[String])
case class Nu(x: String, y: String)
case class Kappa(@Key("_id") id: ObjectId = new ObjectId, k: String, nu: Nu)

object ThetaDAO extends SalatDAO[Theta, ObjectId](collection = MongoConnection()(SalatSpecDb)(ThetaColl))

object XiDAO extends SalatDAO[Xi, ObjectId](collection = MongoConnection()(SalatSpecDb)(XiColl))

object KappaDAO extends SalatDAO[Kappa, ObjectId](collection = MongoConnection()(SalatSpecDb)(KappaColl))

case class ChildInfo(lastUpdated: DateTime = DateTime.now)
case class Child(@Key("_id") id: Int, parentId: ObjectId, x: String, childInfo: ChildInfo = ChildInfo())
case class Parent(@Key("_id") id: ObjectId = new ObjectId, name: String)

object ParentDAO extends SalatDAO[Parent, ObjectId](collection = MongoConnection()(SalatSpecDb)(ParentColl)) {

  val children = new ChildCollection[Child, Int](collection = MongoConnection()(SalatSpecDb)(ChildColl),
    parentIdField = "parentId") {}

}