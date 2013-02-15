/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         PolymorphicSalatDAOSpec.scala
 * Last modified: 2012-12-05 12:42:59 EST
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

import com.novus.salat.test._
import com.novus.salat.test.dao.when_necessary._
import com.mongodb.casbah.Imports._
import org.specs2.specification.Scope
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.{ ConcreteGrater, Grater }

class PolymorphicSalatDAOSpec extends SalatSpec {

  override def is = args(sequential = true) ^ super.is

  trait userContext extends Scope {
    log.debug("before: dropping %s", UserDAO.collection.getFullName())
    log.debug("before: dropping %s", RoleDAO.collection.getFullName())
    UserDAO.collection.drop()
    UserDAO.collection.count() must_== 0L
    RoleDAO.collection.drop()
    RoleDAO.collection.count() must_== 0L

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
    RoleDAO.collection.count() must_== 0L
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

  trait fooContext extends Scope {
    log.debug("before: dropping %s", FooDAO.collection.getFullName())
    FooDAO.collection.drop()
    FooDAO.collection.count() must_== 0L
    val barId = new ObjectId
    val bar = Bar(_id = barId, x = 1, y = "y", z = 3.14)
    FooDAO.insert(bar) must beSome(barId)
    val bazId = new ObjectId
    val baz = Baz(_id = bazId, x = -1, y = "y'", n = "n")
    FooDAO.insert(baz) must beSome(bazId)
    FooDAO.collection.count() must_== 2L
  }

  "Salat DAO" should {
    "support a top-level polymorphic collection with always-on type hinting" in new roleContext {
      RoleDAO.insert(guest) must beSome(guestId)
      RoleDAO.findOneById(guestId) must beSome(guest)
      RoleDAO.find(MongoDBObject("userId" -> user1Id)).toList must contain(guest).only

      RoleDAO.insert(editor, author, admin)(RoleDAO.defaultWriteConcern).flatten must contain(editorId, authorId, adminId).only
      RoleDAO.findOneById(authorId) must beSome(author)
      RoleDAO.findOneById(editorId) must beSome(editor)
      RoleDAO.findOneById(adminId) must beSome(admin)
      RoleDAO.find(MongoDBObject("userId" -> user2Id)).toList must contain(editor).only
      RoleDAO.find(MongoDBObject("userId" -> user3Id)).toList must contain(author, admin).only
    }
    "support a polymorphic child collection with always-on type hinting" in new userContext {
      UserDAO.insert(user)
      UserDAO.collection.count() must_== 1L
      RoleDAO.collection.count() must_== 2L

      val user_* = UserDAO.findOneById(userId)
      user_* must beSome(user)

      val roles_* = UserDAO.roles.findByParentId(userId).toList
      roles_* must contain(author, editor).only
    }
  }

  "An instance of SalatDAO typed to a trait or an abstract class" should {
    "figure out that it needs to force type hinting when inserting, updating and saving" in new fooContext {
      FooDAO.forceTypeHints must beTrue
      FooDAO.decorateDBO(bar).getAs[String](ctx.typeHintStrategy.typeHint) must beSome(bar.getClass.getName)
    }
    "not force appending type hints to find, update and count queries" in {
      FooDAO.appendTypeHintToQueries must beFalse
      FooDAO.decorateQuery(MongoDBObject.empty).getAs[String](ctx.typeHintStrategy.typeHint) must beNone
    }
    "force appending type hints when persisting objects" in new fooContext {
      FooDAO.collection.findOne(MongoDBObject("_id" -> barId)).flatMap(_.getAs[String](ctx.typeHintStrategy.typeHint)) must beSome(bar.getClass.getName)
      FooDAO.collection.findOne(MongoDBObject("_id" -> bazId)).flatMap(_.getAs[String](ctx.typeHintStrategy.typeHint)) must beSome(baz.getClass.getName)
    }
    "successfully deserialize concrete instances of the type hierarchy" in new fooContext {
      FooDAO.findOneById(barId) must beSome(bar)
      FooDAO.findOneById(bazId) must beSome(baz)
    }
    "update common fields across subclasses" in new fooContext {
      FooDAO.update(q = DBObject.empty, o = DBObject("$inc" -> DBObject("x" -> 1)), upsert = false, multi = true).getN must_== 2
      FooDAO.findOneById(barId).map(_.x) must beSome(2)
      FooDAO.findOneById(bazId).map(_.x) must beSome(0)
    }
    "count across subclasses" in new fooContext {
      FooDAO.count(DBObject.empty) must_== 2L
      FooDAO.count("x" $gt 0) must_== 1L
      FooDAO.count("x" $lt 0) must_== 1L
    }
  }

