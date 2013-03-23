package com.novus.salat.test

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.test.custom._

class CustomTransformerSpec extends SalatSpec {

  "Salat" should {
    "allow custom transformers" in {
      val _id = new ObjectId
      val bar = Bar("b")
      val baz = Baz(1, 3.14)
      val foo = Foo(_id, bar, baz)
      val dbo = grater[Foo].asDBObject(foo)
      dbo should haveEntry("_t", foo.getClass.getName)
      dbo should haveEntry("bar", "b")
      dbo should haveEntry("baz._t", baz.getClass.getName)
      dbo should haveEntry("baz.a", 1)
      dbo should haveEntry("baz.b", 3.14)
      val foo_* = grater[Foo].asObject(dbo)
      foo_* must_== foo
    }
  }

}