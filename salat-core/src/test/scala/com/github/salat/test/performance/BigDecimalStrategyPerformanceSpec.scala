/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         BigDecimalStrategyPerformanceSpec.scala
 * Last modified: 2015-06-23 20:52:14 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */

package com.github.salat.test.performance

import com.github.salat._
import com.github.salat.dao.SalatDAO
import com.github.salat.test.RichDuration._
import com.github.salat.util.Logging
import com.mongodb.casbah.Imports._
import org.specs2.mutable._
import org.specs2.specification.Scope

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class BigDecimalStrategyPerformanceSpec extends Specification with Logging {

  // force sequential run
  sequential

  case class bdc(strategy: BigDecimalStrategy) extends Scope {
    implicit val ctx = new Context {
      val name = "testBdc_%s".format(System.currentTimeMillis())
      override val typeHintStrategy = NeverTypeHint
      override val bigDecimalStrategy = strategy
    }

    val name = strategy.getClass.getSimpleName
    val coll = MongoConnection()("salat_test_performance")(name)
    coll.drop()
    coll.count() must_== 0L

    val generator = new Random()
    //    val rangeMin = -1000000d
    //    val rangeMax = 1000000d
    val outTimes = ArrayBuffer.empty[Long]
    val inTimes = ArrayBuffer.empty[Long]

    object FooDAO extends SalatDAO[Foo, ObjectId](collection = coll) {
      // TODO: this is a big disingenuous as some of the serialization is actually handled further down the stack by casbah or mongo-java-driver
      // but good enough for testing big decimal
      override def insert(t: Foo) = {
        coll.insert(timeAndLogNanos(_grater.asDBObject(t))(ns => inTimes += ns))
        Option(t._id)
      }

      def findAll(): List[Foo] = {
        val builder = List.newBuilder[Foo]
        val cursor = coll.find()
        while (cursor.hasNext) {
          val dbo = cursor.next()
          builder += timeAndLogNanos(_grater.asObject(dbo))(ns => outTimes += ns)
        }
        builder.result()
      }
    }

    def serialize(limit: Int) {
      //      log.debug("insert: %s - called with limit of %d", name, limit)
      for (i <- 0 until limit) {
        //        val r = (rangeMin + (rangeMax - rangeMin) * generator.nextGaussian()).toString
        val r = generator.nextGaussian().toString
        //        log.debug("serialize: r=%s", r)
        FooDAO.insert(Foo(x = BigDecimal(r, strategy.mathCtx)))
        if (i > 0 && i % 1000 == 0) {
          //          log.debug("insert: %s - %d of %d", name, i, limit)
        }
      }
      //      log.debug("inTimes: [%s]", inTimes.sorted.mkString(", "))
      inTimes.size must_== limit
    }

    def deserialize(limit: Int): List[Foo] = {
      val deserialized = FooDAO.findAll()
      //      log.debug("outTimes: [%s]", outTimes.sorted.mkString(", "))
      outTimes.size must_== limit
      deserialized.size must_== limit
      deserialized
    }

    def stats() {
      log.info(
        """ 

--------------------
      
COLLECTION: %s [%d entries]

STRATEGY: %s

SERIALIZATION TIMES:
  total: %s
  avg: %s ms
  median: %s ms

DESERIALIZATION TIMES:
  total: %s
  avg: %s ms
  median: %s ms

%s      

-------------------

      """,
        coll.name, coll.size,
        strategy.getClass.getName,
        (inTimes.sum / 1000000L).tersePrint, avg(inTimes) / 1000000d, median(inTimes) / 1000000d,
        (outTimes.sum / 1000000L).tersePrint, avg(outTimes) / 1000000d, median(outTimes) / 1000000d,
        coll.stats
      )
    }
  }

  "Testing performance of BigDecimalStrategy" should {
    val limit = 10000
    "test performance of BigDecimal <-> Binary" in new bdc(BigDecimalToBinaryStrategy(mathCtx = DefaultMathContext)) {
      serialize(limit)
      val deserialized = deserialize(limit)
      stats()
      deserialized.size must_== limit
    }
    "test performance of BigDecimal <-> Double" in new bdc(BigDecimalToDoubleStrategy(mathCtx = DefaultMathContext)) {
      serialize(limit)
      val deserialized = deserialize(limit)
      stats()
      deserialized.size must_== limit
    }
    "test performance of BigDecimal <-> String" in new bdc(BigDecimalToStringStrategy(mathCtx = DefaultMathContext)) {
      serialize(limit)
      val deserialized = deserialize(limit)
      stats()
      deserialized.size must_== limit
    }
  }

}
