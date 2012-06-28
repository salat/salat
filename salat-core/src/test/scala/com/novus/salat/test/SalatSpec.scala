/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         SalatSpec.scala
 * Last modified: 2012-06-27 23:42:09 EDT
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
package com.novus.salat.test

import com.novus.salat.util.Logging
import com.mongodb.casbah.Imports._
import org.specs2.mutable._
import org.specs2.specification.{ Scope, Step }
import com.novus.salat.{ BigDecimalStrategy, Context }

trait SalatSpec extends Specification with Logging {

  override def is =
    Step {
      //      log.info("beforeSpec: registering BSON conversion helpers")
      com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
      com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

    } ^
      super.is ^
      Step {
        //        log.info("afterSpec: dropping test MongoDB '%s'".format(SalatSpecDb))
        MongoConnection().dropDatabase(SalatSpecDb)
      }

  // TODO: matchmaker, matchmaker, make me a MatchResult[Any]....
  def checkByteArrays(actual: Array[Byte], expected: Array[Byte]) = {
    actual.size must_== expected.size
    actual.zip(expected).foreach {
      v => v._1 must_== v._2
    }
    actual must not beEmpty // i feel so hollow.  maybe i should just return success and have done with it?
  }

  trait testContext extends Scope {
    implicit val ctx = new Context {
      val name = "textCtx_%s".format(System.currentTimeMillis())
    }
  }

  case class customBigDecimalCtx(strategy: BigDecimalStrategy) extends Scope {
    implicit val ctx = new Context {
      val name = "customBigDecimalCtx_%s".format(System.currentTimeMillis())
      override val bigDecimalStrategy = strategy
    }
    val x = BigDecimal("3.14", ctx.bigDecimalStrategy.mathCtx)
  }
}