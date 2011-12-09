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
import java.lang.reflect.InvocationTargetException
import com.novus.salat.util.{ ToObjectGlitch, Logging }
import com.novus.salat.util.MissingTypeHint

trait ContextDBObjectTransformation {
  ctx: Context =>

  def lookup_?[B <% MongoDBObject](c: String, dbo: B): Option[Grater[_ <: AnyRef]] =
    lookup_?(c) orElse extractTypeHint(dbo).flatMap(lookup_?(_))

  def lookup[B <% MongoDBObject](dbo: B): Grater[_ <: AnyRef] = extractTypeHint(dbo).map(lookup(_)).getOrElse(throw MissingTypeHint(dbo)(this))

  //  def concreteLookup[A <: CaseClass : Manifest] = lookup[A].asInstanceOf[ConcreteGrater[A]]

  //  def concreteLookup[A <: AnyRef: Manifest](o: A): ConcreteGrater[_ <: CaseClass] = {
  //    getCaseClass(o.getClass.getName)(ctx) match {
  //  case Some(clazz) => lookup(o.getClass.getName, o.asInstanceOf[CaseClass]).asInstanceOf[ConcreteGrater[_ <: CaseClass]]
  //  case _           => error("concreteLookup: A='%s', o='%s' is not concrete".format(manifest[A].erasure.getClass.getName, o.getClass.getName))
  //    }
  //  }

  def concreteLookup[A <: AnyRef: Manifest](dbo: DBObject): ConcreteGrater[_ <: CaseClass] = {
    val m = manifest[A].erasure
    // if class is concrete, hole in one
    if (m.getInterfaces.contains(classOf[Product])) {
      lookup(m.getName).asInstanceOf[ConcreteGrater[_ <: CaseClass]]
    }
    else {
      // otherwise, we depend on a type hint in the DBO to know what it is
      lookup(dbo).asInstanceOf[ConcreteGrater[_ <: CaseClass]]
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

    log.info("""

toDBObject:
  o.getClass.getName = %s
  g = %s

    """, o.getClass.getName, g.toString)

    val builder = MongoDBObject.newBuilder
    // handle type hinting, where necessary
    if (ctx.typeHintStrategy.when == TypeHintFrequency.Always ||
      (ctx.typeHintStrategy.when == TypeHintFrequency.WhenNecessary && g.requiresTypeHint)) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(g.clazz.getName)
    }
    g.iterateOut(o) {
      case (key, value) => {
        log.info("toDBObject: K='%s', V='%s'", key, value)
        builder += key -> value
      }
    }.toList
    builder.result()
  }

  //  def toDBObject[X <: AnyRef: Manifest](o: X): DBObject = {
  //    if (o.getClass.getInterfaces.contains(classOf[Product])) {
  //      toDBObject(o.asInstanceOf[CaseClass])
  //    }
  //    else error("NOT CONCRETE")
  //  }

  // moving away from view bounds on DBObject - it's not any use to me here, and it complicates life on the other side
  def fromDBObject[A <: AnyRef: Manifest](dbo: DBObject): A = {
    val g = concreteLookup[A](dbo)
    if (g.sym.isModule) {
      g.companionObject.asInstanceOf[A]
    }
    else {
      // DBObject.get returns AnyRef, but MongoDBObject.get returns Option[AnyRef]
      val mdbo = wrapDBObj(dbo)
      val args = g.indexedFields.map {
        case field if field.ignore => {
          val v = g.safeDefault(field)
          log.info("%s: ignore, use safe default %s", field.name, v)
          v
        }
        case field => {
          val name = ctx.determineFieldName(g.clazz, field)
          val value1 = mdbo.get(name)
          log.info("value1: %s", value1)
          value1 match {
            case Some(value) => {
              val v = field.in_!(value)
              log.info("%s: name='%s' %s ---> %s", field.name, name, value, v)
              v
            }
            case _ => {
              val v = g.safeDefault(field)
              log.info("%s: ??? falling back to safe default %s", field.name, v)
              v
            }
          }
        }
      }.map(_.get.asInstanceOf[AnyRef]) // TODO: if raw get blows up, throw a more informative error

      log.info("ARGS:\n%s\n", args.mkString("\n"))

      var counter = 0
      for (field <- g.indexedFields) {
        log.info("[%d] %s ---> %s", counter, field.name, args(counter))
        counter += 1
      }

      try {
        (g.constructor.newInstance(args: _*)).asInstanceOf[A]
      }
      catch {
        // when something bad happens feeding args into constructor, catch these exceptions and
        // wrap them in a custom exception that will provide detailed information about what's happening.
        case e: InstantiationException    => throw ToObjectGlitch(g, g.sym, g.constructor, args, e)
        case e: IllegalAccessException    => throw ToObjectGlitch(g, g.sym, g.constructor, args, e)
        case e: IllegalArgumentException  => throw ToObjectGlitch(g, g.sym, g.constructor, args, e)
        case e: InvocationTargetException => throw ToObjectGlitch(g, g.sym, g.constructor, args, e)
        case e                            => throw e
      }
    }

  }
}