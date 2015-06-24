package com.novus.salat.test.json

import com.novus.salat.json.StringTimeZoneStrategy
import com.novus.salat.util.Logging
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
