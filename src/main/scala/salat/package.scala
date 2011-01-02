package com.bumnetworks.salat

import java.math.{RoundingMode, MathContext}
import scala.collection.mutable.{Map => MMap, HashMap}
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.Imports._

trait Context extends Logging {
  private[salat] val graters: MMap[String, Grater[_ <: CaseClass]] = HashMap.empty

  val name: Option[String]
  val typeHint = TypeHint

  def accept(grater: Grater[_ <: CaseClass]): Unit =
    if (!graters.contains(grater.clazz.getName)) {
      graters += grater.clazz.getName -> grater
      log.info("Context(%s) accepted Grater[%s]", name.getOrElse("<no name>"), grater.clazz)
    }

  def lookup(clazz: String): Option[Grater[_ <: CaseClass]] = graters.get(clazz)

  def lookup(clazz: String, dbo: MongoDBObject): Option[Grater[_ <: CaseClass]] =
    lookup(dbo) match {
      case yes @ Some(grater) => yes
      case _ => lookup(clazz)
    }

  def lookup(dbo: MongoDBObject): Option[Grater[_ <: CaseClass]] =
    if (dbo.underlying.isInstanceOf[BasicDBObject]) dbo.get(typeHint) match {
      case Some(typeHint: String) => graters.get(typeHint)
      case _ => None
    } else throw new IllegalArgumentException(dbo.underlying.getClass.getName)
}

object `package` {
  type CasbahLogging = Logging
  type CaseClass = AnyRef with Product
  val TypeHint = "_typeHint"
}

package object global {
  implicit val ctx = new Context { val name = Some("global") }
  implicit val mathCtx = new MathContext(16, RoundingMode.HALF_UP)
}
