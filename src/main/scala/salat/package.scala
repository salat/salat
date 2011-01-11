package com.bumnetworks.salat

import java.math.{RoundingMode, MathContext}
import scala.collection.mutable.{Map => MMap, HashMap}
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.Imports._

import com.bumnetworks.salat.annotations.raw._
import com.bumnetworks.salat.annotations.util._

trait Context extends Logging {
  private[salat] val graters: MMap[String, Grater[_ <: CaseClass]] = HashMap.empty

  val name: Option[String]
  val typeHint: Option[String] = Some(TypeHint)

  def accept(grater: Grater[_ <: CaseClass]): Unit =
    if (!graters.contains(grater.clazz.getName)) {
      graters += grater.clazz.getName -> grater
      log.trace("Context(%s) accepted Grater[%s]", name.getOrElse("<no name>"), grater.clazz)
    }

  // XXX: This check needs to be a little bit less naive. There are
  // other types (Joda Time, anyone?) that are either directly
  // interoperable with MongoDB, or are handled by Casbah's BSON
  // encoders.
  protected def suitable_?(clazz: String): Boolean =
     !(clazz.startsWith("scala.") || clazz.startsWith("java.") || clazz.startsWith("javax.")) || getClassNamed(clazz).map(_.annotated_?[Salat]).getOrElse(false)

  protected def suitable_?(clazz: Class[_]): Boolean = suitable_?(clazz.getName)

  protected def generate_?(c: String): Option[Grater[_ <: CaseClass]] =
    if (suitable_?(c)) {
      getCaseClass(c) match {
        case Some(clazz) =>
          if (clazz.isInterface) None
          else Some({ new Grater[CaseClass](clazz)(this) {} }.asInstanceOf[Grater[CaseClass]])
        case _ => None
      }
    } else None

  protected def generate(clazz: String): Grater[_ <: CaseClass] =
    { new Grater[CaseClass](getCaseClass(clazz).map(_.asInstanceOf[Class[CaseClass]]).get)(this) {} }.asInstanceOf[Grater[CaseClass]]

  def lookup(clazz: String): Option[Grater[_ <: CaseClass]] = graters.get(clazz) match {
    case yes @ Some(_) => yes
    case _ => generate_?(clazz)
  }

  def lookup_!(clazz: String): Grater[_ <: CaseClass] =
    lookup(clazz).getOrElse(generate(clazz))

  def lookup_![X <: CaseClass : Manifest]: Grater[X] =
    lookup_!(manifest[X].erasure.getName).asInstanceOf[Grater[X]]

  protected def extractTypeHint(dbo: MongoDBObject): Option[String] =
    if (dbo.underlying.isInstanceOf[BasicDBObject]) dbo.get(typeHint.getOrElse(TypeHint)) match {
      case Some(hint: String) => Some(hint)
      case _ => None
    } else None

  def lookup(x: CaseClass): Option[Grater[_ <: CaseClass]] = lookup(x.getClass.getName)

  def lookup(clazz: String, x: CaseClass): Option[Grater[_ <: CaseClass]] =
    lookup(clazz) match {
      case yes @ Some(grater) => yes
      case _ => lookup(x)
    }

  def lookup_!(clazz: String, x: CaseClass): Grater[_ <: CaseClass] =
    lookup(clazz, x).getOrElse(generate(x.getClass.getName))

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

  def grater[X <: CaseClass](implicit ctx: Context, m: Manifest[X]): Grater[X] = ctx.lookup_![X](m)

  protected[salat] def getClassNamed(c: String): Option[Class[_]] = {
    try { Some(Class.forName(c)) }
    catch { case _ => None }
  }

  protected[salat] def getCaseClass(c: String): Option[Class[CaseClass]] =
    getClassNamed(c).map(_.asInstanceOf[Class[CaseClass]])

  import java.math.BigInteger

  implicit def shortenOID(oid: ObjectId) = new {
    def asShortString = (new BigInteger(oid.toString, 16)).toString(36)
  }

  implicit def explodeOID(oid: String) = new {
    def asObjectId = new ObjectId((new BigInteger(oid, 36)).toString(16))
  }

  implicit def class2companion(clazz: Class[_]) = new {
    def companionClass: Class[_] = Class.forName("%s$".format(clazz.getName))
    def companionObject = companionClass.getField("MODULE$").get(null)
  }
}

package object global {
  implicit val ctx = new Context { val name = Some("global") }
  val NoTypeHints = new Context {
    val name = Some("global-no-type-hints")
    override val typeHint = None
  }
  implicit val mathCtx = new MathContext(16, RoundingMode.HALF_UP)
}
