package com.novus.salat

import com.mongodb.casbah.Imports._
import net.liftweb.json._

class ProxyGrater[X <: AnyRef](clazz: Class[X])(implicit ctx: Context) extends Grater[X](clazz)(ctx) {

  def asDBObject(o: X): DBObject =
    ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].asDBObject(o)

  def asObject[A <% MongoDBObject](dbo: A): X = {
    log.trace("ProxyGrater.asObject: typeHint='%s'".format(ctx.extractTypeHint(dbo).getOrElse("")))
    ctx.lookup(dbo).asInstanceOf[Grater[X]].asObject(dbo)
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] =
    ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].iterateOut(o)(f)

  //  def fromJSON(j: JObject) = error("### TODO: implement me!")

  def toJSON(o: X) = ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].toJSON(o)

  def toMap(o: X) = ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].toMap(o)

  def fromMap(m: Map[String, Any]) = ctx.lookup(m).asInstanceOf[Grater[X]].fromMap(m)
}
