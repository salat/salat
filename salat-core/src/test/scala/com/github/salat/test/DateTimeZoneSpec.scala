/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         DateTimeZoneSpec.scala
 * Last modified: 2013-01-07 23:00:06 EST
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
package com.github.salat.test

import com.github.salat._
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON.parse
import org.joda.time.DateTimeZone

class DateTimeZoneSpec extends SalatSpec {
  "A grater" should {
    "support org.scala_tools.time.TypeImports.DateTimeZone" in {
      val tz = DateTimeZone.forID("Europe/London")
      val n = Prue(zone = tz)
      val dbo: MongoDBObject = grater[Prue].asDBObject(n)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.github.salat.test.model.Prue")
      dbo must havePair("brawl" -> true)
      dbo must havePair("zone" -> tz.getID)

      val coll = MongoConnection()(SalatSpecDb)("scala_timezone_test_1")
      val wr = coll.insert(dbo)
      val n_* = grater[Prue].asObject(coll.findOne().get)
      n_* must_== n
    }

    "support org.joda.time.DateTimeZone" in {
      val tz = org.joda.time.DateTimeZone.forID("Europe/London")
      val n = Prue(zone = tz)
      val dbo: MongoDBObject = grater[Prue].asDBObject(n)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.github.salat.test.model.Prue")
      dbo must havePair("brawl" -> true)
      dbo must havePair("zone" -> tz.getID)

      val coll = MongoConnection()(SalatSpecDb)("scala_timezone_test_2")
      val wr = coll.insert(dbo)
      val n_* = grater[Prue].asObject(coll.findOne().get)
      n_* must_== n
    }

    "support timezones parsed from JSON" in {
      val n = Prue(zone = org.joda.time.DateTimeZone.forID("Europe/London"))
      val json = grater[Prue].asDBObject(n).toString
      //      log.info(json)
      val n_* = grater[Prue].asObject(parse(json).asInstanceOf[DBObject])
      n_* must_== n
    }

  }

}
