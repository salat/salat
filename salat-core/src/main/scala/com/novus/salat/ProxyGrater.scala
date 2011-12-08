package com.novus.salat

import com.mongodb.casbah.Imports._

class ProxyGrater[X <: AnyRef](clazz: Class[X])(implicit ctx: Context, m: Manifest[X]) extends Grater[X](clazz)(ctx) {

  @deprecated("Use ctx.toDBObject instead") def asDBObject(o: X): DBObject = ctx.toDBObject[X](o)

  @deprecated("Use ctx.fromDBObject instead") def asObject[B <% MongoDBObject](dbo: B): X = {
    ctx.fromDBObject[X, B](dbo)
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] =
    ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].iterateOut(o)(f)
}
