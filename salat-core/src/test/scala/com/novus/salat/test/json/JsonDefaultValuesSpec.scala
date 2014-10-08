package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.util._
import org.bson.types.ObjectId
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification

/** @see https://github.com/novus/salat/issues/130
 */
class JsonDefaultValuesSpec extends Specification with Logging with JsonMatchers {

  "JSON support with default arg suppression disabled (the default)" should {
    "evaluate default constructor argument when corresponding JSON property is missing" in {

      val deserializedObj1 = grater[Probe].fromJSON("{}")
      val deserializedObj2 = grater[Probe].fromJSON("{}")

      deserializedObj1.id should not be null
      deserializedObj1.id should not be deserializedObj2.id
    }
  }

  "JSON support with default arg suppression enabled" should {
    implicit val ctx = new Context {
      val name = "suppressDefault"
      override val suppressDefaultArgs = true
    }
    "serialize instances with multiple ObjectId fields that do not have default values" in {
      val father = Probe()
      val child = ChildProbe(id = new ObjectId, parent = father.id)
      val result = grater[ChildProbe].toCompactJSON(child)
      result must not(throwA[NoSuchMethodException]) and beAnInstanceOf[String]
    }
  }
}

case class Probe(id: ObjectId = new ObjectId())
case class ChildProbe(id: ObjectId, parent: ObjectId)
