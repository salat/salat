/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         BigDecimalPrecisionTest.scala
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

import java.math.{MathContext, RoundingMode}

import com.github.salat._
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._

import scala.math.{BigDecimal => ScalaBigDecimal}

class BigDecimalPrecisionTest extends SalatSpec {

  "Salat grater" should {
    "preserve ScalaBigDecimal precision to 16 places" in {

      val mc = new MathContext(16, RoundingMode.HALF_UP)
      val PrecisePi = ScalaBigDecimal("3.1415926535897932384626433832795028841971693993", mc)

      val MorePrecisePi = ScalaBigDecimal("3.1415926535897932384626433832795028841971693993", new MathContext(22, RoundingMode.HALF_UP))

      val g = George(number = PrecisePi, someNumber = Some(PrecisePi), noNumber = None)

      val dbo: MongoDBObject = grater[George].asDBObject(g)
      dbo must havePair("number" -> PrecisePi.toDouble)
      dbo must havePair("someNumber" -> PrecisePi.toDouble)
      dbo must not have key("noNumber")

      val coll = MongoConnection()(SalatSpecDb)("scala_math_big_decimal_precision_test")
      val wr = coll.insert(dbo)
      //      println("WR: %s".format(wr))

      val g_* = grater[George].asObject(coll.findOne().get)
      g_* must_== g
      g_*.number.precision must_== 16
      g_*.someNumber.get.precision must_== 16

      g_*.number must not be equalTo(MorePrecisePi)
      g_*.number must_== MorePrecisePi(mc)

    }

    "preserve scala.BigDecimal precision to 16 places" in {

      val mc = new MathContext(16, RoundingMode.HALF_UP)
      val PrecisePi = scala.BigDecimal("3.1415926535897932384626433832795028841971693993", mc)

      val MorePrecisePi = scala.BigDecimal("3.1415926535897932384626433832795028841971693993", new MathContext(22, RoundingMode.HALF_UP))

      val h = George2(number = PrecisePi, someNumber = Some(PrecisePi), noNumber = None)

      val dbo: MongoDBObject = grater[George2].asDBObject(h)
      dbo must havePair("number" -> PrecisePi.toDouble)
      dbo must havePair("someNumber" -> PrecisePi.toDouble)
      dbo must not have key("noNumber")

      val coll = MongoConnection()(SalatSpecDb)("scala_big_decimal_precision_test")
      val wr = coll.insert(dbo)
      //      println("WR: %s".format(wr))

      val h_* = grater[George2].asObject(coll.findOne().get)
      h_* must_== h
      h_*.number.precision must_== 16
      h_*.someNumber.get.precision must_== 16

      h_*.number must not be equalTo(MorePrecisePi)
      h_*.number must_== MorePrecisePi(mc)

    }
  }

  "properly deserialize any number type out of Mongo back into a BigDecimal" in {
    val mc = new MathContext(16, RoundingMode.HALF_UP)

    "whole number" in {
      val lake = BigDecimal(123.toString, mc)
      val i = Ida(Some(lake))

      val dbo: MongoDBObject = grater[Ida].asDBObject(i)
      dbo must havePair("lake" -> lake.toDouble)

      val coll = MongoConnection()(SalatSpecDb)("scala_math_big_decimal_precision_test_2")
      val wr = coll.insert(dbo)
      //       println("WR: %s".format(wr))

      val i_* = grater[Ida].asObject(coll.findOne().get)
      i_* must_== i

    }

    "decimal" in {
      val lake = BigDecimal((1.23).toString, mc)
      val i = Ida(Some(lake))

      val dbo: MongoDBObject = grater[Ida].asDBObject(i)
      dbo must havePair("lake" -> lake.toDouble)

      val coll = MongoConnection()(SalatSpecDb)("scala_math_big_decimal_precision_test_3")
      val wr = coll.insert(dbo)
      //       println("WR: %s".format(wr))

      val i_* = grater[Ida].asObject(coll.findOne().get)
      i_* must_== i
    }
  }

}
