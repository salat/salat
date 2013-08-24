/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ChildCollectionSpec.scala
 * Last modified: 2012-10-15 20:40:58 EDT
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

import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.test._
import com.novus.salat.test.global._
import org.joda.time.DateMidnight
import org.joda.time.DateTimeConstants._
import org.specs2.specification.Scope

class ChildCollectionSpec extends SalatSpec {

  // force spec to run sequentially
  sequential

  implicit val wc = ParentDAO.defaultWriteConcern

  "SalatDAO's child collection trait" should {
    "support finding children by typed parent id" in new parentChildContext {
      ParentDAO.children.findByParentId(parent1.id).toList must contain(exactly(child1Parent1, child2Parent1, child3Parent1))
      ParentDAO.children.findByParentId(parent2.id).toList must contain(exactly(child1Parent2, child2Parent2))
      ParentDAO.children.findByParentId(parent3.id).toList must beEmpty
    }

    "support finding children by parent id with key includes" in new parentChildContext {
      ParentDAO.children.findByParentId(parentId = parent1.id, query = MongoDBObject.empty, keys = MongoDBObject("parentId" -> 1, "y" -> 1)).toList must contain(exactly(
        child1Parent1.copy(x = "", childInfo = ChildInfo()),
        child2Parent1.copy(x = "", childInfo = ChildInfo()),
        child3Parent1.copy(x = "", childInfo = ChildInfo())))
      ParentDAO.children.findByParentId(parentId = parent2.id, query = MongoDBObject.empty, keys = MongoDBObject("parentId" -> 1, "y" -> 1)).toList must contain(exactly(
        child1Parent2.copy(x = "", childInfo = ChildInfo()),
        child2Parent2.copy(x = "", childInfo = ChildInfo())))
      ParentDAO.children.findByParentId(parent3.id).toList must beEmpty
    }

    "support finding child IDs by typed parent id" in new parentChildContext {
      ParentDAO.children.idsForParentId(parent1.id).toList must contain(exactly(1, 2, 3))
      ParentDAO.children.idsForParentId(parent2.id).toList must contain(exactly(4, 5))
      ParentDAO.children.idsForParentId(parent3.id).toList must beEmpty
    }

    "support updating children by typed parent id" in new parentChildContext {
      val newLastUpdated = new DateMidnight(1812, FEBRUARY, 7).toDateTime
      val updateQuery = MongoDBObject("$set" -> MongoDBObject("childInfo.lastUpdated" -> newLastUpdated))
      val cr = ParentDAO.children.updateByParentId(parent1.id, updateQuery, false, true)

      // number of children is unchanged
      ParentDAO.children.collection.count() must_== 5L
      // children of parent1 are updated as expected
      ParentDAO.children.findByParentId(parent1.id).toList must contain(exactly(
        child1Parent1.copy(childInfo = ChildInfo(lastUpdated = newLastUpdated)),
        child2Parent1.copy(childInfo = ChildInfo(lastUpdated = newLastUpdated)),
        child3Parent1.copy(childInfo = ChildInfo(lastUpdated = newLastUpdated))))
      // child collection is otherwise unchanged
      ParentDAO.children.findByParentId(parent2.id).toList must contain(exactly(child1Parent2, child2Parent2))
      ParentDAO.children.findByParentId(parent3.id).toList must beEmpty
    }

    "support removing children by parent id" in new parentChildContext {
      val cr = ParentDAO.children.removeByParentId(parent1.id)

      // three children of parent1 have been removed from the child collection, overall count is reduced
      ParentDAO.children.findByParentId(parent1.id).toList must beEmpty
      ParentDAO.children.collection.count() must_== 2L
      // child collection is otherwise unchanged
      ParentDAO.children.findByParentId(parent2.id).toList must contain(exactly(child1Parent2, child2Parent2))
      ParentDAO.children.findByParentId(parent3.id).toList must beEmpty
    }

    "support primitive projections by parent id" in new parentChildContext {
      ParentDAO.children.primitiveProjectionsByParentId[String](parent1.id, "x") must contain(exactly("child1Parent1",
        "child2Parent1", "child3Parent1"))
      ParentDAO.children.primitiveProjectionsByParentId[String](parent2.id, "x") must contain(exactly("child1Parent2",
        "child2Parent2"))
      ParentDAO.children.primitiveProjectionsByParentId[String](parent3.id, "x") must beEmpty
    }

    "support case class projections by parent id" in new parentChildContext {
      ParentDAO.children.projectionsByParentId[ChildInfo](parent1.id, "childInfo") must contain(exactly(child1Parent1.childInfo,
        child2Parent1.childInfo, child3Parent1.childInfo))
      ParentDAO.children.projectionsByParentId[ChildInfo](parent2.id, "childInfo") must contain(exactly(child1Parent2.childInfo,
        child2Parent2.childInfo))
      ParentDAO.children.projectionsByParentId[ChildInfo](parent3.id, "childInfo") must beEmpty
    }

    "support counting by parent id" in new parentChildContext {
      ParentDAO.children.countByParentId(parent1.id) must_== 3L
      ParentDAO.children.countByParentId(parent2.id) must_== 2L
      ParentDAO.children.countByParentId(parent3.id) must_== 0L
    }

    "support counting by parent id with fields" in new parentChildContext {
      ParentDAO.children.countByParentId(parentId = parent1.id,
        fieldsThatMustExist = List("x")) must_== 3L
      ParentDAO.children.countByParentId(parentId = parent1.id,
        fieldsThatMustExist = List("x"),
        fieldsThatMustNotExist = List("y")) must_== 2L
      ParentDAO.children.countByParentId(parentId = parent1.id,
        fieldsThatMustExist = List("x", "y")) must_== 1L
    }

  }

  trait parentChildContext extends Scope {
    log.debug("before: dropping %s", ParentDAO.collection.getFullName())
    ParentDAO.collection.drop()
    ParentDAO.collection.count() must_== 0L

    val childDAO = ParentDAO.children
    log.debug("before: dropping %s", childDAO.collection.getFullName())
    childDAO.collection.drop()
    childDAO.collection.count() must_== 0L

    val parent1 = Parent(name = "parent1")
    val parent2 = Parent(name = "parent2")
    val parent3 = Parent(name = "parent3")

    val _ids = ParentDAO.insert(parent1, parent2, parent3)
    _ids must contain(exactly(Option(parent1.id), Option(parent2.id), Option(parent3.id)))
    ParentDAO.collection.count() must_== 3L

    val child1Parent1 = Child(id = 1, parentId = parent1.id, x = "child1Parent1", y = Some("child1Parent1"))
    val child2Parent1 = Child(id = 2, parentId = parent1.id, x = "child2Parent1")
    val child3Parent1 = Child(id = 3, parentId = parent1.id, x = "child3Parent1")

    val child1Parent2 = Child(id = 4, parentId = parent2.id, x = "child1Parent2", y = Some("child1Parent2"))
    val child2Parent2 = Child(id = 5, parentId = parent2.id, x = "child2Parent2", y = Some("child2Parent2"))

    val childIds = ParentDAO.children.insert(child1Parent1, child2Parent1, child3Parent1, child1Parent2, child2Parent2)
    childIds must contain(exactly(Option(child1Parent1.id), Option(child2Parent1.id), Option(child3Parent1.id),
      Option(child1Parent2.id), Option(child2Parent2.id)))
    ParentDAO.children.collection.count() must_== 5L
  }

}