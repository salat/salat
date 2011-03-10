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

class BasicCaseObjectSpec extends SalatSpec {
  "a grater" should {
    "make DBObject-s out of case class instances" in {
      "properly treat primitive values and optional values" in {
        val e = numbers
        val dbo: MongoDBObject = grater[Edward].asDBObject(e)

        log.info("before: %s", e)
        log.info("after : %s", dbo.asDBObject)

        dbo must havePair("a" -> e.a)
        dbo must not have key("aa")
        dbo must havePair("aaa" -> e.aaa.get)

        dbo must havePair("b" -> e.b)
        dbo must not have key("bb")
        dbo must havePair("bbb" -> e.bbb.get)

        dbo must havePair("c" -> e.c)
        dbo must not have key("cc")
        dbo must havePair("ccc" -> e.ccc.get)
      }

      "work with object graphs" in {
        val a = graph
        val dbo: MongoDBObject = grater[Alice].asDBObject(a)
        log.info("before: %s", a)
        log.info("after : %s", dbo.asDBObject)
        dbo must havePair("x" -> "x")
      }
    }

    "instantiate case class instances using data from DBObject-s" in {
      "cover primitive types" in {
        val e = numbers
        val e_* = grater[Edward].asObject(grater[Edward].asDBObject(e))
        e_* must_== e
      }

      "and silly object graphs" in {
        val a = graph
        val a_* = grater[Alice].asObject(grater[Alice].asDBObject(a))
        // these two checks are *very* naive, but it's hard to compare
        // unordered maps and expect them to come out equal.
        a_*.z.p must_== a.z.p
        a_*.z.q must_== a.z.q
      }

      "and also object graphs of even sillier shapes" in {
        val f = mucho_numbers()
        val dbo: MongoDBObject = grater[Fanny].asDBObject(f)
        dbo.get("complicated") must beSome[AnyRef]
        val f_* = grater[Fanny].asObject(dbo)
        f_* must_== f
      }
    }
  }

  "usage example for the README" should {
    val deflate_me = evil_empire

    "print out some sample JSON" in {
      val deflated = grater[Company].asDBObject(deflate_me)
      val inflated = grater[Company].asObject(deflated)
      inflated.copy(departments = Map.empty) must_== deflate_me.copy(departments = Map.empty)
      inflated.departments("MoK") must_== deflate_me.departments("MoK")
      inflated.departments("FOSS_Sabotage") must_== deflate_me.departments("FOSS_Sabotage")
    }
  }
}