/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         ProxyGrater.scala
 * Last modified: 2012-06-27 23:42:09 EDT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */

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

  def fromJSON(j: JObject) = ctx.lookup(j).asInstanceOf[Grater[X]].fromJSON(j)

  def toJSON(o: X) = ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].toJSON(o)

  def toMap(o: X) = ctx.lookup(o.getClass.getName).asInstanceOf[Grater[X]].toMap(o)

  def fromMap(m: Map[String, Any]) = ctx.lookup(m).asInstanceOf[Grater[X]].fromMap(m)
}
