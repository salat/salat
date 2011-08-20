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
package com.novus.salat.test

import com.novus.salat.test.model.AttributeObject
import scala.collection.immutable.{ Map => IMap }
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.MapPrettyPrinter

class MapSupportSpec extends SalatSpec {

  "a grater" should {

    "support objects that contain maps" in {

      val urls = IMap("foo" -> UrlID(dh = 1L, ph = 2L),
        "bar" -> UrlID(dh = 3L, ph = 4L))
      val ao = AttributeObject(_id = 42L, key = "testKey1", bestDef = "bestDef1", urls)

      val dbo: MongoDBObject = grater[AttributeObject].asDBObject(ao)
      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint", "com.novus.salat.test.model.AttributeObject")
      dbo must havePair("_id", 42L)
      dbo must havePair("key", "testKey1")
      dbo must havePair("bestDef", "bestDef1")
      dbo must havePair("urls", {
        val builder = MongoDBObject.newBuilder
        builder += "foo" -> {
          val u = MongoDBObject.newBuilder
          u += "_typeHint" -> "com.novus.salat.test.model.UrlID"
          u += "dh" -> 1L
          u += "ph" -> 2L
          u.result
        }
        builder += "bar" -> {
          val u = MongoDBObject.newBuilder
          u += "_typeHint" -> "com.novus.salat.test.model.UrlID"
          u += "dh" -> 3L
          u += "ph" -> 4L
          u.result
        }
        builder.result
      })

      val coll = MongoConnection()(SalatSpecDb)("map_support_test_1")
      val wr = coll.insert(dbo)
      //      log.info("WR: %s", wr)
      wr.getLastError.getErrorMessage must beNull

      val ao_* = grater[AttributeObject].asObject(coll.findOne().get)
      ao_* must_== ao
    }

  }

}