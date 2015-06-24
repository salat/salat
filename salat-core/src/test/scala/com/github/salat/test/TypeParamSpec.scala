/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         TypeParamSpec.scala
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
import com.github.salat.test.model._
import com.mongodb
import com.mongodb.casbah.Imports._
import org.specs2.matcher.Matcher

//  @author akraievoy@gmail.com

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
