package com.novus.salat.test


import com.novus.salat._
import annotations.raw.Salat
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class SalatTraitsCollectionsSpec extends SalatSpec {

  // TODO: create an issue for this?

  "a grater" should {
//    "properly serialize/deserialize a list of objects implementing a common trait" in {
//
//      val obj = SomeTopLevelClass(true,
//        SomeSecondLevelClass("b",
//          SomeContainerClass("1",
//            List(SomeSubclassExtendingSaidTrait(2),
//              AnotherSubclassExtendingSaidTrait(3.0),
//              SomeSubclassExtendingSaidTrait(4),
//              AnotherSubclassExtendingSaidTrait(5.0)))))
//
//
//      val dbo: MongoDBObject = grater[SomeTopLevelClass].asDBObject(obj)
//
//      dbo must havePair("a" -> obj.a)
//
//      val secondLevelDbo: MongoDBObject = dbo.expand[BasicDBObject]("b").get
//
//      secondLevelDbo must havePair("c" -> obj.b.c)
//
//      val containerDbo: MongoDBObject = secondLevelDbo.expand[BasicDBObject]("d").get
//      val containerObj: SomeContainerClass = obj.b.d
//
//      containerDbo must havePair("e" -> containerObj.e)
//
//      val listDbo: MongoDBList = containerDbo.expand[BasicDBList]("theListWhichNeedsToBeTested").get
//
//      val theList: List[SomeCommonTrait] = containerObj.theListWhichNeedsToBeTested
//      theList.size must_== listDbo.size
//
//      for( (nestedObj, i) <- theList.zipWithIndex ) {
//        val nestedDbo: MongoDBObject = listDbo(i).asInstanceOf[BasicDBObject]
//        nestedObj match {
//          case someSubclass: SomeSubclassExtendingSaidTrait => {
//            println("%s must equal %s".format(someSubclass.b, nestedDbo("b")))
//            someSubclass.b must_== nestedDbo("b")
//          }
//          case anotherSubClass: AnotherSubclassExtendingSaidTrait => {
//            anotherSubClass.c must_== nestedDbo("c")
//          }
//        }
//      }
//
//      // dbo has been verified to have been properly serialized, now, let's see if we can deserialize the serialized obj
//
//      val obj_* = grater[SomeTopLevelClass].asObject(dbo)
//
//      println(obj_*)
//      obj must_== obj_*
//
//    }

    "properly deserialize a raw DBObject" in {

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
        builder += "c" -> 3.0
        builder.result
      }
      //com.novus.salat.test.model.SomeSubclassExtendingSaidTrait
      //com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait

      val containerClassBuilder = MongoDBObject.newBuilder
      containerClassBuilder += "_typeHint" -> "com.novus.salat.test.model.SomeContainerClass"
      containerClassBuilder += "e" -> "some value for e"
      containerClassBuilder += "theListWhichNeedsToBeTested" -> listBuilder.result

      val dbo = containerClassBuilder.result

      println(dbo)

      val obj = grater[SomeContainerClass].asObject(dbo)

    } pendingUntilFixed
  }

}

