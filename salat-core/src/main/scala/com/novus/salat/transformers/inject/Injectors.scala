/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 *
 */
package com.novus.salat.transformers

import java.lang.reflect.Method
import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import com.novus.salat._
import com.novus.salat.impls._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import org.scala_tools.time.Imports._

package object in {
  def select(pt: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t@TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToSBigDecimal

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with LongToBigInt

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with StringToChar

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DateToJodaDateTime

        case t@TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with OptionInjector with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case t@TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }

      case IsTraversable(t@TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToBigInt with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with StringToChar with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with TraversableInjector {
            val parentType = pt
          }

        case t@TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with TraversableInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with TraversableInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t@TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with TraversableInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with TraversableInjector {
          val parentType = pt
          val grater = ctx.lookup(symbol.path)
        }
      }

      case IsMap(_, t@TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToBigInt with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with StringToChar with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t@TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with MapInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t@TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapInjector {
          val parentType = pt
          val grater = ctx.lookup(symbol.path)
        }
      }
      
      case TypeRefType(_, symbol, _) => pt match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToSBigDecimal

          case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with LongToBigInt

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with StringToChar

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DateToJodaDateTime

        case t@TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case t@TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, pt)(ctx) {}
      }
    }
  }
}


package in {

import java.lang.Integer
import com.novus.salat.annotations.EnumAs

trait LongToInt extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context) = value match {
    case l: Long => l.intValue
    case i: Int => i
    case s: Short => s.intValue
  }
}

trait DoubleToSBigDecimal extends Transformer {
  self: Transformer =>

  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case x: ScalaBigDecimal => x // it doesn't seem as if this could happen, BUT IT DOES.  ugh.
    case d: Double => ScalaBigDecimal(d.toString, ctx.mathCtx)
    case l: Long => ScalaBigDecimal(l.toString, ctx.mathCtx) // sometimes BSON handles a whole number big decimal as a Long...
    case i: Int => ScalaBigDecimal(i.toString, ctx.mathCtx)
    case f: Float => ScalaBigDecimal(f.toString, ctx.mathCtx)
    case s: Short => ScalaBigDecimal(s.toString, ctx.mathCtx)
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
    case dt: DateTime => dt
  }
}

trait LongToBigInt extends Transformer {
  self: Transformer =>

  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case s: String => BigInt(x = s, radix = 10)
    case ba: Array[Byte] => BigInt(ba)
    case bi: BigInt => bi
    case bi: java.math.BigInteger => bi
    case l: Long => BigInt(l)
    case i: Int => BigInt(i)
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
    case _ => None
  }

  private def transform0(dbo: MongoDBObject)(implicit ctx: Context) = (grater orElse ctx.lookup(path, dbo)) match {
    case Some(grater) => grater.asObject(dbo).asInstanceOf[CaseClass]
    case None => throw new GraterFromDboGlitch(path, dbo)(ctx)
  }

  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case dbo: DBObject => transform0(dbo)
    case mdbo: MongoDBObject => transform0(mdbo)
  }
}

trait OptionInjector extends Transformer {
  self: Transformer =>
  override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
    case value if value != null => Some(Some(value))
    case _ => Some(None)
  }
}

trait TraversableInjector extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value

  override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
    case dbl: BasicDBList => {
      val list: MongoDBList = dbl
      Some(list.toList)
    }
    case _ => None
  }

  override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
    case traversable: Traversable[Any] => Some(traversableImpl(parentType, traversable.map {
      
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
    case _ => None
  }

  override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
    case mdbo: MongoDBObject => {
      val builder = MongoDBObject.newBuilder
      mdbo.foreach {
        case (k, v) => builder += k -> super.transform(v)
      }
      Some(mapImpl(parentType, builder.result).asInstanceOf[Map[String, _]])
    }
    case _ => None
  }

  val parentType: TypeRefType
}

class EnumInflaterGlitch(clazz: Class[_], strategy: EnumStrategy, value: Any) extends Error("Not sure how to handle value='%s' as enum of class %s using strategy %s"
  .format(value, clazz.getName, strategy))

trait EnumInflater extends Transformer with Logging {
  self: Transformer =>

  val clazz = Class.forName(path)
  val companion: Any = clazz.companionObject

  val withName: Method = {
    val ms = clazz.getDeclaredMethods
    ms.filter(_.getName == "withName").head
  }
  val applyInt: Method = {
    val ms = clazz.getDeclaredMethods
    ms.filter(_.getName == "apply").head
  }

  object IsInt {
    def unapply(s: String): Option[Int] = s match {
      case s if s != null && s.nonEmpty => try {
        Some(s.toInt)
      }
      catch {
        case _: java.lang.NumberFormatException => None
      }
      case _ => None
    }
  }

  override def transform(value: Any)(implicit ctx: Context): Any = {
    val strategy = clazz.getAnnotation(classOf[EnumAs]) match {
      case specific: EnumAs => specific.strategy
      case _ => ctx.defaultEnumStrategy
    }

    (strategy, value) match {
      case (EnumStrategy.BY_VALUE, name: String) => withName.invoke(companion, name)
      case (EnumStrategy.BY_ID, id: Int) => applyInt.invoke(companion, id.asInstanceOf[Integer])
      case (EnumStrategy.BY_ID, idAsString: String) => idAsString match {
        case IsInt(id) => applyInt.invoke(companion, id.asInstanceOf[Integer])
        case _ => throw new EnumInflaterGlitch(clazz, strategy, value)
      }
      case _ => throw new EnumInflaterGlitch(clazz, strategy, value)
    }
  }

}

}
