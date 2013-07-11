package com.novus.salat.test.custom

import com.mongodb.DBObject
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.Implicits._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.custom.Bicycle
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

class Wibble(val a: String, val b: Int) {
  override def equals(obj: Any) = obj match {
    case w: Wibble => a == w.a && b == w.b
    case _         => super.equals(obj)
  }

  override def hashCode() = 31 * a.hashCode + b
}

object WibbleTransformer extends CustomTransformer[Wibble, DBObject] {
  def deserialize(b: DBObject) = {
    new Wibble(b.getAsOrElse[String]("a", ""), b.getAsOrElse[Int]("b", -99))
  }

  def serialize(a: Wibble) = MongoDBObject("a" -> a.a, "b" -> a.b)
}

object BicycleTransformer extends CustomTransformer[Bicycle, DBObject] {
  def deserialize(b: DBObject) = {
    val cadence = b.getAsOrElse[Int]("cadence", 0)
    val speed = b.getAsOrElse[Int]("speed", 0)
    val gear = b.getAsOrElse[Int]("gear", 0)
    new Bicycle(cadence, speed, gear)
  }

  def serialize(a: Bicycle) = MongoDBObject("cadence" -> a.getCadence, "speed" -> a.getSpeed, "gear" -> a.getGear)
}