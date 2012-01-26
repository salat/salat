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
package com.novus.salat.test.json

import com.novus.salat._
import org.joda.time.DateTimeConstants._
import com.novus.salat.json.{ StringDateStrategy, JSONConfig }
import org.joda.time.{ DateTimeZone, DateTime }
import org.joda.time.format.DateTimeFormat

object `package` {

  val TestDateFormatter = DateTimeFormat.forPattern("MM/dd/yyyy").withZone(DateTimeZone.forID("US/Eastern"))

  val TestTypeHint = "_t"

  implicit val ctx = new Context {
    val name = "json-test-context"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary,
      typeHint = TestTypeHint)
    override val jsonConfig = JSONConfig(dateStrategy = StringDateStrategy(dateFormatter = TestDateFormatter))
  }

  val testDate = new DateTime(2011, DECEMBER, 28, 14, 37, 56, 8)
  val testURL = new java.net.URL("http://www.typesafe.com")
}
