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
import com.mongodb.casbah.Imports._
import org.specs2.specification.Scope

class SalatDAOSpec extends SalatSpec {

  // which most specs can execute concurrently, this particular spec needs to execute sequentially to avoid mutating shared state,
  // namely, the MongoDB collection referenced by the AlphaDAO
  override def is = args(sequential = true) ^ super.is

  val alpha1 = Alpha(id = 1, beta = List[Beta](Gamma("gamma3"), Delta("delta3", "sampi3")))
  val alpha2 = Alpha(id = 2, beta = List[Beta](Gamma("gamma2"), Delta("delta2", "sampi2"), Delta("digamma2", "san2")))
  val alpha3 = Alpha(id = 3, beta = List[Beta](Gamma("gamma3"), Delta("delta3", "sampi3")))
  val alpha4 = Alpha(id = 4, beta = List[Beta](Delta("delta4", "koppa4")))
  val alpha5 = Alpha(id = 5, beta = List[Beta](Gamma("gamma5"), Gamma("gamma5-1")))
  val alpha6 = Alpha(id = 6, beta = List[Beta](Delta("delta6", "heta2"), Gamma("gamma6")))

  "Salat simple DAO" should {

    "insert a case class" in new context {
      val wr = AlphaDAO.insert(alpha3)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 1L

      val dbo: MongoDBObject = MongoConnection()(SalatSpecDb)(DaoSpecColl).findOne().get
      grater[Alpha].asObject(dbo) must_== alpha3
    }

    "insert a collection of case classes" in new context {
      val wr = AlphaDAO.insert(alpha4, alpha5, alpha6)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 3L

      // the standard collection cursor returns DBOs
      val mongoCursor = AlphaDAO.collection.find()
      mongoCursor.next() must_== grater[Alpha].asDBObject(alpha4)
      mongoCursor.next() must_== grater[Alpha].asDBObject(alpha5)
      mongoCursor.next() must_== grater[Alpha].asDBObject(alpha6)

      // BUT the Salat DAO returns a cursor types to case classes!
      val salatCursor = AlphaDAO.find(MongoDBObject())
      salatCursor.next must_== alpha4
      salatCursor.next must_== alpha5
      salatCursor.next must_== alpha6
    }

    "support findOne returning Option[T]" in new context {
      val wr = AlphaDAO.insert(alpha4, alpha5, alpha6)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 3L

      // note: you can query using an object transformed into a dbo
      AlphaDAO.findOne(grater[Alpha].asDBObject(alpha6)) must beSome(alpha6)
    }

    "support findOneById returning Option[T]" in new context {
      val wr = AlphaDAO.insert(alpha4, alpha5, alpha6)
      wr.getLastError.getErrorMessage must beNull

      AlphaDAO.collection.count must_== 3L

      AlphaDAO.findOneByID(id = 5.asInstanceOf[AnyRef]) must beSome(alpha5)
    }

    "support saving a case class" in new context {

      val wr = AlphaDAO.insert(alpha3)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 1L

      val alpha3_* = alpha3.copy(beta = List[Beta](Gamma("gamma3")))
      alpha3_* must_!= alpha3
      val wr_* = AlphaDAO.save(alpha3_*)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 1L

      val dbo: MongoDBObject = MongoConnection()(SalatSpecDb)(DaoSpecColl).findOne().get
      grater[Alpha].asObject(dbo) must_== alpha3_*
    }

    "support removing a case class" in new context {
      val wr = AlphaDAO.insert(alpha4, alpha5, alpha6)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 3L

      val wr_* = AlphaDAO.remove(alpha5)
      wr_*.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 2L

      AlphaDAO.findOne(grater[Alpha].asDBObject(alpha5)) must beNone

      // and then there were two!
      val salatCursor = AlphaDAO.find(MongoDBObject())
      salatCursor.next must_== alpha4
      salatCursor.next must_== alpha6
    }

    "support find returning a Mongo cursor typed to a case class" in new context {
      val wr = AlphaDAO.insert(alpha1, alpha2, alpha3, alpha4, alpha5, alpha6)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 6L

      val salatCursor = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
      salatCursor.next must_== alpha2
      salatCursor.next must_== alpha3
      salatCursor.next must_== alpha4
      salatCursor.next must_== alpha5
      salatCursor.next must_== alpha6
      salatCursor.hasNext must beFalse

      val salatCursor2 = AlphaDAO.find(ref = grater[Alpha].asDBObject(alpha6))
      salatCursor2.next must_== alpha6
      salatCursor2.hasNext must beFalse

      // works with limits!
      val salatCursor3 = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2))).limit(2)
      salatCursor3.next must_== alpha2
      salatCursor3.next must_== alpha3
      salatCursor3.hasNext must beFalse

      // works with limits and skip
      val salatCursor4 = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
        .skip(2)
        .limit(1)
      salatCursor4.next must_== alpha4
      salatCursor4.hasNext must beFalse

      // works with limits and skip
      val salatCursor5 = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> 2)))
        .sort(orderBy = MongoDBObject("_id" -> -1)) // sort by _id desc
        .skip(1)
        .limit(1)
      salatCursor5.next must_== alpha5
      salatCursor5.hasNext must beFalse
    }

    "support find with a set of keys" in new context {
      val wr = AlphaDAO.insert(alpha1, alpha2, alpha3, alpha4, alpha5, alpha6)
      wr.getLastError.getErrorMessage must beNull
      AlphaDAO.collection.count must_== 6L

      val salatCursor = AlphaDAO.find(ref = MongoDBObject("_id" -> MongoDBObject("$lt" -> 3)),
        keys = MongoDBObject("beta" -> 0))  // forces beta key to be excluded
      salatCursor.next must_== alpha1.copy(beta = Nil)
      salatCursor.next must_== alpha2.copy(beta = Nil)
      salatCursor.hasNext must beFalse
    }
  }

    trait context extends Scope {
      log.info("before: dropping %s", AlphaDAO.collection.getFullName())
      AlphaDAO.collection.drop()
      AlphaDAO.collection.count must_== 0L
    }
}