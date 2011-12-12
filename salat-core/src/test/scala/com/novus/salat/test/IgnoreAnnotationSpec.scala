package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import com.novus.salat.util.MapPrettyPrinter

class IgnoreAnnotationSpec extends SalatSpec {

  "The @Ignore annotation" should {
    "suppress serialization of the field while populating with the default arg during deserialization" in {
      val t = Titus(ignoreMe = "look", dontIgnoreMe = -999)
      val dbo: MongoDBObject = ctx.toDBObject(t)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Titus")
      dbo must havePair("dontIgnoreMe" -> -999)
      dbo.getAs[String]("ignoreMe") must beNone
      val t_* = ctx.fromDBObject[Titus](dbo)
      // because of @Ignore, the supplied value "look" is discarded on serialization 
      // on deserialization, ignoreMe field is populated with default value "bits"
      t_* must_== t.copy(ignoreMe = "bits")
      val ignoreMeDefaultValue = classOf[Titus].companionClass.getMethod("apply$default$1").invoke(classOf[Titus].companionObject)
      t_*.ignoreMe must_== ignoreMeDefaultValue
    }

    "allow a null default value" in {
      val t = Titus2(ignoreMe = "look", dontIgnoreMe = -999)
      val dbo: MongoDBObject = ctx.toDBObject(t)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Titus2")
      dbo must havePair("dontIgnoreMe" -> -999)
      dbo.getAs[String]("ignoreMe") must beNone
      val t_* = ctx.fromDBObject[Titus2](dbo)
      // because of @Ignore, the supplied value "look" is discarded on serialization
      // on deserialization, ignoreMe field is populated with default value null
      t_* must_== t.copy(ignoreMe = null)
      t_*.ignoreMe must_== classOf[Titus2].companionClass.getMethod("apply$default$1").invoke(classOf[Titus2].companionObject)
    }

    "ignore a field with an unsupported type annotated with @Ignore" in {
      val _id = new ObjectId
      val s = SomeClassWithUnsupportedField(id = _id, unsupportedType = new java.io.File("."))
      val dbo: MongoDBObject = ctx.toDBObject(s)
      //      println(MapPrettyPrinter(dbo))
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.SomeClassWithUnsupportedField")
      dbo must havePair("_id" -> _id)
      dbo.getAs[String]("text") must beNone
      dbo.getAs[java.io.File]("unsupportedType") must beNone
      val s_* = ctx.fromDBObject[SomeClassWithUnsupportedField](dbo)
      s_* must_== s.copy(unsupportedType = null)
      s_*.unsupportedType must_== classOf[SomeClassWithUnsupportedField].companionClass.getMethod("apply$default$3").
        invoke(classOf[SomeClassWithUnsupportedField].companionObject)
    }
  }

}