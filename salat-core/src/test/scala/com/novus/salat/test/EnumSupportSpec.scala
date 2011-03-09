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
import com.novus.salat.annotations._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class EnumSupportSpec extends SalatSpec {

  "a grater" should {
    import com.novus.salat.test.global._
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

    "allow an individual enum annotated with EnumAs to override default enum handling strategy" in {

      import com.novus.salat.global._

      ctx.defaultEnumStrategy mustEqual EnumStrategy.BY_VALUE
      DoneInById.getClass.getAnnotation(classOf[EnumAs]).asInstanceOf[EnumAs].strategy mustEqual EnumStrategy.BY_ID

      val h1 = HectorOverrideId(thug = ThugLevel.Two, doneInById = DoneInById.PiningForTheFjords)

      val dbo: MongoDBObject = grater[HectorOverrideId].asDBObject(h1)
      dbo must havePair("thug" -> "Honour student")
      dbo must havePair("doneInById" -> 1)

      val h1_* = grater[HectorOverrideId].asObject(dbo)
      h1_* mustEqual h1

    }

    "allow an individual enum annotated with EnumAs to override custom enum handling strategy" in {

      implicit val ctx = new Context {
        val name = Some("EnumSupportSpec-4")
        override val defaultEnumStrategy = EnumStrategy.BY_ID
      }

      ctx.defaultEnumStrategy mustEqual EnumStrategy.BY_ID
      DoneInByValue.getClass.getAnnotation(classOf[EnumAs]).asInstanceOf[EnumAs].strategy mustEqual EnumStrategy.BY_VALUE

      val h1 = HectorOverrideValue(thug = ThugLevel.Two, doneInByValue = DoneInByValue.PiningForTheFjords)

      val dbo: MongoDBObject = grater[HectorOverrideValue].asDBObject(h1)
      dbo must havePair("thug" -> 1)
      dbo must havePair("doneInByValue" -> "PiningForTheFjords")

      val h1_* = grater[HectorOverrideValue].asObject(dbo)
      h1_* mustEqual h1

    }


  }

}