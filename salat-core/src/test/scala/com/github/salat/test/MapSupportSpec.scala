/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         MapSupportSpec.scala
 * Last modified: 2015-06-23 20:52:14 EDT
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
package com.github.salat.test

import com.github.salat._
import com.github.salat.test.global._
import com.github.salat.test.model.{AttributeObject, _}
import com.github.salat.util.MapPrettyPrinter
import com.mongodb.casbah.Imports._

import scala.collection.immutable.{Map => IMap}

class MapSupportSpec extends SalatSpec {

  "a grater" should {

    "support objects that contain maps" in {

      val urls = IMap(
        "foo" -> UrlID(dh = 1L, ph = 2L),
        "bar" -> UrlID(dh = 3L, ph = 4L)
      )
      val ao = AttributeObject(_id = 42L, key = "testKey1", bestDef = "bestDef1", urls)

      val dbo: MongoDBObject = grater[AttributeObject].asDBObject(ao)
      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.github.salat.test.model.AttributeObject")
      dbo must havePair("_id" -> 42L)
      dbo must havePair("key" -> "testKey1")
      dbo must havePair("bestDef" -> "bestDef1")
      dbo must havePair("urls" -> {
        val builder = MongoDBObject.newBuilder
        builder += "foo" -> {
          val u = MongoDBObject.newBuilder
          u += "_typeHint" -> "com.github.salat.test.model.UrlID"
          u += "dh" -> 1L
          u += "ph" -> 2L
          u.result
        }
        builder += "bar" -> {
          val u = MongoDBObject.newBuilder
          u += "_typeHint" -> "com.github.salat.test.model.UrlID"
          u += "dh" -> 3L
          u += "ph" -> 4L
          u.result
        }
        builder.result
      })

      val coll = MongoConnection()(SalatSpecDb)("map_support_test_1")
      val wr = coll.insert(dbo)
      //      log.info("WR: %s", wr)
      wr.getCachedLastError must beNull

      val ao_* = grater[AttributeObject].asObject(coll.findOne().get)
      ao_* must_== ao
    }

  }

}
