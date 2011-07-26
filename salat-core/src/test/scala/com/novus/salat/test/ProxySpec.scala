package com.novus.salat.test


import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.test.model._
import org.specs2.execute.{Success, PendingUntilFixed}

class ProxySpec extends SalatSpec with PendingUntilFixed {
  "a proxy grater" should {
    """delegate heavy lifting to correct "concrete" graters""" in {
      val i1 = SomeTraitImpl1("foo")
      val i2 = SomeTraitImpl2(5)

      val proxy = new ProxyGrater[SomeTrait](classOf[SomeTrait])

      val dbo1: MongoDBObject = proxy.asDBObject(i1)
      val dbo2: MongoDBObject = proxy.asDBObject(i2)

      log.trace("""
                dbo1 = %s
                dbo2 = %s
                """, dbo1, dbo2)

      dbo1 must havePair("_typeHint", classOf[SomeTraitImpl1].getName)
      dbo2 must havePair("_typeHint", classOf[SomeTraitImpl2].getName)

      success
    }
  }
}
