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
import com.mongodb.casbah.Implicits._
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

  def concreteLookup[A <: AnyRef: Manifest, B <% MongoDBObject](dbo: B): ConcreteGrater[_ <: CaseClass] = {
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

  @deprecated("Use fromDBObject instead") def asObject[A <: AnyRef: Manifest, B <% MongoDBObject](dbo: B): A =
    fromDBObject[A, B](dbo)

  def toDBObject[A <: AnyRef: Manifest](o: A): DBObject = {
    val g = lookup(o.getClass.getName).asInstanceOf[Grater[A]]

    val builder = MongoDBObject.newBuilder
    // handle type hinting, where necessary
    if (ctx.typeHintStrategy.when == TypeHintFrequency.Always ||
      (ctx.typeHintStrategy.when == TypeHintFrequency.WhenNecessary && g.requiresTypeHint)) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(g.clazz.getName)
    }
    g.iterateOut(o) {
      case (key, value) => builder += key -> value
    }.toList
    builder.result()
  }

  def fromDBObject[A <: AnyRef: Manifest, B <% MongoDBObject](dbo: B): A = {
    val g = concreteLookup[A, B](dbo)
    if (g.sym.isModule) {
      g.companionObject.asInstanceOf[A]
    }
    else {
      val args = g.indexedFields.map {
        case field if field.ignore => g.safeDefault(field)
        case field => {
          dbo.get(ctx.determineFieldName(g.clazz, field)) match {
            case Some(value) => {
              field.in_!(value)
            }
            case _ => g.safeDefault(field)
          }
        }
      }.flatten.map(_.asInstanceOf[AnyRef])

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