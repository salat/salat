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

import com.novus.salat.test._
import com.novus.salat._
import com.novus.salat.global._
import org.specs2.execute.Success
import com.mongodb.casbah.commons.Logging
import org.specs2.specification.Before
import com.mongodb.casbah.Imports._


class SalatDAOSpec extends SalatSpec {

  // TODO: neither of these seem to run in expected sequence
//  object context extends Before with Logging {
//    def before = {
//      log.info("before: dropping %s", FooDAO.collection.getName())
//      FooDAO.collection.drop()
//      FooDAO.collection.count must_== 0L
//    }
//  }
//
//  trait context extends Success {
//      log.info("before: dropping %s", FooDAO.collection.getFullName())
//      FooDAO.collection.drop()
//      FooDAO.collection.count must_== 0L
//  }

  // TODO: replace this with a context that handles this before each step - need to get on the specs2 mailing group
  def cleanUpCollection: Unit = {
    step {
      log.info("before: dropping %s", FooDAO.collection.getFullName())
      FooDAO.collection.drop()
      FooDAO.collection.count must_== 0L
    }
  }

  val foo3 = Foo(x = 3, y = "Turning and turning in the widening gyre")
  val foo4 = Foo(x = 4, y = "The falcon cannot hear the falconer")
  val foo5 = Foo(x = 5, y = "Things fall apart; the centre cannot hold")
  val foo6 = Foo(x = 6, y = "Mere anarchy is loosed upon the world")

  "Salat simple DAO" should {

    cleanUpCollection

    "insert a case class" in {

      val wr = FooDAO.insert(foo3)
      wr.getLastError.getErrorMessage must beNull
      FooDAO.collection.count must_== 1L

      val dbo: MongoDBObject = MongoConnection()(SalatSpecDb)("foo-dao-spec").findOne().get
      grater[Foo].asObject(dbo) must_== foo3
    }

    cleanUpCollection


    "insert a collection of case classes" in {
      val wr = FooDAO.insert(foo4, foo5, foo6)
       wr.getLastError.getErrorMessage must beNull

      FooDAO.collection.count must_== 3L
    }

    cleanUpCollection

    "support findOne returning Option[T]" in {
      val wr = FooDAO.insert(foo4, foo5, foo6)
      wr.getLastError.getErrorMessage must beNull

      FooDAO.collection.count must_== 3L

      // note: you can query using an object transformed into a dbo
      val foo6_* = FooDAO.findOne(grater[Foo].asDBObject(foo6))
      foo6_* must beSome(foo6)
    }

    cleanUpCollection

    "support findOneById returning Option[T]" in {

    }

  }

}