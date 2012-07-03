/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         Injectors.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
package com.novus.salat.transformers

import java.lang.reflect.Method
import scala.collection.immutable.{ List => IList, Map => IMap }
import scala.collection.mutable.{ Map => MMap }
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{ BigDecimal => ScalaBigDecimal }
import com.novus.salat.annotations.util._

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.util._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging
import org.scala_tools.time.Imports._

package object in {

  def select(pt: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with BigDecimalInjector

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with BigIntInjector

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with StringToChar

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToFloat

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DateToJodaDateTime

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with OptionInjector with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }

      case IsTraversable(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalInjector with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntInjector with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with StringToChar with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToFloat with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with TraversableInjector {
            val parentType = pt
          }

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumInflater with TraversableInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with TraversableInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with TraversableInjector {
          val parentType = pt
          val grater = ctx.lookup_?(symbol.path)
        }
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalInjector with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntInjector with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with StringToChar with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToFloat with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumInflater with MapInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapInjector {
          val parentType = pt
          val grater = ctx.lookup_?(symbol.path)
        }
      }

      case TypeRefType(_, symbol, _) => pt match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with BigDecimalInjector

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with BigIntInjector

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with StringToChar

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToFloat

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DateToJodaDateTime

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, pt)(ctx) {}
      }
    }
  }
}

package in {

  import java.lang.Integer
  import com.novus.salat.annotations.EnumAs
  import net.liftweb.json.JsonAST.{ JObject, JArray }

  trait LongToInt extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context) = value match {
      case l: Long   => l.intValue
      case d: Double => d.intValue // Mongo 1.8.3 shell quirk - fixed with NumberInt in 1.9.1 (see https://jira.mongodb.org/browse/SERVER-854)
      case f: Float  => f.intValue // Mongo 1.8.3 shell quirk - fixed with NumberInt in 1.9.1 (see https://jira.mongodb.org/browse/SERVER-854)
      case i: Int    => i
      case s: Short  => s.intValue
      case x: String => try {
        Integer.valueOf(x)
      }
      catch {
        case e => None
      }
    }
  }

  trait BigDecimalInjector extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = ctx.bigDecimalStrategy.in(value)
  }

  trait DoubleToFloat extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case d: Double => d.toFloat
      case i: Int    => i.toFloat
      case l: Long   => l.toFloat
      case s: Short  => s.toFloat
    }
  }

  trait StringToChar extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case s: String if s != null && s.length == 1 => s.charAt(0)
    }
  }

  trait DateToJodaDateTime extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case d: java.util.Date if d != null => new DateTime(d)
      case dt: DateTime                   => dt
    }
  }

  trait BigIntInjector extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = {
      ctx.bigIntStrategy.in(value)
    }
  }

  trait DBObjectToInContext extends Transformer with InContextTransformer with Logging {
    self: Transformer =>
    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case mdbo: MongoDBObject => Some(mdbo)
      case cc: CaseClass       => Some(cc)
      case _                   => None
    }

    private def transform0(dbo: MongoDBObject)(implicit ctx: Context) =
      (if (grater.isDefined) grater else ctx.lookup_?(path, dbo)) match {
        case Some(grater) => grater.asObject(dbo).asInstanceOf[CaseClass]
        case None         => throw GraterFromDboGlitch(path, dbo)(ctx)
      }

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case dbo: DBObject       => transform0(dbo)
      case mdbo: MongoDBObject => transform0(mdbo)
      case x                   => x
    }
  }

  trait OptionInjector extends Transformer {
    self: Transformer =>
    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case value @ Some(x) if x != null => Some(value)
      case value if value != null       => Some(Some(value))
      case _                            => Some(None)
    }
  }

  trait TraversableInjector extends Transformer with Logging {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case mdl: MongoDBList => Some(mdl.toList) // casbah_core 2.3.0_RC1 onwards
      case dbl: BasicDBList => {
        // previous to casbah_core 2.3.0
        val list: MongoDBList = dbl
        Some(list.toList)
      }
      case j: JArray  => Some(j.arr)
      case l: List[_] => Some(l)
      case _          => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case traversable: Traversable[_] => Some(traversableImpl(parentType, traversable.map {
        el => super.transform(el)
      }))
      case _ => None
    }

    val parentType: TypeRefType
  }

  trait MapInjector extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case m: Map[_, _] => Some(m)
      case _            => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case mdbo: MongoDBObject => {
        val builder = MongoDBObject.newBuilder
        mdbo.foreach {
          case (k, v) => builder += k -> super.transform(v)
        }
        Some(mapImpl(parentType, builder.result))
      }
      case m: Map[_, _] => Some(mapImpl(parentType, m))
      case _            => None
    }

    val parentType: TypeRefType
  }

  trait EnumInflater extends Transformer with Logging {
    self: Transformer =>

    val clazz = getClassNamed_!(path)
    val companion: Any = clazz.companionObject
    val withName: Method = clazz.getDeclaredMethods.filter(_.getName == "withName").head
    val applyInt: Method = clazz.getDeclaredMethods.filter(_.getName == "apply").head

    override def transform(value: Any)(implicit ctx: Context): Any = {
      val strategy = {
        val s = getClassNamed_!(path).annotation[com.novus.salat.annotations.raw.EnumAs].map(_.strategy())
        if (s.isDefined) s.get else ctx.defaultEnumStrategy
      }

      (strategy, value) match {
        case (EnumStrategy.BY_VALUE, name: String) => withName.invoke(companion, name)
        case (EnumStrategy.BY_ID, id: Int)         => applyInt.invoke(companion, id.asInstanceOf[Integer])
        case (EnumStrategy.BY_ID, idAsString: String) => idAsString match {
          case s: String if s != null && s.nonEmpty => try {
            applyInt.invoke(companion, s.toInt.asInstanceOf[Integer])
          }
          catch {
            case _: java.lang.NumberFormatException => None
          }
          case _ => throw EnumInflaterGlitch(clazz, strategy, value)
        }
        case _ => throw EnumInflaterGlitch(clazz, strategy, value)
      }
    }

  }

}
