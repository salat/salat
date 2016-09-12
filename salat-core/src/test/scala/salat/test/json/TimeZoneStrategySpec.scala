/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         TimeZoneStrategySpec.scala
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

package salat.test.json

import salat.json.StringTimeZoneStrategy
import salat.util.Logging
import org.joda.time.DateTimeZone
import org.json4s.JsonAST.{JInt, JString}
import org.specs2.mutable.Specification

class TimeZoneStrategySpec extends Specification with Logging {
  val z = DateTimeZone.forID("US/Eastern")

  "JSON date strategy" should {
    "string" in {
      val s = StringTimeZoneStrategy()
      val formatted = "America/New_York"
      val j = JString(formatted)

      "from DateTime to string" in {
        s.out(z) must_== j
      }
      "from string to DateTime" in {
        s.toDateTimeZone(j) must_== z
      }
      "throw an error when an unexpected date format is submitted" in {
        s.toDateTimeZone(JString("abc")) must throwA[IllegalArgumentException]
      }
      "throw an error when an unexpected JSON field type is submitted" in {
        s.toDateTimeZone(JInt(1)) must throwA[RuntimeException]
      }
    }
  }
}
