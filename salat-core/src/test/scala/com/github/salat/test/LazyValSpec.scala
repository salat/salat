/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         LazyValSpec.scala
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
package com.github.salat.test

import com.github.salat._
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._

class LazyValSpec extends SalatSpec {

  "a grater" should {
    "work with case classes that have lazy values" in {
      val l = LazyThing(excuses = Seq(1, 2, 3))
      l.firstExcuse must beSome(1)
      l.lastExcuse must beSome(3)
      l.factorial must_== 6
      l.nthDegree must_== List(1, 7, 13, 19, 25, 31) // a lazy value that depends on factorial lazy value

      val dbo: MongoDBObject = grater[LazyThing].asDBObject(l)
      dbo.get("excuses") must beSome[AnyRef]
      dbo.get("firstExcuse") must beNone
      dbo.get("lastExcuse") must beNone
      dbo.get("factorial") must beNone
      dbo.get("nthDegree") must beNone

      val l_* = grater[LazyThing].asObject(dbo)
      l_*.excuses must_== Seq(1, 2, 3)
      l_*.firstExcuse must beSome(1)
      l_*.lastExcuse must beSome(3)
      l_*.factorial must_== 6
      l_*.nthDegree must_== List(1, 7, 13, 19, 25, 31)
    }
  }
}
