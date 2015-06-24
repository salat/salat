/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         OptionSupportSpec.scala
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

import com.github.salat._
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._

class OptionSupportSpec extends SalatSpec {
  "a grater" should {
    "support Option[String]" in {
      "with value" in {
        val r = Rhoda(consumed = Some("flames"))
        val dbo: MongoDBObject = grater[Rhoda].asDBObject(r)
        dbo must havePair("_typeHint" -> "com.github.salat.test.model.Rhoda")
        dbo must havePair("consumed" -> "flames")

        val r_* = grater[Rhoda].asObject(dbo)
        r_*.consumed must beSome("flames")
        r_* must_== r
      }
      "with no value" in {
        val r = Rhoda(consumed = None)
        val dbo: MongoDBObject = grater[Rhoda].asDBObject(r)
        dbo must havePair("_typeHint" -> "com.github.salat.test.model.Rhoda")
        // TODO: what happened to must not haveKey

        val r_* = grater[Rhoda].asObject(dbo)
        r_*.consumed must beNone
        r_* must_== r
      }
    }
    "support Option[BigDecimal]" in {
      "with value" in {
        val temp = BigDecimal("451")
        val r = Rhoda2(howHot = Some(temp))
        val dbo: MongoDBObject = grater[Rhoda2].asDBObject(r)
        dbo must havePair("_typeHint" -> "com.github.salat.test.model.Rhoda2")
        dbo must havePair("howHot" -> 451.0)

        val r_* = grater[Rhoda2].asObject(dbo)
        r_*.howHot must beSome(temp)
        r_* must_== r
      }
      "with no value" in {
        val r = Rhoda2(howHot = None)
        val dbo: MongoDBObject = grater[Rhoda2].asDBObject(r)
        dbo must havePair("_typeHint" -> "com.github.salat.test.model.Rhoda2")
        // TODO: what happened to must not haveKey

        val r_* = grater[Rhoda2].asObject(dbo)
        r_*.howHot must beNone
        r_* must_== r
      }
    }
  }
}
