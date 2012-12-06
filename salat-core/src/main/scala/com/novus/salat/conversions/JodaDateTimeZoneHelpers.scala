package com.novus.salat.conversions

import com.mongodb.casbah.commons.conversions
import conversions.{ MongoConversionHelper, scala }

import org.bson.{ BSON, Transformer }

import org.scala_tools.time.Imports._
import scala.JodaDateTimeHelpers
import org.joda.time.tz.CachedDateTimeZone

object RegisterJodaTimeZoneConversionHelpers extends JodaDateTimeZoneHelpers {
  def apply() = {
    log.debug("Registering  Joda Time Scala Conversions.")
    super.register()
  }
}

object DeregisterJodaTimeZoneConversionHelpers extends JodaDateTimeHelpers {
  def apply() = {
    log.debug("Unregistering Joda Time Scala Conversions.")
    super.unregister()
  }
}

trait JodaDateTimeZoneHelpers extends JodaDateTimeZoneSerializer

trait JodaDateTimeZoneSerializer extends MongoConversionHelper {

  private val encodeType = classOf[DateTimeZone]
  private val encodeTypeCached = classOf[CachedDateTimeZone]
  /** Encoding hook for MongoDB To be able to persist Joda DateTimeZone to MongoDB */
  private val transformer = new Transformer {
    log.trace("Encoding a Joda DateTimeZone.")

    def transform(o: AnyRef): AnyRef = o match {
      case tz: DateTimeZone => tz.getID
      case _                => o
    }
  }

  override def register() = {
    log.debug("Hooking up Joda DateTimeZone serializer.")
    /** Encoding hook for MongoDB To be able to persist Joda DateTimeZone to MongoDB */
    BSON.addEncodingHook(encodeType, transformer)
    BSON.addEncodingHook(encodeTypeCached, transformer)
    super.register()
  }

  override def unregister() = {
    log.debug("De-registering Joda DateTimeZone serializer.")
    BSON.removeEncodingHooks(encodeType)
    BSON.removeEncodingHooks(encodeTypeCached)
    super.unregister()
  }
}
