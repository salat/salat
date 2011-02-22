package com.novus.salat.test


import com.novus.salat._
import com.novus.salat.util._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class SalatTraitSpec extends SalatSpec {

  "a grater" should {

    "handle a collection of concrete instances of a trait annotated with @Salat" in {
      val s1 = SomeSubclassExtendingSaidTrait(7)
      val s2 = AnotherSubclassExtendingSaidTrait(8.0)
      val container = SomeContainerClass("tally ho", theListWhichNeedsToBeTested = List[SomeCommonTrait](s1, s2))
      val dbo: MongoDBObject = grater[SomeContainerClass].asDBObject(container)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint", "com.novus.salat.test.model.SomeContainerClass")
      dbo must havePair("e", "tally ho")
      dbo must havePair("theListWhichNeedsToBeTested", {
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
      container_* mustEqual container
    }

    "handle a collection of concrete instances of an abstract class annotated with @Salat" in {
      val container = VertebrateList(vList = List[Vertebrate](
          Bird("Sammy Sparrow"),
          Bird("Oscar Ostrich", false),
          Squirrel("Joe"),
          Squirrel("Rocky", true)
        )
      )
//      println(MapPrettyPrinter(container))
      val dbo: MongoDBObject = grater[VertebrateList].asDBObject(container)
//      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint", "com.novus.salat.test.model.VertebrateList")
      dbo must havePair("vList", {
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
      container_* mustEqual container
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

      obj_* mustEqual obj
    }
  }

}

