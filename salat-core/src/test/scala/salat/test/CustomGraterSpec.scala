package salat.test

import org.specs2.mutable.Specification
import salat.transformers.CustomTransformer
import com.mongodb.casbah.commons.MongoDBObject
import salat.Context
import salat._
import com.mongodb.casbah.Imports._

final class MyClass(val values: Array[Double]) {
  override def equals(other: Any): Boolean = {
    other match {
      case otherMyClass: MyClass => values.deep == otherMyClass.values.deep
      case _                     => false
    }
  }

}

case class MyCaseClass(values: Array[Double]) {
  override def equals(other: Any): Boolean = {
    other match {
      case otherMyClass: MyCaseClass => values.deep == otherMyClass.values.deep
      case _                      => false
    }
  }

}

case class TypicalCaseClass(name: String, value: Option[Double], attrs: Map[String, Any])

/**
  * Created with IntelliJ IDEA.
  *  Author: Edmondo Porcu
  *  Date: 17/07/13
  *  Time: 15:52
  *
  */
class SingleCustomGraterSpecification[T <: AnyRef](item: T, transformer: CustomTransformer[T, DBObject])(
  implicit context: Context, manifest: Manifest[T]) extends Specification {

  context.registerCustomTransformer(transformer)

  s"A class ${manifest.runtimeClass} for which exists a custom transformation to DBObject" should {

    val myGrater = grater[T]

    "Have a grater with a custom transformer" in {
      "capable of (de)serializing its target classes to dbObject" in {
        val dbObjectVersion = myGrater.asDBObject(item)
        val deserializedMyInstance = myGrater.asObject(dbObjectVersion)

        deserializedMyInstance must_== item
      }

      "capable of (de)serializing its target classes to json" in {
        val jsonVersion = myGrater.toPrettyJSON(item)
        val deserializedJsonMyInstance = myGrater.fromJSON(jsonVersion)

        deserializedJsonMyInstance must_== item
      }

      "capable of (de)serializing other case classes as normal to dbObject" in {
        val other = TypicalCaseClass("foo", Some(1.0d), Map("A" -> 1, "B" -> 2))
        val dbObjectVersion = myGrater.asDBObject()
        val deserializedMyInstance = myGrater.asObject(dbObjectVersion)

        deserializedMyInstance must_== other
      }

      "that transforms other case classes as normal to dbObject" in {
        val other = TypicalCaseClass("foo", Some(1.0d), Map("A" -> 1, "B" -> 2))

        val jsonVersion = myGrater.toPrettyJSON()
        val deserializedJsonMyInstance = myGrater.fromJSON(jsonVersion)

        deserializedJsonMyInstance must_== other
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

  val customTransformer2 = new CustomTransformer[MyCaseClass, DBObject] {

    def deserialize(b: DBObject): MyCaseClass = {
      val betterApi = new MongoDBObject(b)
      new MyCaseClass(betterApi.getAs[List[Double]]("values").get.toArray)
    }

    def serialize(a: MyCaseClass): DBObject = MongoDBObject("values" -> a.values.toList)

  }

  implicit val context = new Context {
    val name: String = "MyCustomContext"
  }

  val myInstance = new MyClass(Array(1d, 2d))
  val myCaseClassInstance = MyCaseClass(Array(1d, 2d))

  include(new SingleCustomGraterSpecification(myInstance, customTransformer))

  include(new SingleCustomGraterSpecification(myCaseClassInstance, customTransformer2))

}
