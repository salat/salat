/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         DateStrategySpec.scala
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

package com.novus.salat.test.json

import org.specs2.mutable.Specification
import com.novus.salat.util.Logging
import org.joda.time.{DateTimeZone, DateTime}
import com.novus.salat.json.{StrictJSONDateStrategy, TimestampDateStrategy, StringDateStrategy}
import org.joda.time.format.ISODateTimeFormat
import org.json4s.JsonAST._

class DateStrategySpec extends Specification with Logging {

  val z = DateTimeZone.forID("US/Eastern")
  val dt = new DateTime(2011, 12, 28, 14, 37, 56, 8, z)
  val d = dt.toDate

  "JSON date strategy" should {
    "string" in {
      val s = StringDateStrategy(dateFormatter = ISODateTimeFormat.dateTimeNoMillis().withZone(z))
      val formatted = "2011-12-28T14:37:56-05:00"
      val j = JString(formatted)
      val dtNoMillis = new DateTime(2011, 12, 28, 14, 37, 56, 0, z)
      "from DateTime to string" in {
        s.out(dt) must_== j
      }
      "from java.util.Date to string" in {
        s.out(d) must_== j
      }
      "from string to DateTime" in {
        s.toDateTime(j) must_== dtNoMillis
      }
      "from string to java.util.Date" in {
        s.toDate(j) must_== dtNoMillis.toDate
      }
      "throw an error when an unexpected date format is submitted" in {
        s.toDateTime(JString("2011/12/28")) must throwA[IllegalArgumentException]
      }
      "throw an error when an unexpected JSON field type is submitted" in {
        s.toDateTime(JInt(1)) must throwA[RuntimeException]
      }
    }
    "Timestamp" in {
      val s = TimestampDateStrategy(z)
      val j = JInt(1325101076008L)
      "from DateTime to timestamp" in {
        s.out(dt) must_== j
      }
      "from java.util.Date to timestamp" in {
        s.out(d) must_== j
      }
      "from timestamp to DateTime" in {
        s.toDateTime(j) must_== dt
      }
      "from timestamp to java.util.Date" in {
        s.toDate(j) must_== d
      }
      "throw an error when an unexpected JSON field type is submitted" in {
        s.toDateTime(JString("2011/12/28")) must throwA[RuntimeException]
      }
    }
    "Strict JSON" in {
      val s = StrictJSONDateStrategy(z)
      val j = JObject(JField("$date", JInt(1325101076008L)) :: Nil)
      "from DateTime to timestamp" in {
        s.out(dt) must_== j
      }
      "from java.util.Date to timestamp" in {
        s.out(d) must_== j
      }
      "from timestamp to DateTime" in {
        s.toDateTime(j) must_== dt
      }
      "from timestamp to java.util.Date" in {
        s.toDate(j) must_== d
      }
      "throw an error when an unexpected JSON field type is submitted" in {
        s.toDateTime(JString("2011/12/28")) must throwA[RuntimeException]
      }
    }

  }
}