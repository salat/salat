package com.novus.salat.test.custom

import com.mongodb.casbah.MongoConnection
import com.novus.salat.dao.SalatDAO
import com.novus.salat.test._
import com.novus.salat.transformers.CustomTransformer
import org.bson.types.ObjectId

case class Bar(x: String)

object BarTransformer extends CustomTransformer[Bar, String] {
  def deserialize(b: String) = Bar(b)

  def serialize(a: Bar) = a.x
}

case class Baz(a: Int, b: Double)

case class Foo(_id: ObjectId, bar: Bar, baz: Baz)

case class FooOptionBar(_id: ObjectId, bar: Option[Bar], baz: Baz)

case class FooListBar(_id: ObjectId, bar: List[Bar], baz: Baz)

case class FooMapBar(_id: ObjectId, bar: Map[String, Bar], baz: Baz)

object FooDAO extends SalatDAO[Foo, ObjectId](MongoConnection()(SalatSpecDb)(custom.ctx.name))