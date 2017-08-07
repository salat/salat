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
      case _                         => false
    }
  }

}

case class TypicalCaseClass(name: String, value: Option[Double], attrs: Map[String, Any])

/** Replicate nested serialization / deserialization tests. */
case class NestedClass(name: String, myClass: MyClass)
case class NestedCaseClass(name: String, myClass: MyCaseClass)
case class DoubleNestedCaseClass(name: String, nested: NestedCaseClass)

/**
 * Issue #92 case classes should support custom transformers.
 * Continued as Issue #203
 * Original PR (#93) contributed by @edmondo1984
 */
abstract class SingleCustomGraterSpecification[T <: AnyRef: Manifest] extends Specification {

  def item: T // could be standard class or case class
  def transformer: CustomTransformer[T, DBObject]
  def context: Context

  implicit lazy val ctx = context
  context.registerCustomTransformer(transformer)

  s"A class ${manifest.runtimeClass} for which exists a custom transformation to DBObject" should {

    val classUnderTestGrater = grater[T]

    "Have a grater with a custom transformer" in {
      "capable of (de)serializing its target classes to/from dbObject" in {
        val dbObjectVersion = classUnderTestGrater.asDBObject(item)
        val deserializedMyInstance = classUnderTestGrater.asObject(dbObjectVersion)

        deserializedMyInstance must_== item
      }

      "capable of (de)serializing its target classes to/from json" in {
        val jsonVersion = classUnderTestGrater.toPrettyJSON(item)
        val deserializedJsonMyInstance = classUnderTestGrater.fromJSON(jsonVersion)

        deserializedJsonMyInstance must_== item
      }

      "capable of (de)serializing other case classes as normal to/from dbObject" in {
        val other = TypicalCaseClass("foo", Some(1.0d), Map("A" -> 1, "B" -> 2))
        val typicalGrater = grater[TypicalCaseClass]

        val dbObjectVersion = typicalGrater.asDBObject(other)
        val deserializedMyInstance = typicalGrater.asObject(dbObjectVersion)

        deserializedMyInstance must_== other
      }

      "capable of (de)serializing other classes as normal to/from json" in {
        val other = TypicalCaseClass("foo", Some(1.0d), Map("A" -> 1, "B" -> 2))
        val typicalGrater = grater[TypicalCaseClass]

        val jsonVersion = typicalGrater.toPrettyJSON(other)
        val deserializedJsonMyInstance = typicalGrater.fromJSON(jsonVersion)

        deserializedJsonMyInstance must_== other
      }

    }
  }

}

class CustomCaseClassTransformerSpec extends Specification {

  val customTransformer = new CustomTransformer[MyClass, DBObject] {

    def deserialize(b: DBObject): MyClass = {
      val betterApi = new MongoDBObject(b)
      val arr = betterApi.getAs[List[Double]]("values").getOrElse(sys.error("expected values field"))
      new MyClass(arr.toArray[Double])
    }

    def serialize(a: MyClass): DBObject = MongoDBObject("values" -> a.values.toList)

  }

  val caseClassTransformer = new CustomTransformer[MyCaseClass, DBObject] {

    def deserialize(b: DBObject): MyCaseClass = {
      val betterApi = new MongoDBObject(b)
      val arr = betterApi.getAs[List[Double]]("values").getOrElse(sys.error("expected values field"))
      MyCaseClass(arr.toArray[Double])
    }

    def serialize(a: MyCaseClass): DBObject = MongoDBObject("values" -> a.values.toList)

  }

  val myInstance = new MyClass(Array(1d, 2d))
  val myCaseClassInstance = MyCaseClass(Array(1d, 2d))

  include(new SingleCustomGraterSpecification[MyClass] {
    def item = myInstance
    def transformer = customTransformer
    lazy val context = new Context {
      val name = "MyCustomContext"
      override val typeHintStrategy = StringTypeHintStrategy(TypeHintFrequency.Always)
    }
  })

  include(new SingleCustomGraterSpecification[MyCaseClass] {
    def item = myCaseClassInstance
    def transformer = caseClassTransformer
    lazy val context = new Context {
      val name = "MyCustomCaseClassContext"
      override val typeHintStrategy = StringTypeHintStrategy(TypeHintFrequency.Always)
    }
  })

  "Grater with a custom transformer" should {
    implicit lazy val ctx = new Context {
      val name = "NestedClassesTestContext"
      override val typeHintStrategy = StringTypeHintStrategy(TypeHintFrequency.Always)
    }

    ctx.registerCustomTransformer(customTransformer)
    ctx.registerCustomTransformer(caseClassTransformer)

    "support DBObject serialization to/from case classes with nested standard classes that custom transform" in {
      val nested = NestedClass("test", new MyClass(Array(1.0, 2.0, 3.0)))
      val dbo = grater[NestedClass].asDBObject(nested)
      val backAgain = grater[NestedClass].asObject(dbo)

      nested must_== backAgain
    }

    "support json serialization to/from case classes with nested standard classes that custom transform" in {
      val nested = NestedClass("test", new MyClass(Array(1.0, 2.0, 3.0)))
      val json = grater[NestedClass].toPrettyJSON(nested)
      val backAgain = grater[NestedClass].fromJSON(json)

      nested must_== backAgain
    }

    "support DBObject serialization to/from case classes with nested case classes that custom transform" in {
      val nested = NestedCaseClass("test", MyCaseClass(Array(1.0, 2.0, 3.0)))
      val dbo = grater[NestedCaseClass].asDBObject(nested)
      val backAgain = grater[NestedCaseClass].asObject(dbo)

      nested must_== backAgain
    }

    "support json serialization to/from case classes with nested case classes that custom transform" in {
      val nested = NestedCaseClass("test", MyCaseClass(Array(1.0, 2.0, 3.0)))
      val json = grater[NestedCaseClass].toPrettyJSON(nested)
      val backAgain = grater[NestedCaseClass].fromJSON(json)

      nested must_== backAgain
    }

    "support DBObject serialization to/from case classes with even more deeply nested case classes that custom transform" in {
      val nested = DoubleNestedCaseClass("outer", NestedCaseClass("test", MyCaseClass(Array(1.0, 2.0, 3.0))))
      val dbo = grater[DoubleNestedCaseClass].asDBObject(nested)
      val backAgain = grater[DoubleNestedCaseClass].asObject(dbo)

      nested must_== backAgain
    }

    "support json serialization to/from case classes with even more deeply nested case classes that custom transform" in {
      val nested = DoubleNestedCaseClass("outer", NestedCaseClass("test", MyCaseClass(Array(1.0, 2.0, 3.0))))
      val json = grater[DoubleNestedCaseClass].toPrettyJSON(nested)
      val backAgain = grater[DoubleNestedCaseClass].fromJSON(json)

      nested must_== backAgain
    }

  }

}
