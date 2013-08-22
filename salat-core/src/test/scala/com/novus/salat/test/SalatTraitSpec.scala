/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         SalatTraitSpec.scala
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
import com.novus.salat.test.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.test.model._

class SalatTraitSpec extends SalatSpec {

  "a grater" should {

    "handle a collection of concrete instances of a trait annotated with @Salat" in {
      val s1 = SomeSubclassExtendingSaidTrait(7)
      val s2 = AnotherSubclassExtendingSaidTrait(8.0)
      val container = SomeContainerClass("tally ho", theListWhichNeedsToBeTested = List[SomeCommonTrait](s1, s2))
      val dbo: MongoDBObject = grater[SomeContainerClass].asDBObject(container)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.SomeContainerClass")
      dbo must havePair("e" -> "tally ho")
      dbo must havePair("theListWhichNeedsToBeTested" -> {
        val listBuilder = MongoDBList.newBuilder
        listBuilder += {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait"
          builder += "b" -> 7
          builder.result
        }
        listBuilder += {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait"
          builder += "d" -> 8.0
          builder.result
        }
        listBuilder.result
      })

      val container_* = grater[SomeContainerClass].asObject(dbo)
      container_* must_== container
    }

    "handle a collection of concrete instances of an abstract class annotated with @Salat" in {
      val container = VertebrateList(vList = List[Vertebrate](
        Bird("Sammy Sparrow"),
        Bird("Oscar Ostrich", false),
        Squirrel("Joe"),
        Squirrel("Rocky", true)))
      //      println(MapPrettyPrinter(container))
      val dbo: MongoDBObject = grater[VertebrateList].asDBObject(container)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.VertebrateList")
      dbo must havePair("vList" -> {
        val listBuilder = MongoDBList.newBuilder
        listBuilder += {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.Bird"
          builder += "name" -> "Sammy Sparrow"
          builder += "canFly" -> true
          builder.result
        }
        listBuilder += {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.Bird"
          builder += "name" -> "Oscar Ostrich"
          builder += "canFly" -> false
          builder.result
        }
        listBuilder += {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.Squirrel"
          builder += "name" -> "Joe"
          builder += "canFly" -> false
          builder.result
        }
        listBuilder += {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.Squirrel"
          builder += "name" -> "Rocky"
          builder += "canFly" -> true
          builder.result
        }
        listBuilder.result
      })

      val container_* = grater[VertebrateList].asObject(dbo)
      container_* must_== container
    }

    "properly deserialize a raw DBObject containing concrete instances of a trait annotated with @Salat" in {
      val listBuilder = MongoDBList.newBuilder

      listBuilder += {
        val builder = MongoDBObject.newBuilder
        builder += "_typeHint" -> "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait"
        builder += "b" -> 2
        builder.result
      }

      listBuilder += {
        val builder = MongoDBObject.newBuilder
        builder += "_typeHint" -> "com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait"
        builder += "d" -> 3.0
        builder.result
      }

      val containerClassBuilder = MongoDBObject.newBuilder
      containerClassBuilder += "_typeHint" -> "com.novus.salat.test.model.SomeContainerClass"
      containerClassBuilder += "e" -> "some value for e"
      containerClassBuilder += "theListWhichNeedsToBeTested" -> listBuilder.result

      val dbo = containerClassBuilder.result

      val obj_* = grater[SomeContainerClass].asObject(dbo)

      val obj = SomeContainerClass("some value for e",
        List(SomeSubclassExtendingSaidTrait(2),
          AnotherSubclassExtendingSaidTrait(3.0)))

      obj_* must_== obj
    }
  }

  "handle a value typed to a top-level trait" in {
    // the "someTrait" field is typed to SomeTrait, which is annotated with @Salat
    val container1 = ContainsFieldTypedToTrait(someTrait = SomeTraitImpl1(x = "Hello"))
    val dbo1: MongoDBObject = grater[ContainsFieldTypedToTrait].asDBObject(container1)
    dbo1 must havePair("_typeHint" -> "com.novus.salat.test.model.ContainsFieldTypedToTrait")
    dbo1 must havePair("someTrait" -> {
      // _typeHint shows that @Salat annotation on SomeTrait is working
      MongoDBObject("_typeHint" -> "com.novus.salat.test.model.SomeTraitImpl1",
        "x" -> "Hello")
    })
    grater[ContainsFieldTypedToTrait].asObject(dbo1) must_== container1

    val container2 = ContainsFieldTypedToTrait(someTrait = SomeTraitImpl2(y = 33))
    val dbo2: MongoDBObject = grater[ContainsFieldTypedToTrait].asDBObject(container2)
    dbo2 must havePair("_typeHint" -> "com.novus.salat.test.model.ContainsFieldTypedToTrait")
    dbo2 must havePair("someTrait" -> {
      // _typeHint shows that @Salat annotation on SomeTrait is working
      MongoDBObject("_typeHint" -> "com.novus.salat.test.model.SomeTraitImpl2",
        "y" -> 33)
    })
    grater[ContainsFieldTypedToTrait].asObject(dbo2) must_== container2
  }

  "handle multiple levels of @Salat annotation" in {
    val investments = Investments(contracts = List[Contract](
      Stock(name = "Apple", ticker = "AAPL"),
      Turbo(name = "Knock out", ticker = "ASX"),
      Index(name = "FTSE 100")))
    val dbo: MongoDBObject = grater[Investments].asDBObject(investments)
    dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Investments")
    dbo must havePair("contracts" -> {
      val builder = MongoDBList.newBuilder
      builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Stock", "name" -> "Apple", "ticker" -> "AAPL")
      builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Turbo", "name" -> "Knock out", "ticker" -> "ASX")
      builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Index", "name" -> "FTSE 100")
      builder.result()
    })

    grater[Investments].asObject(dbo) must_== investments
  }

}

