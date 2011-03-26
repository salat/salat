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

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class KeyAnnotationSpec extends SalatSpec {

  "The @Key annotation" should {

    "override a field name to persist with the given value" in {
      val j = James2("peach pits", true)
      val dbo: MongoDBObject = grater[James2].asDBObject(j)
      // the field name is "lye" but the @Key annotation specifies "cyanide"
      dbo must havePair("cyanide", "peach pits")
      dbo must havePair("byMistake", true)

      val j_* = grater[James2].asObject(dbo)
      j_* must_== j
    }

    "override a field name when used in a trait" in {
      val j = James3("old lace", false)
      val dbo: MongoDBObject = grater[James3].asDBObject(j)
      // the field name is "lye" but the @Key annotation specifies "arsenic"
      dbo must havePair("arsenic", "old lace")
      dbo must havePair("byMistake", false)

      val j_* = grater[James3].asObject(dbo)
      j_* must_== j
    }

    "override a field name when used in an abstract superclass" in {
      val j = James4("mad as a hatter", true)
      val dbo: MongoDBObject = grater[James4].asDBObject(j)
      // the field name is "lye" but the @Key annotation specifies "mercury"
      dbo must havePair("mercury", "mad as a hatter")
      dbo must havePair("byMistake", true)

      val j_* = grater[James4].asObject(dbo)
      j_* must_== j
    }
  }

}