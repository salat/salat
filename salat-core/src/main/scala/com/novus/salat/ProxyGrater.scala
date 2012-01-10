package com.novus.salat

import com.mongodb.casbah.Imports._

class ProxyGrater[X <: AnyRef](clazz: Class[X])(implicit ctx: Context) extends Grater[X](clazz)(ctx) {

  def asDBObject(o: X): DBObject =
    ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].asDBObject(o)

  def asObject[A <% MongoDBObject](dbo: A): X = {
    log.trace("asObject: typeHint='%s'".format(ctx.extractTypeHint(dbo).getOrElse("")))
    ctx.lookup(dbo).asInstanceOf[Grater[X]].asObject(dbo)
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] =
    ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].iterateOut(o)(f)
}
