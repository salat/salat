/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class BigIntSpec extends SalatSpec {
  "A grater" should {
    "support BigInt" in {
      val swallowed = BigInt("1234567890")
      val tacks = BigInt(Long.MaxValue + 1L)
      val l = Leo(swallowed = Some(swallowed), tacks = tacks)
      val dbo: MongoDBObject = ctx.toDBObject(l)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Leo")
      checkByteArrays(actual = dbo.expand[Array[Byte]]("swallowed").getOrElse(Array.empty[Byte]),
        swallowed.toByteArray)
      checkByteArrays(actual = dbo.expand[Array[Byte]]("tacks").getOrElse(Array.empty[Byte]),
        BigInt("-9223372036854775808").toByteArray)

      val coll = MongoConnection()(SalatSpecDb)("scala_big_int_test_1")
      val wr = coll.insert(dbo)
      //      println("WR: %s".format(wr))

      val l_* = grater[Leo].asObject(coll.findOne().get)
      //      println(MapPrettyPrinter(l_*))
      l_* must_== l
    }
  }
}