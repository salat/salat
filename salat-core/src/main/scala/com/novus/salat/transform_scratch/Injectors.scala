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
package com.novus.salat.transform_scratch

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.annotations._

import scala.collection.Traversable
import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.commons.Imports._
import com.novus.salat.annotations.raw.EnumAs
import com.novus.salat.util._
import scala.math.{ BigDecimal => SBigDecimal }
import org.scala_tools.time.Imports._

trait OptionInjector extends Transformation {
  override def after(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case value if value != null => Some(Some(value))
    case _                      => None
  }
}

trait TraversableInjector extends TransformationWithParentType {
  override def before(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case dbl: BasicDBList => {
      val list: MongoDBList = dbl
      Some(list.toList)
    }
    case _ => None
  }

  override def after(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case traversable: Traversable[Any] => Some(traversableImpl(pt, traversable.map(transform(path, t, _))))
    case _                             => None
  }
}

trait MapInjector extends TransformationWithParentType {
  override def before(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case dbo: DBObject => {
      val mdbo: MongoDBObject = dbo
      Some(mdbo)
    }
    case _ => None
  }

  override def after(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case mdbo: MongoDBObject => {
      val builder = MongoDBObject.newBuilder
      mdbo.foreach {
        case (k, v) => builder += k -> transform(path, t, v)
      }
      Some(mapImpl(pt, builder.result()))

    }
  }
}

trait EnumInflation extends Transformation {

  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = {

    val clazz = Class.forName(path)
    val companionObject = clazz.companionObject
    val strategy = clazz.getAnnotation(classOf[EnumAs]) match {
      case specific: EnumAs => specific.strategy
      case _                => ctx.defaultEnumStrategy
    }

    (strategy, value) match {
      case (EnumStrategy.BY_VALUE, name: String) => clazz.getDeclaredMethods.
        filter(_.getName == "withName").head.
        invoke(companionObject, name)
      case (EnumStrategy.BY_ID, id: Int) => clazz.getDeclaredMethods.
        filter(_.getName == "apply").head.
        invoke(companionObject, id.asInstanceOf[Integer])
      case (EnumStrategy.BY_ID, idAsString: String) => idAsString match {
        case IsInt(id) => clazz.getDeclaredMethods.
          filter(_.getName == "apply").head.
          invoke(companionObject, id.asInstanceOf[Integer])
        case _ => throw EnumInflaterGlitch(clazz, strategy, value)
      }
      case _ => throw EnumInflaterGlitch(clazz, strategy, value)
    }
  }
}

trait InContextInjector extends Transformation {

  override def before(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case dbo: DBObject => {
      val mdbo: MongoDBObject = dbo
      Some(mdbo)
    }
    case mdbo: MongoDBObject => Some(mdbo)
    case _                   => None
  }

  // TODO: add proxyGrater
  private def transform0(path: String, t: TypeRefType, dbo: MongoDBObject)(implicit ctx: Context) = (ctx.lookup(t.symbol.path) orElse ctx.lookup(path, dbo)) match {
    case Some(grater) => grater.asObject(dbo).asInstanceOf[CaseClass]
    case None         => throw GraterFromDboGlitch(path, dbo)(ctx)
  }

  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case dbo: DBObject       => transform0(path, t, dbo)
    case mdbo: MongoDBObject => transform0(path, t, mdbo)
  }
}

trait LongToInt extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case l: Long  => l.intValue
    case i: Int   => i
    case s: Short => s.intValue
  }
}

trait DoubleToSBigDecimal extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case x: SBigDecimal => x // it doesn't seem as if this could happen, BUT IT DOES.  ugh.
    case d: Double      => SBigDecimal(d.toString, ctx.mathCtx)
    case l: Long        => SBigDecimal(l.toString, ctx.mathCtx) // sometimes BSON handles a whole number big decimal as a Long...
    case i: Int         => SBigDecimal(i.toString, ctx.mathCtx)
    case f: Float       => SBigDecimal(f.toString, ctx.mathCtx)
    case s: Short       => SBigDecimal(s.toString, ctx.mathCtx)
  }
}

trait LongToBigInt extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case s: String                => BigInt(x = s, radix = 10)
    case ba: Array[Byte]          => BigInt(ba)
    case bi: BigInt               => bi
    case bi: java.math.BigInteger => bi
    case l: Long                  => BigInt(l)
    case i: Int                   => BigInt(i)
  }
}

trait DoubleToFloat extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case d: Double => d.toFloat
    case i: Int    => i.toFloat
    case l: Long   => l.toFloat
    case s: Short  => s.toFloat
  }
}

trait StringToChar extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case s: String if s != null && s.length == 1 => s.charAt(0)
  }
}

trait DateToJodaDateTime extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case d: java.util.Date if d != null => new DateTime(d)
    case dt: DateTime                   => dt
  }
}

