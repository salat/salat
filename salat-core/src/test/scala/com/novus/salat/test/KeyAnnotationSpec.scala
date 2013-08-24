/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         KeyAnnotationSpec.scala
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

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import org.joda.time.DateTime

class KeyAnnotationSpec extends SalatSpec {

  "The @Key annotation" should {

    "override a field name to persist with the given value" in {
      val j = James2("peach pits", true)
      val dbo: MongoDBObject = grater[James2].asDBObject(j)
      // the field name is "lye" but the @Key annotation specifies "cyanide"
      dbo must havePair("cyanide" -> "peach pits")
      dbo must havePair("byMistake" -> true)

      val j_* = grater[James2].asObject(dbo)
      j_* must_== j
    }

    "override a field name when used in a trait" in {
      val j = James3("old lace", false)
      val dbo: MongoDBObject = grater[James3].asDBObject(j)
      // the field name is "lye" but the @Key annotation specifies "arsenic"
      dbo must havePair("arsenic" -> "old lace")
      dbo must havePair("byMistake" -> false)

      val j_* = grater[James3].asObject(dbo)
      j_* must_== j
    }

    "override a field name when used in an abstract superclass" in {
      val j = James4("mad as a hatter", true)
      val dbo: MongoDBObject = grater[James4].asDBObject(j)
      // the field name is "lye" but the @Key annotation specifies "mercury"
      dbo must havePair("mercury" -> "mad as a hatter")
      dbo must havePair("byMistake" -> true)

      val j_* = grater[James4].asObject(dbo)
      j_* must_== j
    }

    "work with a field whose type is handled by a custom BSON encoding hook" in {
      com.novus.salat.test.util.RegisterURIConversionHelpers()
      val date = new DateTime(2011, 3, 26, 11, 45, 22, 5)
      val uri = new java.net.URI("http://slashdot.org")
      val p = Page(
        uri = uri,
        crawled = List(date),
        ads = None,
        title = Some("title"),
        description = Some("description"),
        keywords = Some("very clever minus two"))
      val dbo: MongoDBObject = grater[Page].asDBObject(p)
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Page")
      // @Key overrides field name "uri" with "_id"
      // the value will be serialized as a String using our custom java.net.URI -> String BSON encoding hook
      dbo must havePair("_id" -> uri)
      dbo must havePair("crawled" -> MongoDBList(date))
      dbo must havePair("title" -> "title")
      dbo must havePair("description" -> "description")
      dbo must havePair("keywords" -> "very clever minus two")

      val coll = MongoConnection()(SalatSpecDb)("key-annotation-spec-1")
      val wr = coll.insert(dbo)
      //      log.info("WR: %s", wr)

      val dbo_* : MongoDBObject = coll.findOne().get
      //      log.info(MapPrettyPrinter(dbo_*))
      val p_* = grater[Page].asObject(dbo_*)
      com.novus.salat.test.util.DeregisterURIConversionHelpers()
      p_* must_== p // our custom BSON encoding for URI is crunk e pur si muove
    }
  }

}