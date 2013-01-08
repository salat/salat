/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         IgnoreAnnotationSpec.scala
 * Last modified: 2013-01-07 22:41:58 EST
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

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import com.novus.salat.util.MapPrettyPrinter
import org.joda.time.DateTime

class IgnoreAnnotationSpec extends SalatSpec {

  "The @Ignore annotation" should {
    "suppress serialization of the field while populating with the default arg during deserialization" in {
      val t = Titus(ignoreMe = "look", dontIgnoreMe = -999)
      val dbo: MongoDBObject = grater[Titus].asDBObject(t)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Titus")
      dbo must havePair("dontIgnoreMe" -> -999)
      dbo.getAs[String]("ignoreMe") must beNone
      val t_* = grater[Titus].asObject(dbo)
      // because of @Ignore, the supplied value "look" is discarded on serialization 
      // on deserialization, ignoreMe field is populated with default value "bits"
      t_* must_== t.copy(ignoreMe = "bits")
      val ignoreMeDefaultValue = classOf[Titus].companionClass.getMethod("apply$default$1").invoke(classOf[Titus].companionObject)
      t_*.ignoreMe must_== ignoreMeDefaultValue
    }

    "allow a null default value" in {
      val t = Titus2(ignoreMe = "look", dontIgnoreMe = -999)
      val dbo: MongoDBObject = grater[Titus2].asDBObject(t)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Titus2")
      dbo must havePair("dontIgnoreMe" -> -999)
      dbo.getAs[String]("ignoreMe") must beNone
      val t_* = grater[Titus2].asObject(dbo)
      // because of @Ignore, the supplied value "look" is discarded on serialization
      // on deserialization, ignoreMe field is populated with default value null
      t_* must_== t.copy(ignoreMe = null)
      t_*.ignoreMe must_== classOf[Titus2].companionClass.getMethod("apply$default$1").invoke(classOf[Titus2].companionObject)
    }

    "ignore a field with an unsupported type annotated with @Ignore" in {
      val _id = new ObjectId
      val s = SomeClassWithUnsupportedField(id = _id, unsupportedType = new java.io.File("."))
      val dbo: MongoDBObject = grater[SomeClassWithUnsupportedField].asDBObject(s)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.SomeClassWithUnsupportedField")
      dbo must havePair("_id" -> _id)
      dbo.getAs[String]("text") must beNone
      dbo.getAs[java.io.File]("unsupportedType") must beNone
      val s_* = grater[SomeClassWithUnsupportedField].asObject(dbo)
      s_* must_== s.copy(unsupportedType = null)
      s_*.unsupportedType must_== classOf[SomeClassWithUnsupportedField].companionClass.getMethod("apply$default$3").
        invoke(classOf[SomeClassWithUnsupportedField].companionObject)
    }

    "ignore a field with an unsupported collection annotated with @Ignore" in {
      val _id = new ObjectId
      val now = DateTime.now
      val s = SomeClassWithUnsupportedField2(id = _id,
        email = "nobody@something.org",
        status = Borked,
        cascade = Map(1 -> Set(1, 2, 3)),
        thingy = Some(9),
        created = now,
        updated = now)
      val dbo: MongoDBObject = grater[SomeClassWithUnsupportedField2].asDBObject(s)
      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.SomeClassWithUnsupportedField2")
      dbo must havePair("_id" -> _id)
      dbo must havePair("email" -> "nobody@something.org")
      dbo must havePair("status" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Borked$"))
      dbo.getAs[Map[Int, Set[Int]]]("cascade") must beNone // serialization of cascade has been suppressed, as expected
      dbo must havePair("thingy" -> 9)
      dbo must havePair("created" -> now)
      dbo must havePair("updated" -> now)
      val s_* = grater[SomeClassWithUnsupportedField2].asObject(dbo)
      s_* must_== s.copy(cascade = Map.empty)
      s_*.cascade must_== classOf[SomeClassWithUnsupportedField2].companionClass.getMethod("apply$default$4").invoke(classOf[SomeClassWithUnsupportedField2].companionObject)
    }

  }

}