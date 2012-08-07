/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         DateTimeSpec.scala
 * Last modified: 2012-06-28 15:37:35 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON.parse
import org.bson.types.Code
import org.specs2.matcher.Matcher
import com.mongodb

class TypeParamSpec extends SalatSpec {
  "A grater" should {
    "support case classes with simple CharSequence field" in {
      val obj = new CC("a string here")
      val dbo: MongoDBObject = grater[CC].asDBObject(obj)

      //  LATER avoid matcher up-casting
      dbo.get("field") must beSome[String].asInstanceOf[Matcher[Option[AnyRef]]]
      dbo.get("field").asInstanceOf[Some[String]].get must_== "a string here"

      val objRe = grater[CC].asObject(dbo)
      objRe.field must_== "a string here"
    }

    "support case classes with type params and minor generalizations" in {
      val obj = new CCwithTypePar(new CCwithTypeParNest("a string here"))
      val dbo: MongoDBObject = grater[CCwithTypePar[_ <: CharSequence]].asDBObject(obj)

      dbo.get("typeParamField") must beSome[mongodb.BasicDBObject].asInstanceOf[Matcher[Option[AnyRef]]]
      val nestedDbo: MongoDBObject =
        dbo.get("typeParamField").asInstanceOf[Some[mongodb.BasicDBObject]].get
      nestedDbo.get("typeParamField").asInstanceOf[Some[String]].get must_== "a string here"

      val objRe = grater[CCwithTypePar[CharSequence]].asObject(dbo)
      objRe.typeParamField.typeParamField must_== "a string here"
    }

    "support case classes with covariant type params" in {
      val obj = new CCwithCovarTP(new CCwithCovarTPNest("a string here"))
      val dbo: MongoDBObject = grater[CCwithCovarTP[_ <: CharSequence]].asDBObject(obj)

      dbo.get("typeParamField") must beSome[mongodb.BasicDBObject].asInstanceOf[Matcher[Option[AnyRef]]]
      val nestedDbo: MongoDBObject =
        dbo.get("typeParamField").asInstanceOf[Some[mongodb.BasicDBObject]].get
      nestedDbo.get("typeParamField").asInstanceOf[Some[String]].get must_== "a string here"

      val objRe = grater[CCwithCovarTP[CharSequence]].asObject(dbo)
      objRe.typeParamField.typeParamField must_== "a string here"
    }

    "support case classes with covariant type params and existentially typed fields" in {
      val obj = new CCwithCTPAndExistentialField(new CCwithCTPAndExistentialFieldNest("a string here"))
      val dbo: MongoDBObject = grater[CCwithCTPAndExistentialField[_ <: CharSequence]].asDBObject(obj)

      dbo.get("typeParamField") must beSome[mongodb.BasicDBObject].asInstanceOf[Matcher[Option[AnyRef]]]
      val nestedDbo: MongoDBObject =
        dbo.get("typeParamField").asInstanceOf[Some[mongodb.BasicDBObject]].get
      nestedDbo.get("typeParamField").asInstanceOf[Some[String]].get must_== "a string here"

      val objRe = grater[CCwithCTPAndExistentialField[CharSequence]].asObject(dbo)
      objRe.typeParamField.typeParamField must_== "a string here"
    }

    //  TODO a test for possible JSON-centric usecases
  }

}
