/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         MapSupportSpec.scala
 * Last modified: 2012-10-15 20:40:59 EDT
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
package com.novus.salat.test

import com.novus.salat.test.model.AttributeObject
import scala.collection.immutable.{ Map => IMap }
import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.MapPrettyPrinter
import org.bson.types.ObjectId

class MapSupportSpec extends SalatSpec {

  "a grater" should {

    "support objects that contain maps" in {

      val urls = IMap("foo" -> UrlID(dh = 1L, ph = 2L),
        "bar" -> UrlID(dh = 3L, ph = 4L))
      val ao = AttributeObject(_id = 42L, key = "testKey1", bestDef = "bestDef1", urls)

      val dbo: MongoDBObject = grater[AttributeObject].asDBObject(ao)
      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.AttributeObject")
      dbo must havePair("_id" -> 42L)
      dbo must havePair("key" -> "testKey1")
      dbo must havePair("bestDef" -> "bestDef1")
      dbo must havePair("urls" -> {
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
      wr.getCachedLastError must beNull

      val ao_* = grater[AttributeObject].asObject(coll.findOne().get)
      ao_* must_== ao
    }

    "support complex map" in {
      val parameter1 = Parameter("parameter1", Some(Map("key1" -> SimpleClass())), Map("key1" -> "value1", "key2" -> 2))
      val parameter2 = Parameter("parameter2", Some(List(parameter1)), Map("key1" -> "value1", "key2" -> 2))
      val parameter3 = Parameter("parameter3", Some(SimpleClass()), Map("key1" -> "value1", "key2" -> 2, "key3" -> parameter1))
      val parameters = List(parameter1, parameter2, parameter3)
      
      val data = Data("data", parameters)
      
      val metadata = MetaData("metadata", parameters, List(data))
      
      val id = new ObjectId().toString

      val view = ViewMetaData(id, metadata)

      val dbo: MongoDBObject = grater[ViewMetaData].asDBObject(view)
      log.info(MapPrettyPrinter(dbo))

      val coll = MongoConnection()(SalatSpecDb)("map_support_test_2")
      val wr = coll.insert(dbo)
//      log.info("WR: %s", wr)
      wr.getCachedLastError must beNull

      val view_* = grater[ViewMetaData].asObject(coll.findOne().get)
      view_* must_== view
    }

  }

}