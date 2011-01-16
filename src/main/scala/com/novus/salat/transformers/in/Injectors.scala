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
import java.math.MathContext

import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.global.mathCtx
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import com.novus.salat.transformers._
import com.novus.salat.transformers.in._

package object in {
  def select(pt: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToSBigDecimal

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with OptionInjector with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }

      case IsSeq(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with SeqInjector { val parentType = pt }

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with SeqInjector { val parentType = pt }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with SeqInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with SeqInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with SeqInjector {
          val parentType = pt
          val grater = ctx.lookup(symbol.path)
        }
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with MapInjector { val parentType = pt }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
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
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToSBigDecimal

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, pt)(ctx) {}
      }
    }
  }
}


package in {

trait DoubleToSBigDecimal extends Transformer {
  self: Transformer =>
  override def transform(value: Any): Any = value match {
    case d: Double => ScalaBigDecimal(d.toString, mathCtx)
  }
}

trait DBObjectToInContext extends Transformer with InContextTransformer with Logging {
  self: Transformer =>
  override def before(value: Any): Option[Any] = value match {
    case dbo: DBObject => {
      val mdbo: MongoDBObject = dbo
      Some(mdbo)
    }
    case mdbo: MongoDBObject => Some(mdbo)
    case _ => None
  }

  private def transform0(dbo: MongoDBObject)(implicit ctx: Context) =
    (grater orElse ctx.lookup(path, dbo)).map {
      grater => grater.asObject(dbo).asInstanceOf[CaseClass]
    }.getOrElse(throw new Exception("no grater found for '%s' OR '%s'".format(path, dbo(ctx.typeHint.getOrElse(TypeHint)))))

  override def transform(value: Any): Any = value match {
    case dbo: DBObject => transform0(dbo)
    case mdbo: MongoDBObject => transform0(mdbo)
  }
}

trait OptionInjector extends Transformer {
  self: Transformer =>
  override def after(value: Any): Option[Any] = value match {
    case value if value != null => Some(Some(value))
    case _ => Some(None)
  }
}

trait SeqInjector extends Transformer {
  self: Transformer =>
  override def transform(value: Any): Any = value

  override def before(value: Any): Option[Any] = value match {
    case dbl: BasicDBList => {
      val list: MongoDBList = dbl
      Some(list.toList)
    }
    case _ => None
  }

  override def after(value: Any): Option[Any] = value match {
    case list: Seq[Any] => Some(seqImpl(parentType, list.map {
      el => super.transform(el)
    }))
    case _ => None
  }

  val parentType: TypeRefType
}

trait MapInjector extends Transformer {
  self: Transformer =>
  override def transform(value: Any): Any = value

  override def before(value: Any): Option[Any] = value match {
    case dbo: DBObject => {
      val mdbo: MongoDBObject = dbo
      Some(mdbo)
    }
    case _ => None
  }

  override def after(value: Any): Option[Any] = value match {
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

trait EnumInflater extends Transformer {
  self: Transformer =>

  val clazz = Class.forName(path)
  val companion: Any = clazz.companionObject
  val withName: Method = {
    val ms = clazz.getDeclaredMethods
    ms.filter(_.getName == "withName").head
  }

  override def transform(value: Any): Any = value match {
    case name: String => withName.invoke(companion, name)
  }
}

}