  "An instance of SalatDAO with ConcreteSubclassDAO" should {
    "force type hinting when inserting, updating and saving" in new fooContext {
      BarDAO.forceTypeHints must beTrue
      BarDAO.decorateDBO(bar).getAs[String](ctx.typeHintStrategy.typeHint) must beSome(bar.getClass.getName)
    }
    "force appending type hinting to find, update and count queries" in new fooContext {
      BarDAO.appendTypeHintToQueries must beTrue
      BarDAO.decorateQuery(MongoDBObject.empty).getAs[String](ctx.typeHintStrategy.typeHint) must beSome(bar.getClass.getName)
    }
    "limit find queries to instances of the concrete subclass" in new fooContext {
      // TODO: specs2 collection matchers REALLY don't like polymorphism
      val allFoos = FooDAO.find(MongoDBObject.empty).toList
      allFoos must haveSize(2)
      allFoos.contains(bar) must beTrue
      allFoos.contains(baz) must beTrue
      BarDAO.find(MongoDBObject.empty).toList must contain(bar).only
    }
    "limit count queries to instances of the concrete subclass" in new fooContext {
      BarDAO.count(MongoDBObject.empty) must_== 1L
      FooDAO.count(MongoDBObject.empty) must_== 2L
    }
    "force type hinting when inserting objects" in new fooContext {
      val bar2Id = new ObjectId
      val bar2 = Bar(_id = bar2Id, x = 5, y = "flight", z = 89.8)
      BarDAO.insert(bar2) must beSome(bar2Id)
      BarDAO.findOneById(bar2Id) must beSome(bar2)
      BarDAO.collection.findOne(MongoDBObject("_id" -> bar2Id)).flatMap(_.getAs[String](ctx.typeHintStrategy.typeHint)) must beSome(bar2.getClass.getName)
    }
    "limit updates to instances of the concrete subclass" in new fooContext {
      val bar2Id = new ObjectId
      val bar2 = Bar(_id = bar2Id, x = 5, y = "flight", z = 89.8)
      BarDAO.insert(bar2) must beSome(bar2Id)
      BarDAO.update(q = DBObject.empty, o = DBObject("$inc" -> DBObject("x" -> 1)), upsert = false, multi = true).getN must_== 2
      BarDAO.findOneById(barId).map(_.x) must beSome(bar.x + 1)
      BarDAO.findOneById(bar2Id).map(_.x) must beSome(bar2.x + 1)
      FooDAO.findOneById(bazId).map(_.x) must beSome(baz.x) // BarDAO update query did not affect instances of Baz!
    }
  }

  "An instance of SalatDAO for a type hierarchy member but without ConcreteSubclassDAO" should {
    val quxId = new ObjectId
    val qux = Qux(quxId, "bah")
    "not force type hinting when inserting, updating and saving" in {
      QuxDAO.forceTypeHints must beFalse
      QuxDAO._grater.asInstanceOf[ConcreteGrater[_]].useTypeHint must beFalse
      QuxDAO.decorateDBO(qux).getAs[String](ctx.typeHintStrategy.typeHint) must beNone
    }
    "not force appending type hints to find, update and count queries" in {
      QuxDAO.appendTypeHintToQueries must beFalse
      QuxDAO.decorateQuery(DBObject.empty) must_== DBObject.empty
    }
    "not forcibly append a type hint when an instance is inserted" in {
      QuxDAO.collection.findOne(MongoDBObject("_id" -> quxId)).flatMap(_.getAs[String](ctx.typeHintStrategy.typeHint)) must beNone
    }
  }
}
