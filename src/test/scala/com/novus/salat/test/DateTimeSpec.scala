/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 *
 */
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.util._
import com.novus.salat.global._
import com.novus.salat.test.model._

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON.parse

import org.scala_tools.time.Imports._


class DateTimeSpec extends SalatSpec {
    "A grater" should {
    "support dates" in {
      val dt = DateTime.now
      val n = Neville(asOf = dt)
      val dbo: MongoDBObject = grater[Neville].asDBObject(n)
//      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint", "com.novus.salat.test.model.Neville")
      dbo must havePair("ennui" -> true)
      dbo must havePair("asOf", dt)

      val coll = MongoConnection()(SalatSpecDb)("scala_date_test_1")
      val wr = coll.insert(dbo)
      val n_* = grater[Neville].asObject(coll.findOne().get)
      n_* mustEqual n
    }
    "support dates parsed from JSON" in {
      val n = Neville(asOf = DateTime.now.withMillis(0L))
      val json = grater[Neville].asDBObject(n).toString
      log.info(json)
      val n_* = grater[Neville].asObject(parse(json).asInstanceOf[DBObject])
      n_* mustEqual n
    }
  }
}