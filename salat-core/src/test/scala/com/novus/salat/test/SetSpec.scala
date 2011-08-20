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

class SetSpec extends SalatSpec {
  "a grater" should {
    "persist and retrieve sets" in {

      val expectedSet = Set("kings", "ships", "shoes", "cabbages", "sealing wax")
      val elephantSeal = ElephantSeal(Set("shoes", "ships", "sealing wax", "cabbages", "kings"))

      elephantSeal.distinctThings must_== expectedSet
      "a case class with a set" in {
        val dbo: MongoDBObject = grater[ElephantSeal[String]].asDBObject(elephantSeal)
        dbo.get("distinctThings") must beSome[AnyRef]

        val elephantSeal_* = grater[ElephantSeal[String]].asObject(dbo)
        elephantSeal_*.distinctThings must_== expectedSet
      }

      "handle sets" in {
        val expectedSet2 = Set("is", "and", "hot", "sea", "the", "why", "boiling")
        val elephantSeal2 = ElephantSeal(Set("and", "why", "the", "sea", "is", "boiling", "hot"))
        elephantSeal2.distinctThings must_== expectedSet2
        val expectedSet3 = Set("?", "and", "have", "pigs", "wings", "whether")
        val elephantSeal3 = ElephantSeal(Set("and", "whether", "pigs", "have", "wings", "?"))
        elephantSeal3.distinctThings must_== expectedSet3

        val expectedHerd = Set(elephantSeal2, elephantSeal3, elephantSeal)
        val herd = ElephantSeal(distinctThings = Set(elephantSeal, elephantSeal2, elephantSeal3))
        herd.distinctThings must_== expectedHerd

        val dbo: MongoDBObject = grater[ElephantSeal[ElephantSeal[String]]].asDBObject(herd)
        dbo.get("distinctThings") must beSome[AnyRef]

        val herd_* = grater[ElephantSeal[ElephantSeal[String]]].asObject(dbo)
        herd_*.distinctThings must_== expectedHerd
        // herd_*.distinctThings(0).distinctThings must_== expectedSet2
        // herd_*.distinctThings(1).distinctThings must_== expectedSet3
        // herd_*.distinctThings(2).distinctThings must_== expectedSet
      }
    }
  }
}