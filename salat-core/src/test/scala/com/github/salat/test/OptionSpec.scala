/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         OptionSpec.scala
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

class OptionSpec extends SalatSpec {
  "A grater" should {
    "support Option[Int] and Option[float]" in {
      val a = OptionSpecExample(timestamp = Some(1356048000000L), valueInt = Some(26), valueDouble = Some(1337.0), valueFloat = Some(31047.0f))
      val dbo: MongoDBObject = grater[OptionSpecExample].asDBObject(a)
      dbo must havePair("_typeHint" -> "com.github.salat.test.model.OptionSpecExample")
      dbo must havePair("timestamp" -> 1356048000000L)
      dbo must havePair("valueInt" -> 26)
      dbo must havePair("valueDouble" -> 1337.0)
      dbo must havePair("valueFloat" -> 31047.0f)

      grater[OptionSpecExample].asObject(dbo) must_== a

    }
    "support option not there" in {
      val a = OptionSpecExample(None, None)
      val dbo: MongoDBObject = grater[OptionSpecExample].asDBObject(a)
      dbo must havePair("_typeHint" -> "com.github.salat.test.model.OptionSpecExample")

      val json = dbo.toString()
      grater[OptionSpecExample].fromJSON(json) must_== a
    }

  }
}

