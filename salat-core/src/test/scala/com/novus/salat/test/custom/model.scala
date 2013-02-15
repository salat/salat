package com.novus.salat.test.custom

import com.novus.salat.transformers.CustomTransformer
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.MongoConnection
import com.novus.salat.test._

case class Bar(x: String)

object BarTransformer extends CustomTransformer[Bar, String] {
  def deserialize(b: String) = Bar(b)

  def serialize(a: Bar) = a.x
}

case class Baz(a: Int, b: Double)

case class Foo(_id: ObjectId, bar: Bar, baz: Baz)

object FooDAO extends SalatDAO[Foo, ObjectId](MongoConnection()(SalatSpecDb)(custom.ctx.name))