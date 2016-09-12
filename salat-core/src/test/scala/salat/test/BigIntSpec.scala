/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         BigIntSpec.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
package salat.test

import com.mongodb.casbah.Imports._
import salat._
import salat.test.global._
import salat.test.model._

class BigIntSpec extends SalatSpec {
  "A grater" should {
    "support BigInt" in {
      val swallowed = BigInt("1234567890")
      val tacks = BigInt(Long.MaxValue + 1L)
      val l = Leo(swallowed = Some(swallowed), tacks = tacks)
      val dbo: MongoDBObject = grater[Leo].asDBObject(l)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "salat.test.model.Leo")
      checkByteArrays(
        actual = dbo.expand[Array[Byte]]("swallowed").getOrElse(Array.empty[Byte]),
        swallowed.toByteArray
      )
      checkByteArrays(
        actual = dbo.expand[Array[Byte]]("tacks").getOrElse(Array.empty[Byte]),
        BigInt("-9223372036854775808").toByteArray
      )

      val coll = MongoConnection()(SalatSpecDb)("scala_big_int_test_1")
      val wr = coll.insert(dbo)
      //      println("WR: %s".format(wr))

      val l_* = grater[Leo].asObject(coll.findOne().get)
      //      println(MapPrettyPrinter(l_*))
      l_* must_== l
    }
  }
}
