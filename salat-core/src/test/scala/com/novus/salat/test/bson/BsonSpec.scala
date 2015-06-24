package com.novus.salat.test.bson

import com.novus.salat._
import com.novus.salat.test.SalatSpec
import com.novus.salat.test.global._

class BsonSpec extends SalatSpec {
  "Grater should handle BSON encoding round-trip" in {

    val person = Person(1, 2d, 3, "Foo", List("bar", "baz"))
    val people = People("El Dorado", person :: Nil)

    val encoded = grater[People].toBSON(people)
    val decoded = grater[People].fromBSON(encoded)

    decoded must_== people
  }
}
