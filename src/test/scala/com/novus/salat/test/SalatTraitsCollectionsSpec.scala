package com.novus.salat.test


import com.novus.salat._
import annotations.raw.Salat
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class SalatTraitsCollectionsSpec extends SalatSpec {

  "a grater" should {
    "properly deserialize a raw DBObject" in {

    // TODO: create an issue for this?
    // if you comment this block out, the second block fails with
    // no grater found for 'com.novus.salat.test.model.SomeCommonTrait' OR 'com.novus.salat.test.model.SomeSubclassExtendingSaidTrait' (Injectors.scala:166)
      {
        val obj = SomeContainerClass("1",
          List(SomeSubclassExtendingSaidTrait(2),
            AnotherSubclassExtendingSaidTrait(3.0),
            SomeSubclassExtendingSaidTrait(4),
            AnotherSubclassExtendingSaidTrait(5.0)))

        val dbo: MongoDBObject = grater[SomeContainerClass].asDBObject(obj)

        val obj_* = grater[SomeContainerClass].asObject(dbo)

        obj must_== obj_*

      }


      {

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

        val obj = grater[SomeContainerClass].asObject(dbo)

      }

    }

  }

}

