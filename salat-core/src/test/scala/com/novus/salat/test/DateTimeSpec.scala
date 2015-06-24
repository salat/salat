/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         DateTimeSpec.scala
 * Last modified: 2013-01-07 22:47:46 EST
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
import com.mongodb.util.JSON.parse
import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import org.joda.time.{DateTime, LocalDateTime}

class DateTimeSpec extends SalatSpec {
  "A grater" should {
    "support org.scala_tools.time.TypeImports.DateTime" in {
      val dt = DateTime.now
      val n = Neville(asOf = dt)
      val dbo: MongoDBObject = grater[Neville].asDBObject(n)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Neville")
      dbo must havePair("ennui" -> true)
      dbo must havePair("asOf" -> dt)

      val coll = MongoConnection()(SalatSpecDb)("scala_date_test_1")
      val wr = coll.insert(dbo)
      val n_* = grater[Neville].asObject(coll.findOne().get)
      n_* must_== n
    }

    "support org.scala_tools.time.TypeImports.LocalDateTime" in {
      val ldt = LocalDateTime.now
      val v = Victor(departureTime = ldt)
      val dbo: MongoDBObject = grater[Victor].asDBObject(v)

      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Victor")
      dbo must havePair("departureTime" -> ldt)

      val coll = MongoConnection()(SalatSpecDb)("scala_date_test_2")
      val wr = coll.insert(dbo)
      val v_* = grater[Victor].asObject(coll.findOne().get)
      v_* must_== v
    }

    "support org.joda.time.DateTime" in {
      val dt = new org.joda.time.DateTime()
      val n = Neville(asOf = dt)
      val dbo: MongoDBObject = grater[Neville].asDBObject(n)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Neville")
      dbo must havePair("ennui" -> true)
      dbo must havePair("asOf" -> dt)

      val coll = MongoConnection()(SalatSpecDb)("scala_date_test_3")
      val wr = coll.insert(dbo)
      val n_* = grater[Neville].asObject(coll.findOne().get)
      n_* must_== n
    }

    "support org.joda.time.LocalDateTime" in {
      val ldt = new org.joda.time.LocalDateTime()
      val v = Victor(departureTime = ldt)
      val dbo: MongoDBObject = grater[Victor].asDBObject(v)

      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Victor")
      dbo must havePair("departureTime" -> ldt)

      val coll = MongoConnection()(SalatSpecDb)("scala_date_test_4")
      val wr = coll.insert(dbo)
      val v_* = grater[Victor].asObject(coll.findOne().get)
      v_* must_== v
    }

    "support dates parsed from JSON" in {
      val n = Neville(asOf = new org.joda.time.DateMidnight().toDateTime)
      val json = grater[Neville].asDBObject(n).toString
      //      log.info(json)
      val n_* = grater[Neville].asObject(parse(json).asInstanceOf[DBObject])
      n_* must_== n
    }

  }
}
