package com.novus.salat

import com.mongodb.casbah.Imports._

class ProxyGrater[X <: CaseClass](clazz: Class[X])(implicit ctx: Context) extends Grater[X](clazz)(ctx) {
  def asDBObject(o: X): DBObject =
    ctx.lookup_!(o.getClass.getName).asInstanceOf[Grater[X]].asDBObject(o)

  def asObject(dbo: MongoDBObject): X =
    ctx.lookup_!(dbo).asInstanceOf[Grater[X]].asObject(dbo)

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] =
    ctx.lookup_!(o.getClass.getName).asInstanceOf[Grater[X]].iterateOut(o)(f)
}