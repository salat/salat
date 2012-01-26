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