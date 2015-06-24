package com.github.salat.test.bson

import com.github.salat._
import com.github.salat.test.SalatSpec
import com.github.salat.test.global._

class BsonSpec extends SalatSpec {
  "Grater should handle BSON encoding round-trip" in {

    val person = Person(1, 2d, 3, "Foo", List("bar", "baz"))
    val people = People("El Dorado", person :: Nil)

    val encoded = grater[People].toBSON(people)
    val decoded = grater[People].fromBSON(encoded)

    decoded must_== people
  }
}
