/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         JodaDateTimeZoneHelpers.scala
 * Last modified: 2013-01-07 22:43:52 EST
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

package com.novus.salat.conversions

import com.mongodb.casbah.commons.conversions.MongoConversionHelper
import com.mongodb.casbah.commons.conversions.scala.JodaDateTimeHelpers
import org.bson.{BSON, Transformer}
import org.joda.time.DateTimeZone
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
