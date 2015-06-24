/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         CaseObjectSpec.scala
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
import com.github.salat.test.model.case_object_override._
import com.github.salat.test.model.coo._
import com.mongodb.casbah.Imports._

class CaseObjectSpec extends SalatSpec {
  "The context" should {

    "support custom case class serialization" in {
      "for a case object hierarchy" in {
        val bar = Thingy(Bar)
        val baz = Thingy(Baz)
        val qux = Thingy(Qux)
        val barDbo: MongoDBObject = grater[Thingy].asDBObject(bar)
        val bazDbo: MongoDBObject = grater[Thingy].asDBObject(baz)
        val quxDbo: MongoDBObject = grater[Thingy].asDBObject(qux)
        barDbo must havePair("foo" -> "B")
        bazDbo must havePair("foo" -> "Z")
        quxDbo must havePair("foo" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.coo.Qux$"))
        grater[Thingy].asObject(barDbo) must_== bar
        grater[Thingy].asObject(bazDbo) must_== baz
        grater[Thingy].asObject(quxDbo) must_== qux
        // for backwards compatibility
        grater[Thingy].asObject(MongoDBObject("foo" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.coo.Bar$"))) must_== bar
      }
      "for Option containing case object" in {
        val bar = Thingy2(Option(Bar))
        val baz = Thingy2(Option(Baz))
        val qux = Thingy2(Option(Qux))
        val barDbo: MongoDBObject = grater[Thingy2].asDBObject(bar)
        val bazDbo: MongoDBObject = grater[Thingy2].asDBObject(baz)
        val quxDbo: MongoDBObject = grater[Thingy2].asDBObject(qux)
        barDbo must havePair("foo" -> "B")
        bazDbo must havePair("foo" -> "Z")
        quxDbo must havePair("foo" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.coo.Qux$"))
        grater[Thingy2].asObject(barDbo) must_== bar
        grater[Thingy2].asObject(bazDbo) must_== baz
        grater[Thingy2].asObject(quxDbo) must_== qux
        // for backwards compatibility
        grater[Thingy2].asObject(MongoDBObject("foo" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.coo.Bar$"))) must_== bar
      }
      "for collection containing case objects" in {
        val t = Thingy3(Bar :: Baz :: Qux :: Nil)
        val dbo: MongoDBObject = grater[Thingy3].asDBObject(t)
        dbo must havePair("foo" -> MongoDBList("B", "Z", MongoDBObject("_typeHint" -> "com.github.salat.test.model.coo.Qux$")))
      }
    }
  }
}
