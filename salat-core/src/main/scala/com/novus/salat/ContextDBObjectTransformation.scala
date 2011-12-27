/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat

import com.mongodb.casbah.Imports._
import com.novus.salat.util.ToObjectGlitch._
import com.novus.salat.util.{ ToObjectGlitch, Logging }
import com.novus.salat.util.MissingTypeHint
import java.lang.reflect.{ Constructor, InvocationTargetException }

trait ContextDBObjectTransformation {
  ctx: Context =>

  def lookup_?[B <% MongoDBObject](c: String, dbo: B): Option[Grater[_ <: AnyRef]] =
    lookup_?(c) orElse extractTypeHint(dbo).flatMap(lookup_?(_))

  def lookup[B <% MongoDBObject](dbo: B): Grater[_ <: AnyRef] = extractTypeHint(dbo).map(lookup(_)).getOrElse(throw MissingTypeHint(dbo)(this))

  def concreteLookup[A <: AnyRef: Manifest](dbo: DBObject, path: Option[String] = None): ConcreteGrater[_ <: CaseClass] = {
    val m = manifest[A].erasure
    if (ctx.debug.typeInformation) log.debug("concreteLookup: A=%s dbo.%s='%s'", m.getName, ctx.typeHintStrategy.typeHint, extractTypeHint(dbo))
    // if class is concrete, hole in one
    if (m.getInterfaces.contains(classOf[Product])) {
      lookup(m.getName).asInstanceOf[ConcreteGrater[_ <: CaseClass]]
    }
    else {
      // otherwise, we depend on a path or a type hint in the DBO to know what it is
      path.map(lookup(_)).getOrElse(lookup(dbo)).asInstanceOf[ConcreteGrater[_ <: CaseClass]]
    }
  }

  def extractTypeHint[B <% MongoDBObject](dbo: B): Option[String] = {
    dbo.get(typeHintStrategy.typeHint).map(typeHintStrategy.decode(_))
  }

  // these methods parked here for backwards compatibility

  @deprecated("Use toDBObject instead") def asDBObject[A <: AnyRef: Manifest](o: A): DBObject = toDBObject[A](o)

  @deprecated("Use fromDBObject instead") def asObject[A <: AnyRef: Manifest](dbo: DBObject): A =
    fromDBObject[A](dbo)

  def toDBObject[A <: AnyRef: Manifest](o: A): DBObject = {
    val g = lookup(o.getClass.getName).asInstanceOf[Grater[A]]

    if (ctx.debug.typeInformation) log.info("toDBObject: o.getClass.getName = %s g = %s", o.getClass.getName, g.toString)

    val builder = MongoDBObject.newBuilder
    // handle type hinting, where necessary
    if (ctx.typeHintStrategy.when == TypeHintFrequency.Always ||
      (ctx.typeHintStrategy.when == TypeHintFrequency.WhenNecessary && g.requiresTypeHint)) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(g.clazz.getName)
    }
    g.iterateOut(o) {
      case (key, value) => {
        if (ctx.debug.in) log.info("toDBObject: K='%s', V='%s'", key, value)
        builder += key -> value
      }
    }.toList
    builder.result()
  }

  // moving away from view bounds on DBObject - it complicates life at the callsite
  // ironically, i actually require a MongoDBObject here - the hell with it, just wrap it manually.
  def fromDBObject[A <: AnyRef: Manifest](dbo: DBObject): A = {
    if (ctx.debug.typeInformation) log.info("fromDBObject: m.erasure.getName='%s'", manifest[A].erasure.getName)
    val g = concreteLookup[A](dbo)
    if (ctx.debug.typeInformation) log.info("fromDBObject: g.clazz='%s'", g.clazz.getName)
    if (g.sym.isModule) {
      g.companionObject.asInstanceOf[A]
    }
    else {
      // DBObject.get returns AnyRef, but MongoDBObject.get returns Option[AnyRef]
      val mdbo = wrapDBObj(dbo)
      val args = g.indexedFields.map {
        case field if field.ignore => {
          val v = g.safeDefault(field)
          if (ctx.debug.out) log.info("%s: ignore, use safe default %s", field.name, v)
          v
        }
        case field => {
          val name = ctx.determineFieldName(g.clazz, field)
          val value1 = mdbo.get(name)
          if (ctx.debug.out) log.info("value1: %s", value1)
          value1 match {
            case Some(value) => {
              val v = field.in_!(value)
              if (ctx.debug.out) log.info("%s: name='%s' %s ---> %s", field.name, name, value, v)
              v
            }
            case _ => {
              val v = g.safeDefault(field)
              if (ctx.debug.out) log.info("%s: ??? falling back to safe default %s", field.name, v)
              v
            }
          }
        }
      }.map(_.get.asInstanceOf[AnyRef]) // TODO: if raw get blows up, throw a more informative error

      if (ctx.debug.out) log.info("ARGS:\n%s\n", args.mkString("\n"))

      var counter = 0
      for (field <- g.indexedFields) {
        if (ctx.debug.out) log.info("[%d] %s ---> %s", counter, field.name, args(counter))
        counter += 1
      }

      try {
        (g.constructor.newInstance(args: _*)).asInstanceOf[A]
      }
      catch {
        // when something bad happens feeding args into constructor, catch these exceptions and
        // wrap them in a custom exception that will provide detailed information about what's happening.
        case e: InstantiationException    => throw ToObjectGlitch(g, g.sym, args, e)
        case e: IllegalAccessException    => throw ToObjectGlitch(g, g.sym, args, e)
        case e: IllegalArgumentException  => throw ToObjectGlitch(g, g.sym, args, e)
        case e: InvocationTargetException => throw ToObjectGlitch(g, g.sym, args, e)
        case e                            => throw e
      }
    }
  }
}