/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         MapSupportSpec.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
package salat.test

import com.mongodb.casbah.Imports._
import salat._
import salat.test.global._
import salat.test.model.{AttributeObject, _}
import salat.util.MapPrettyPrinter
import org.bson.types.ObjectId

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
      dbo must havePair("_typeHint" -> "salat.test.model.AttributeObject")
      dbo must havePair("_id" -> 42L)
      dbo must havePair("key" -> "testKey1")
      dbo must havePair("bestDef" -> "bestDef1")
      dbo must havePair("urls" -> {
        val builder = MongoDBObject.newBuilder
        builder += "foo" -> {
          val u = MongoDBObject.newBuilder
          u += "_typeHint" -> "salat.test.model.UrlID"
          u += "dh" -> 1L
          u += "ph" -> 2L
          u.result
        }
        builder += "bar" -> {
          val u = MongoDBObject.newBuilder
          u += "_typeHint" -> "salat.test.model.UrlID"
          u += "dh" -> 3L
          u += "ph" -> 4L
          u.result
        }
        builder.result
      })

      val coll = MongoClient()(SalatSpecDb)("map_support_test_1")
      val wr = coll.insert(dbo)

      val ao_* = grater[AttributeObject].asObject(coll.findOne().get)
      ao_* must_== ao
    }

    "support objects that contains of types: Map[String, Any], List[Any] and Option[Any]" in {
      val parameter1 = Parameter("parameter1", Some(Map("map1" -> SimpleClass())), Map("key1" -> "value1", "key2" -> 2))
      val parameter2 = Parameter("parameter2", Some(List(parameter1)), Map("key1" -> "value1", "key2" -> 2))
      val parameter3 = Parameter("parameter3", Some(SimpleClass()), Map("key1" -> "value1", "key2" -> 2, "key3" -> parameter1))
      val parameters = List(parameter1, parameter2, parameter3)

      val data = Data("data", parameters)

      val metadata = MetaData("metadata", parameters, List(data))

      val id = new ObjectId().toString

      val view = ViewMetaData(id, metadata)

      val dbo: MongoDBObject = grater[ViewMetaData].asDBObject(view)
      log.info(MapPrettyPrinter(dbo))

      val coll = MongoClient()(SalatSpecDb)("map_support_test_2")
      val wr = coll.insert(dbo)

      val view_* = grater[ViewMetaData].asObject(coll.findOne().get)
      view_* must_== view
    }

  }

}
