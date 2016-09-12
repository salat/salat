/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         DateTimeZoneSpec.scala
 * Last modified: 2016-07-10 23:42:23 EDT
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
import com.mongodb.util.JSON.parse
import salat._
import salat.test.global._
import salat.test.model._
import org.joda.time.DateTimeZone

class DateTimeZoneSpec extends SalatSpec {
  "A grater" should {
    "support org.scala_tools.time.TypeImports.DateTimeZone" in {
      val tz = DateTimeZone.forID("Europe/London")
      val n = Prue(zone = tz)
      val dbo: MongoDBObject = grater[Prue].asDBObject(n)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "salat.test.model.Prue")
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
      dbo must havePair("_typeHint" -> "salat.test.model.Prue")
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
