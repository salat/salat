package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.util._
import org.bson.types.ObjectId
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification

/** @see https://github.com/novus/salat/issues/130
 */
class JsonDefaultValuesSpec extends Specification with Logging with JsonMatchers {

  "JSON support" should {
    "evaluate default constructor argument when corresponding JSON property is missing" in {

      val deserializedObj1 = grater[Probe].fromJSON("{}")
      val deserializedObj2 = grater[Probe].fromJSON("{}")

      deserializedObj1.id should not be null
      deserializedObj1.id should not be deserializedObj2.id
    }
  }
}

case class Probe(id: ObjectId = new ObjectId())
