package com.novus.salat.test

import org.specs2.mutable.Specification
import com.novus.salat.transformers.CustomTransformer
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.Context
import com.novus.salat._
import com.mongodb.casbah.Imports._
import scala.reflect.ClassTag

/** Created with IntelliJ IDEA.
 *  Author: Edmondo Porcu
 *  Date: 17/07/13
 *  Time: 15:52
 *
 */
final class MyClass(val values: Array[Double]) {
  override def equals(other: Any): Boolean = {
    other match {
      case otherMyClass: MyClass => values.deep == otherMyClass.values.deep
      case _                     => false
    }
  }

}

case class MyClass2(values: Array[Double]) {
  override def equals(other: Any): Boolean = {
    other match {
      case otherMyClass: MyClass2 => values.deep == otherMyClass.values.deep
      case _                      => false
    }
  }

}

class SingleCustomGraterSpecification[T <: AnyRef](item: T, transformer: CustomTransformer[T, DBObject])(implicit context: Context, manifest: Manifest[T]) extends Specification {
  context registerCustomTransformer transformer

  s"A class ${manifest.runtimeClass.toString} for which exist a custom transformation to DBObject" should {

    val myGrater = grater[T]
    "Have a special grater " in {

      "Capable of  (de)serializing to dbObject" in {
        val dbObjectVersion = myGrater asDBObject item
        val deserializedMyInstance = myGrater asObject dbObjectVersion
        deserializedMyInstance must_== item
      }
      "Capable of  (de)serializing to json" in {
        val jsonVersion = myGrater toPrettyJSON item
        val deserializedJsonMyInstance = myGrater fromJSON jsonVersion
        deserializedJsonMyInstance must_== item
      }
    }

  }

}

class CustomGraterSpec extends Specification {

  val customTransformer = new CustomTransformer[MyClass, DBObject] {

    def deserialize(b: DBObject): MyClass = {
      val betterApi = new MongoDBObject(b)
      new MyClass(betterApi.getAs[List[Double]]("values").get.toArray)
    }

    def serialize(a: MyClass): DBObject = MongoDBObject("values" -> a.values.toList)

  }

  val customTransformer2 = new CustomTransformer[MyClass2, DBObject] {

    def deserialize(b: DBObject): MyClass2 = {
      val betterApi = new MongoDBObject(b)
      new MyClass2(betterApi.getAs[List[Double]]("values").get.toArray)
    }

    def serialize(a: MyClass2): DBObject = MongoDBObject("values" -> a.values.toList)

  }

  implicit val context = new Context {
    val name: String = "MYTextContext"

  }
  val myInstance = new MyClass(Array(1d, 2d))
  val myInstance2 = MyClass2(Array(1d, 2d))

  include(new SingleCustomGraterSpecification(myInstance, customTransformer))

  include(new SingleCustomGraterSpecification(myInstance2, customTransformer2))

}
