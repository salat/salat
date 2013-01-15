/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         SuppressDefaultArgsSpec.scala
 * Last modified: 2012-10-15 20:40:58 EDT
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

import com.novus.salat._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import org.specs2.mutable._
import org.specs2.specification.Scope
import com.novus.salat.util.MapPrettyPrinter
import com.mongodb.casbah.commons.MongoDBList

class SuppressDefaultArgsSpec extends SalatSpec {

  "Suppressing default values" should {

    import suppress_default_args._
    ctx.suppressDefaultArgs must beTrue

    "suppress fields with default values from being serialized to output" in {
      val s = Susan.empty
      s.how must_== SuppressDefaults.HowDefault
      s.perished must_== SuppressDefaults.PerishedDefault
      s.fits must_== SuppressDefaults.FitsDefault
      s.about must_== SuppressDefaults.AboutDefault
      val dbo: MongoDBObject = grater[Susan].asDBObject(s)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint", "com.novus.salat.test.model.Susan")
      dbo.get("how") must beNone
      dbo.get("perished") must beNone
      dbo.get("fits") must beNone
      dbo.get("about") must beNone
      grater[Susan].asObject(dbo) must_== s
    }

    "don't suppress fields without default values from being serialized to output" in {
      val s = Susan(how = "why",
        perished = false,
        fits = List(Fit(1), Fit(2), Fit(3)),
        about = Map("a" -> "ants", "b" -> "bears"))
      s.how must_!= SuppressDefaults.HowDefault
      s.perished must_!= SuppressDefaults.PerishedDefault
      s.fits must_!= SuppressDefaults.FitsDefault
      s.about must_!= SuppressDefaults.AboutDefault
      val dbo: MongoDBObject = grater[Susan].asDBObject(s)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint", "com.novus.salat.test.model.Susan")
      dbo must havePair("how", "why")
      dbo must havePair("perished", false)
      dbo must havePair("about", MongoDBObject("a" -> "ants", "b" -> "bears"))
      dbo must havePair("fits", MongoDBList(
        MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Fit", "length" -> 1),
        MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Fit", "length" -> 2),
        MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Fit") // 3 is a default arg for Fit: suppressed
        ))
      grater[Susan].asObject(dbo) must_== s
    }
  }

}
