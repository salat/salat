/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         FloatSpec.scala
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
package com.novus.salat.test

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._

class LongSpec extends SalatSpec {
  "A grater" should {
    "support longs" in {
      val a = LongSpecExample(timestamp = 1356048000000L, value = 26)
      val dbo: MongoDBObject = grater[LongSpecExample].asDBObject(a)
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.LongSpecExample")
      dbo must havePair("timestamp" -> 1356048000000L)
      dbo must havePair("value" -> 26)

      val json = dbo.toString()
      grater[LongSpecExample].fromJSON(json) must_== a
    }
  }
}
