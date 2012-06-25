/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         AlphaDAO.scala
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
package com.novus.salat.test.dao

import com.novus.salat._
import com.novus.salat.test._
import com.novus.salat.test.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._
import org.scala_tools.time.Imports._
import com.novus.salat.dao.SalatDAO
import org.joda.time.DateMidnight
import org.joda.time.DateTimeConstants._
import com.mongodb.casbah.Imports

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

case class ChildInfo(lastUpdated: DateTime = new DateMidnight(0L).toDateTime)
case class Child(@Key("_id") id: Int,
                 parentId: ObjectId,
                 x: String = "",
                 childInfo: ChildInfo = ChildInfo(),
                 y: Option[String] = None)
case class Parent(@Key("_id") id: ObjectId = new ObjectId, name: String)

object ParentDAO extends SalatDAO[Parent, ObjectId](collection = MongoConnection()(SalatSpecDb)(ParentColl)) {

  val children = new ChildCollection[Child, Int](collection = MongoConnection()(SalatSpecDb)(ChildColl),
    parentIdField = "parentId") {}

}

case class User(_id: ObjectId = new ObjectId, name: String, roles: List[Role])

sealed trait Role {
  val _id: ObjectId
  val userId: ObjectId
}
case class Guest(_id: ObjectId = new ObjectId, userId: ObjectId) extends Role
case class Author(_id: ObjectId = new ObjectId, userId: ObjectId) extends Role
case class Editor(_id: ObjectId = new ObjectId, userId: ObjectId) extends Role
case class Admin(_id: ObjectId = new ObjectId, userId: ObjectId) extends Role

object UserDAO extends SalatDAO[User, ObjectId](collection = MongoConnection()(SalatSpecDb)(UserColl)) {
  val roles = new ChildCollection[Role, ObjectId](collection = MongoConnection()(SalatSpecDb)(RoleColl),
    parentIdField = "userId") {}

  // demonstration of how you might break apart an object graph when saving to MongoDB
  override def insert(u: User) = {
    u.roles.foreach {
      role =>
        roles.insert(role)
    }
    super.insert(u.copy(roles = Nil))
  }

  // and reassemble the object graph when retrieving from MongoDB
  override def findOneById(id: ObjectId) = {
    super.findOneById(id).map {
      user =>
        user.copy(roles = roles.findByParentId(user._id).toList)
    }
  }
}

object RoleDAO extends SalatDAO[Role, ObjectId](collection = MongoConnection()(SalatSpecDb)(RoleColl))