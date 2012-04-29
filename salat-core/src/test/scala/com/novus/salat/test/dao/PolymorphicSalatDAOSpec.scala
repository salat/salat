/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         PolymorphicSalatDAOSpec.scala
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

import com.novus.salat.test._
import com.mongodb.casbah.Imports._
import org.specs2.specification.Scope
import com.mongodb.casbah.commons.MongoDBObject

class PolymorphicSalatDAOSpec extends SalatSpec {

  override def is = args(sequential = true) ^ super.is

  trait userContext extends Scope {
    log.debug("before: dropping %s", UserDAO.collection.getFullName())
    log.debug("before: dropping %s", RoleDAO.collection.getFullName())
    UserDAO.collection.drop()
    UserDAO.collection.count must_== 0L
    RoleDAO.collection.drop()
    RoleDAO.collection.count must_== 0L

    val userId = new ObjectId
    val editorId = new ObjectId
    val authorId = new ObjectId
    val editor: Role = Editor(_id = editorId, userId = userId)
    val author: Role = Author(_id = authorId, userId = userId)
    val user = User(_id = userId, name = "Polly", roles = List(editor, author))
  }

  trait roleContext extends Scope {
    log.debug("before: dropping %s", RoleDAO.collection.getFullName())
    RoleDAO.collection.drop()
    RoleDAO.collection.count must_== 0L
    val user1Id = new ObjectId
    val user2Id = new ObjectId
    val user3Id = new ObjectId

    val guestId = new ObjectId
    val editorId = new ObjectId
    val authorId = new ObjectId
    val adminId = new ObjectId

    val guest: Role = Guest(_id = guestId, userId = user1Id)
    val editor: Role = Editor(_id = editorId, userId = user2Id)
    val author: Role = Author(_id = authorId, userId = user3Id)
    val admin: Role = Admin(_id = adminId, userId = user3Id)
  }

  "Salat DAO" should {
    "support a top-level polymorphic collection" in new roleContext {
      RoleDAO.insert(guest) must beSome(guestId)
      RoleDAO.findOneByID(guestId) must beSome(guest)
      RoleDAO.find(MongoDBObject("userId" -> user1Id)).toList must contain(guest).only

      RoleDAO.insert(editor, author, admin).flatten must contain(editorId, authorId, adminId).only
      RoleDAO.findOneByID(authorId) must beSome(author)
      RoleDAO.findOneByID(editorId) must beSome(editor)
      RoleDAO.findOneByID(adminId) must beSome(admin)
      RoleDAO.find(MongoDBObject("userId" -> user2Id)).toList must contain(editor).only
      RoleDAO.find(MongoDBObject("userId" -> user3Id)).toList must contain(author, admin).only
    }
    "support a polymorphic child collection" in new userContext {
      UserDAO.insert(user)
      UserDAO.collection.count must_== 1L
      RoleDAO.collection.count must_== 2L

      val user_* = UserDAO.findOneByID(userId)
      user_* must beSome(user)

      val roles_* = UserDAO.roles.findByParentId(userId).toList
      roles_* must contain(author, editor).only
    }
  }

}
