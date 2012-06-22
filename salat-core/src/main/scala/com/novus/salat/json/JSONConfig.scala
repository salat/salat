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
import org.bson.types.ObjectId

object JSONConfig {
  val ISO8601 = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC)
}

case class JSONConfig(dateStrategy: JSONDateStrategy = StringDateStrategy(),
                      objectIdStrategy: JSONObjectIdStrategy = StrictJSONObjectIdStrategy,
                      outputNullValues: Boolean = false)

trait JSONObjectIdStrategy {
  def in(j: JValue): ObjectId

  def out(o: ObjectId): JValue

  protected def unexpectedInput(x: JValue) =
    sys.error("in: unexpected OID input class='%s', value='%s'".format(x.getClass.getName, x.values))
}

object StrictJSONObjectIdStrategy extends JSONObjectIdStrategy {

  def in(j: JValue) = j match {
    case JObject(JField("$oid", JString(oid)) :: Nil) => new ObjectId(oid)
    case x => unexpectedInput(x)
  }

  def out(o: ObjectId) = JObject(List(JField("$oid", JString(o.toString))))
}

object StringObjectIdStrategy extends JSONObjectIdStrategy {

  def in(j: JValue) = j match {
    case JString(oid) => new ObjectId(oid)
    case x            => unexpectedInput(x)
  }

  def out(o: ObjectId) = JString(o.toString)
}

trait JSONDateStrategy {
  def out(d: DateTime): JValue

  def out(d: Date): JValue

  def toDateTime(j: JValue): DateTime

  def toDate(j: JValue) = toDateTime(j).toDate
}

case class StringDateStrategy(dateFormatter: DateTimeFormatter = JSONConfig.ISO8601) extends JSONDateStrategy {
  def out(d: Date) = JString(dateFormatter.print(d.getTime))

  def out(d: DateTime) = JString(dateFormatter.print(d))

  def toDateTime(j: JValue) = j match {
    case JString(s) => dateFormatter.parseDateTime(s)
    case x          => sys.error("toDateTime: unsupported input type class='%s', value='%s'".format(x.getClass.getName, x.values))
  }
}

case class TimestampDateStrategy(zone: DateTimeZone = DateTimeZone.UTC) extends JSONDateStrategy {
  def out(d: Date) = JInt(d.getTime)

  def out(d: DateTime) = JInt(d.getMillis)

  def toDateTime(j: JValue) = j match {
    case JInt(v) => new DateTime(v.toLong, zone)
    case x       => sys.error("toDate: unsupported input type class='%s', value='%s'".format(x.getClass.getName, x.values))
  }
}

case class StrictJSONDateStrategy(zone: DateTimeZone = DateTimeZone.UTC) extends JSONDateStrategy {
  def out(d: Date) = JObject(JField("$date", JInt(d.getTime)) :: Nil)

  def out(d: DateTime) = JObject(JField("$date", JInt(d.getMillis)) :: Nil)

  def toDateTime(j: JValue) = j match {
    case JObject(JField(_, JInt(v)) :: Nil) => new DateTime(v.toLong, zone)
    case x                                  => sys.error("toDate: unsupported input type class='%s', value='%s'".format(x.getClass.getName, x.values))
  }
}

// or roll your own date strategy....  O the excitement.