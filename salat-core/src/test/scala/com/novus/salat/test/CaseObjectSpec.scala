package com.novus.salat.test

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.test.model.case_object_override._
import com.novus.salat.test.model.coo._

class CaseObjectSpec extends SalatSpec {
  "The context" should {

    "support custom case class serialization" in {
      "for a case object hierarchy" in {
        val bar = Thingy(Bar)
        val baz = Thingy(Baz)
        val qux = Thingy(Qux)
        val barDbo: MongoDBObject = grater[Thingy].asDBObject(bar)
        val bazDbo: MongoDBObject = grater[Thingy].asDBObject(baz)
        val quxDbo: MongoDBObject = grater[Thingy].asDBObject(qux)
        barDbo must havePair("foo", "B")
        bazDbo must havePair("foo", "Z")
        quxDbo must havePair("foo", MongoDBObject("_typeHint" -> "com.novus.salat.test.model.coo.Qux$"))
        grater[Thingy].asObject(barDbo) must_== bar
        grater[Thingy].asObject(bazDbo) must_== baz
        grater[Thingy].asObject(quxDbo) must_== qux
        // for backwards compatibility
        grater[Thingy].asObject(MongoDBObject("foo" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.coo.Bar$"))) must_== bar
      }
      "for Option containing case object" in {
        val bar = Thingy2(Option(Bar))
        val baz = Thingy2(Option(Baz))
        val qux = Thingy2(Option(Qux))
        val barDbo: MongoDBObject = grater[Thingy2].asDBObject(bar)
        val bazDbo: MongoDBObject = grater[Thingy2].asDBObject(baz)
        val quxDbo: MongoDBObject = grater[Thingy2].asDBObject(qux)
        barDbo must havePair("foo", "B")
        bazDbo must havePair("foo", "Z")
        quxDbo must havePair("foo", MongoDBObject("_typeHint" -> "com.novus.salat.test.model.coo.Qux$"))
        grater[Thingy2].asObject(barDbo) must_== bar
        grater[Thingy2].asObject(bazDbo) must_== baz
        grater[Thingy2].asObject(quxDbo) must_== qux
        // for backwards compatibility
        grater[Thingy2].asObject(MongoDBObject("foo" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.coo.Bar$"))) must_== bar
      }
      "for collection containing case objects" in {
        val t = Thingy3(Bar :: Baz :: Qux :: Nil)
        val dbo: MongoDBObject = grater[Thingy3].asDBObject(t)
        dbo must havePair("foo" -> MongoDBList("B", "Z", MongoDBObject("_typeHint" -> "com.novus.salat.test.model.coo.Qux$")))
      }
    }
  }
}
