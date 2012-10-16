/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         SortedSeqSpec.scala
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

import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class SortedSeqSpec extends SalatSpec {
  "a grater" should {
    "persist and retrieve sorted things" in {

      val arbitraryOrdering = new Ordering[String] {
        def compare(x: String, y: String) = if (x.length == y.length) x.compare(y) else x.length.compare(y.length)
      }

      val expectedOrder = Seq("kings", "ships", "shoes", "cabbages", "sealing wax")
      val walrus = Walrus(Seq("shoes", "ships", "sealing wax", "cabbages", "kings").sorted(arbitraryOrdering))

      walrus.manyThings must_== expectedOrder
      "a case class with a sorted list" in {
        val dbo: MongoDBObject = grater[Walrus[String]].asDBObject(walrus)
        dbo.get("manyThings") must beSome[AnyRef]

        val walrus_* = grater[Walrus[String]].asObject(dbo)
        walrus_*.manyThings must_== expectedOrder
      }

      "handle sorted sequences" in {
        val expectedOrder2 = Seq("is", "and", "hot", "sea", "the", "why", "boiling")
        val walrus2 = Walrus(Seq("and", "why", "the", "sea", "is", "boiling", "hot").sorted(arbitraryOrdering))
        walrus2.manyThings must_== expectedOrder2
        val expectedOrder3 = Seq("?", "and", "have", "pigs", "wings", "whether")
        val walrus3 = Walrus(Seq("and", "whether", "pigs", "have", "wings", "?").sorted(arbitraryOrdering))
        walrus3.manyThings must_== expectedOrder3

        val expectedHerd = Seq(walrus2, walrus3, walrus)
        val herd = Walrus(manyThings = Seq(walrus, walrus2, walrus3).sorted(new Ordering[Walrus[String]] {
          def compare(x: Walrus[String], y: Walrus[String]) = y.manyThings.length.compare(x.manyThings.length)
        }))
        herd.manyThings must_== expectedHerd

        val dbo: MongoDBObject = grater[Walrus[Walrus[String]]].asDBObject(herd)
        dbo.get("manyThings") must beSome[AnyRef]

        val herd_* = grater[Walrus[Walrus[String]]].asObject(dbo)
        herd_*.manyThings must_== expectedHerd
        herd_*.manyThings(0).manyThings must_== expectedOrder2
        herd_*.manyThings(1).manyThings must_== expectedOrder3
        herd_*.manyThings(2).manyThings must_== expectedOrder
      }
    }
  }
}