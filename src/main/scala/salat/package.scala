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

  protected def generate(clazz: String): Grater[_ <: CaseClass] =
    { new Grater[CaseClass](Class.forName(clazz).asInstanceOf[Class[CaseClass]])(this) {} }.asInstanceOf[Grater[CaseClass]]

  def lookup(clazz: String): Option[Grater[_ <: CaseClass]] = graters.get(clazz)

  def lookup_!(clazz: String): Grater[_ <: CaseClass] =
    lookup(clazz).getOrElse(generate(clazz))

  def lookup_![X <: CaseClass : Manifest]: Grater[X] =
    lookup_!(manifest[X].erasure.getName).asInstanceOf[Grater[X]]

  protected def extractTypeHint(dbo: MongoDBObject): Option[String] =
    if (dbo.underlying.isInstanceOf[BasicDBObject]) dbo.get(typeHint) match {
      case Some(hint: String) => Some(hint)
      case _ => None
    } else None

  def lookup(x: CaseClass): Option[Grater[_ <: CaseClass]] = lookup(x.getClass.getName)

  def lookup(clazz: String, x: CaseClass): Option[Grater[_ <: CaseClass]] =
    lookup(clazz) match {
      case yes @ Some(grater) => yes
      case _ => lookup(x)
    }

  def lookup(clazz: String, dbo: MongoDBObject): Option[Grater[_ <: CaseClass]] =
    lookup(dbo) match {
      case yes @ Some(grater) => yes
      case _ => lookup(clazz)
    }

  def lookup(dbo: MongoDBObject): Option[Grater[_ <: CaseClass]] =
    extractTypeHint(dbo) match {
      case Some(hint: String) => graters.get(hint)
      case _ => None
    }

  def lookup_!(dbo: MongoDBObject): Grater[_ <: CaseClass] =
    generate(extractTypeHint(dbo).getOrElse(throw new Exception("no type hint found")))
}

object `package` {
  type CasbahLogging = Logging
  type CaseClass = AnyRef with Product
  val TypeHint = "_typeHint"

  def timeAndLog[T](f: => T)(l: Long => Unit): T = {
    val t = System.currentTimeMillis
    val r = f
    l.apply(System.currentTimeMillis - t)
    r
  }
}

package object global {
  implicit val ctx = new Context { val name = Some("global") }
  implicit val mathCtx = new MathContext(16, RoundingMode.HALF_UP)
}
