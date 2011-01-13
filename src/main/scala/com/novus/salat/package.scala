package com.novus.salat

import scala.collection.mutable.{Map => MMap, HashMap}
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.Imports._

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import java.math.{BigInteger, RoundingMode, MathContext}


object `package` {

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

  implicit def shortenOID(oid: ObjectId) = new {
    def asShortString = (new BigInteger(oid.toString, 16)).toString(36)
  }

  implicit def explodeOID(oid: String) = new {
    def asObjectId = new ObjectId((new BigInteger(oid, 36)).toString(16))
  }

  implicit def class2companion(clazz: Class[_]) = new {
    def companionClass: Class[_] =
      Class.forName(if (clazz.getName.endsWith("$")) clazz.getName else "%s$".format(clazz.getName))
    def companionObject = companionClass.getField("MODULE$").get(null)
  }
}