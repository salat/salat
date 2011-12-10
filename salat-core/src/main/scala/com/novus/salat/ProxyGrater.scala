package com.novus.salat

import com.mongodb.casbah.Imports._

class ProxyGrater[X <: AnyRef](clazz: Class[X])(implicit ctx: Context, m: Manifest[X]) extends Grater[X](clazz)(ctx) {

  @deprecated("Use ctx.toDBObject instead") def asDBObject(o: X): DBObject = ctx.toDBObject[X](o)

  @deprecated("Use ctx.fromDBObject instead") def asObject[B <% MongoDBObject](dbo: B): X = {
    ctx.fromDBObject[X](unwrapDBObj(dbo))
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] = {
    val g = ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]]
    if (ctx.debug.typeInformation) {
      log.info("""

      iterateOut:
        manifest[X].erasure.getName = %s
        o.getClass.getName = %s
        g.clazz = %s

          """, manifest[X].erasure.getName, o.getClass.getName, g.clazz.getName)
    }
    g.iterateOut(o)(f)
  }

}
