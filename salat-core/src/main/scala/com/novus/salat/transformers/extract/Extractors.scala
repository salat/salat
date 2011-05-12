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

import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import com.novus.salat._
import com.novus.salat.impls._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.novus.salat.transformers.out._

package object out {
  def select(t: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    t match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with SBigDecimalToDouble

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with BigIntToLong

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with CharToString

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with OptionExtractor with EnumStringifier
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionExtractor
      }

      case IsSeq(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with SeqExtractor

       case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntToLong with SeqExtractor

       case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString with SeqExtractor

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier with SeqExtractor
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with SeqExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(t.symbol.path, t)(ctx) with InContextToDBObject with SeqExtractor {
            val grater = ctx.lookup(t.symbol.path)
          }

        case TypeRefType(_, symbol, _) =>
          new Transformer(symbol.path, t)(ctx) with SeqExtractor
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with MapExtractor

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntToLong with MapExtractor

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString with MapExtractor

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined =>
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier with MapExtractor

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapExtractor
      }
      
      case IsSet(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with SetExtractor

       case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntToLong with SetExtractor

       case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString with SetExtractor

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier with SetExtractor
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with SetExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(t.symbol.path, t)(ctx) with InContextToDBObject with SetExtractor {
            val grater = ctx.lookup(t.symbol.path)
          }

        case TypeRefType(_, symbol, _) =>
          new Transformer(symbol.path, t)(ctx) with SetExtractor
      }

      case TypeRefType(_, symbol, _) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntToLong

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with CharToString

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) {}
      }
    }
  }
}

package out {

import com.novus.salat.annotations.EnumAs

trait SBigDecimalToDouble extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case sbd: ScalaBigDecimal => sbd(ctx.mathCtx).toDouble
  }
}

trait BigIntToLong extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case bi: BigInt => bi.toLong
    case bi: java.math.BigInteger => bi.longValue
  }
}

trait CharToString extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context) = value match {
    case c: Char => c.toString
    case c: java.lang.Character => c.toString
  }
}

trait InContextToDBObject extends Transformer with InContextTransformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case cc: CaseClass => ctx.lookup_!(path, cc).asInstanceOf[Grater[CaseClass]].asDBObject(cc)
    case _ => MongoDBObject("failed-to-convert" -> value.toString)
  }
}

trait OptionExtractor extends Transformer {
  self: Transformer =>

  // ok, Some(null) should never happen.  except sometimes it does.
  override def before(value: Any)(implicit ctx: Context):Option[Any] = value match {
    case Some(value) if value != null => Some(super.transform(value))
    case _ => None
  }
}

trait SeqExtractor extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value

  override def after(value: Any)(implicit ctx: Context):Option[Any] = value match {
    case seq: Seq[_] =>
      Some(MongoDBList(seq.map {
        case el => super.transform(el)
      }: _*))
    case _ => None
  }
}

trait SetExtractor extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value

  override def after(value: Any)(implicit ctx: Context):Option[Any] = value match {
    case set: Set[_] =>
      Some(MongoDBList((set.map {
        case el => super.transform(el)
      }).toList: _*))
    case _ => None
  }
}

trait MapExtractor extends Transformer {
  self: Transformer =>
  override def transform(value: Any)(implicit ctx: Context): Any = value

  override def after(value: Any)(implicit ctx: Context):Option[Any] = value match {
    case map: Map[String, _] => {
      val builder = MongoDBObject.newBuilder
      map.foreach {
        case (k, el) =>
          builder += (k match {
            case s: String => s case x => x.toString
          }) -> super.transform(el)
      }
      Some(builder.result)
    }
    case _ => None
  }
}

trait EnumStringifier extends Transformer {
  self: Transformer =>

  val strategy = Class.forName(path).getAnnotation(classOf[EnumAs]) match {
    case specific: EnumAs => specific.strategy
    case _ => ctx.defaultEnumStrategy
  }

  override def transform(value: Any)(implicit ctx: Context): Any = value match {
    case ev: Enumeration#Value if strategy == EnumStrategy.BY_VALUE => ev.toString
    case ev: Enumeration#Value if strategy == EnumStrategy.BY_ID => ev.id
  }
}

}

