/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         json.scala
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
package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.json.{JSONConfig, StringDateStrategy}
import org.joda.time.DateTimeConstants._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

object `package` {

  val TestDateFormatter = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)

  val TestTypeHint = "_t"

  implicit val ctx = new Context {
    val name = "json-test-context"
    override val typeHintStrategy = StringTypeHintStrategy(
      when     = TypeHintFrequency.WhenNecessary,
      typeHint = TestTypeHint
    )
    override val jsonConfig = JSONConfig(dateStrategy = StringDateStrategy(dateFormatter = TestDateFormatter))
    override val bigIntStrategy = BigIntToLongStrategy
  }

  val testDate = new DateTime(2011, DECEMBER, 28, 14, 37, 56, 8, DateTimeZone.UTC)
  val testURL = new java.net.URL("http://www.typesafe.com")
}
