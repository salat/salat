/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         JSONConfig.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */

package com.novus.salat.json

import org.scala_tools.time.Imports._
import org.joda.time.DateTimeZone
import org.joda.time.format.{ DateTimeFormatter, ISODateTimeFormat }
import java.util.Date
import org.scala_tools.time.Imports
import net.liftweb.json.JsonAST._

object JSONConfig {
  val DefaultDateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC)
}

case class JSONConfig(dateStrategy: JSONDateStrategy = StringDateStrategy(),
                      outputNullValues: Boolean = false)

trait JSONDateStrategy {
  def out(d: DateTime): JValue
  def out(d: Date): JValue
}

case class StringDateStrategy(dateFormatter: DateTimeFormatter = JSONConfig.DefaultDateTimeFormatter) extends JSONDateStrategy {
  def out(d: Date) = JString(dateFormatter.print(d.getTime))

  def out(d: DateTime) = JString(dateFormatter.print(d))
}

object StrictJSONDateStrategy extends JSONDateStrategy {
  def out(d: Date) = JField("$date", JInt(d.getTime))

  def out(d: DateTime) = JField("$date", JInt(d.getMillis))
}

// or roll your own date strategy....  O the excitement.