package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class DoubleSpec extends SalatSpec {
  "Salat" should {
    "deserialize an int to a double and an option double" in {
      val obj = grater[DoubleTest].asObject(DBObject("d" -> 9, "d2" -> 9))
      obj must_== DoubleTest(9d, Option(9d))
      obj.d must haveClass[java.lang.Double]
      obj.d2 must haveClass[Some[java.lang.Double]]
      obj.d2.map(_ * 2) must not(throwA[ClassCastException])
    }
  }
}
