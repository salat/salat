/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         SalatSpec.scala
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
package com.github.salat.test

import com.github.salat.conversions.RegisterJodaTimeZoneConversionHelpers
import com.github.salat.{BigDecimalStrategy, Context}
import com.mongodb.casbah.Imports._
import org.specs2.specification.{Scope, Step}

trait SalatSpec extends org.specs2.mutable.Specification with com.mongodb.casbah.commons.test.CasbahSpecificationBase {

  override def is =
    Step {
      //      log.info("beforeSpec: registering BSON conversion helpers")
      com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
      com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

      RegisterJodaTimeZoneConversionHelpers()

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
