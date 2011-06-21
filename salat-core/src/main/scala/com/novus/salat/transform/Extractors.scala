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
package com.novus.salat.transform

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.annotations._

import scala.collection.Traversable
import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.commons.Imports._
import com.novus.salat.annotations.raw.EnumAs
import com.novus.salat.util._
import scala.math.{BigDecimal => SBigDecimal}
import org.scala_tools.time.Imports._



trait OptionExtractor extends Transformation {
  override def before(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case Some(value) if value != null => Some(transform(path, t, value))
    case _ => None
  }
}

trait TraversableExtractor extends TransformationWithParentType {
  override def after(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case traversable: Traversable[_] => Some(MongoDBList(traversable.map(transform(path, t, _)).toSeq: _*))
    case _ => None
  }
}

trait MapExtractor extends TransformationWithParentType {

  override def after(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case map: scala.collection.Map[String, _] => {
      val builder = MongoDBObject.newBuilder
      map.foreach {
        case (k, el) =>
          builder += (k match {
            case s: String => s
            case x => x.toString
          }) -> transform(path, t, el)
      }
      Some(builder.result)
    }
    case _ => None
  }
}

trait EnumDeflator extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = {
    val strategy = Class.forName(path).getAnnotation(classOf[EnumAs]) match {
      case specific: EnumAs => specific.strategy
      case _ => ctx.defaultEnumStrategy
    }
    value match {
      case ev: Enumeration#Value if strategy == EnumStrategy.BY_VALUE => ev.toString
      case ev: Enumeration#Value if strategy == EnumStrategy.BY_ID => ev.id
    }
  }
}

trait InContextExtractor extends Transformation {
  // TODO: add proxyGrater
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case cc: CaseClass => ctx.lookup_!(path, cc).asInstanceOf[Grater[CaseClass]].asDBObject(cc)
    case _ => MongoDBObject("failed-to-convert" -> value.toString)
  }
}

trait SBigDecimalToDouble extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case sbd: SBigDecimal => sbd(ctx.mathCtx).toDouble
  }
}

trait BigIntToLong extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case bi: BigInt => bi.toLong
    case bi: java.math.BigInteger => bi.longValue
  }
}

trait FloatToDouble extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case f: Float => f.toDouble
    case f: java.lang.Float => f.doubleValue()
  }
}

trait CharToString extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value match {
    case c: Char => c.toString
    case c: java.lang.Character => c.toString
  }
}









