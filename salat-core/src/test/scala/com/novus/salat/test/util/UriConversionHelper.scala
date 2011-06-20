/**
* Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
* For questions and comments about this product, please see the project page at:
*
* http://github.com/novus/salat
*
*/
package com.novus.salat.test.util

import com.mongodb.casbah.commons.conversions.MongoConversionHelper
import org.bson.{BSON, Transformer}

// Want to write your own custon BSON encoding?  Look no further than this excellent example:
// https://github.com/mongodb/casbah/blob/master/casbah-commons/src/main/scala/conversions/ScalaConversions.scala

object RegisterURIConversionHelpers extends URIHelpers with com.mongodb.casbah.commons.Logging {
  def apply(): Unit = {
    log.info("Registering java.net.URI Scala Conversions.")
    super.register()
  }
}

object DeregisterURIConversionHelpers extends URIHelpers with com.mongodb.casbah.commons.Logging {
  def apply() = {
    log.debug("Unregistering java.net.URI Scala Conversions.")
    super.unregister()
  }
}

trait URIHelpers extends URISerializer with URIDeserializer

trait URISerializer extends MongoConversionHelper {

  private val encodeType = classOf[java.net.URI]
  /** Encoding hook for MongoDB To be able to persist java.net.URI to MongoDB */
  private val transformer = new Transformer {
    log.trace("Encoding java.net.URI.")

    /**
     * Return a String object which BSON can encode
     */
    def transform(o: AnyRef): AnyRef = o match {
      // the dumbest way to encode a URI that actually works for the purposes of my spec
      case uri: java.net.URI => "URI~%s".format(uri.toString)
      case _ => o
    }

  }

  override def register() = {
    log.debug("Hooking up java.net.URI serializer.") 
    /** Encoding hook for MongoDB To be able to persist java.net.URI to MongoDB */
    BSON.addEncodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    log.debug("De-registering java.net.URI serializer.") 
    BSON.removeEncodingHooks(encodeType)
    super.unregister()
  }
}

trait URIDeserializer extends MongoConversionHelper {

  private val encodeType = classOf[String]
  private val transformer = new Transformer with com.mongodb.casbah.commons.Logging {
    log.trace("Decoding java.net.URI")

    def transform(o: AnyRef): AnyRef = o match {
      // it's late at night, what do you want?  String casts the net pretty damned wide
      case s: String if s.startsWith("URI~") && s.split("~").size == 2 => {
        log.info("DECODING: %s", s)
        new java.net.URI(s.split("~")(1))
      }
      case _ => o
    }
  }

  override def register() = {
    log.debug("Hooking up java.net.URI deserializer")  
    /** Encoding hook for MongoDB To be able to read java.net.URI from MongoDB's BSON Date */
    BSON.addDecodingHook(encodeType, transformer)
    super.register()
  }

  override def unregister() = {
    log.debug("De-registering java.net.URI deserializer.")
    BSON.removeDecodingHooks(encodeType)
    super.unregister()
  }
}