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
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class EnumSupportSpec extends SalatSpec {

  "a grater" should {
    import com.novus.salat.global._
    "work with Scala enums" in {
      "be able to serialize Scala enums" in {
        val me = Me("max")
        val g = grater[Me]
        val dbo: MongoDBObject = g.asDBObject(me)
        dbo("_typeHint") must_== classOf[Me].getName
        dbo("state") must_== Frakked.BeyondRepair.toString
      }

      "be able to deserialize Scala enums" in {
        val me = Me("max")
        val g = grater[Me]
        val dbo = g.asDBObject(me)
        val me_* = g.asObject(dbo)
        me must_== me_*
      }
    }
  }

  "a context" should {

    val h = Hector(thug = ThugLevel.Three, doneIn = DoneIn.OhDear)

    "provide a default enum handling strategy of toString" in {
      import com.novus.salat.global._

      ctx.defaultEnumStrategy mustEqual EnumStrategy.BY_VALUE

      val dbo: MongoDBObject = grater[Hector].asDBObject(h)
      dbo must havePair("thug" -> "Just a good boy who loves his mum")
      dbo must havePair("doneIn" -> "OhDear")

      val h_* = grater[Hector].asObject(dbo)
      h_* mustEqual h
    }

    "allow for context-level custom enum handling strategy" in {
      implicit val ctx = new Context {
        val name = Some("EnumSupportSpec-1")
        override val defaultEnumStrategy = EnumStrategy.BY_ID
      }

      ctx.defaultEnumStrategy mustEqual EnumStrategy.BY_ID

      val dbo: MongoDBObject = grater[Hector].asDBObject(h)
      dbo must havePair("thug" -> 2)
      dbo must havePair("doneIn" -> 3)

      val h_* = grater[Hector].asObject(dbo)
      h_* mustEqual h

    }


  }

}