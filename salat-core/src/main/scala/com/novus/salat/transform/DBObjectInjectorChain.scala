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
package com.novus.salat.transform

import com.novus.salat._
import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import scala.math.{ BigDecimal => SBigDecimal }
import com.novus.salat.util.GraterFromDboGlitch._
import com.mongodb.DBObject
import com.novus.salat.util.{ ClassPrettyPrinter, GraterFromDboGlitch, TransformPrettyPrinter }

object DBObjectInjectorChain extends TransformerChain {

  def bigDecimalTransformer = {
    case (path, t @ TypeRefType(_, symbol, _), ctx, value) if isBigDecimal(symbol.path) => {
      val xformed = value match {
        case x: SBigDecimal => x // it doesn't seem as if this could happen, BUT IT DOES.  ugh.
        case d: Double      => SBigDecimal(d.toString, ctx.mathCtx)
        case l: Long        => SBigDecimal(l.toString, ctx.mathCtx) // sometimes BSON handles a whole number big decimal as a Long...
        case i: Int         => SBigDecimal(i.toString, ctx.mathCtx)
        case f: Float       => SBigDecimal(f.toString, ctx.mathCtx)
        case s: Short       => SBigDecimal(s.toString, ctx.mathCtx)
      }
      //      log.info(TransformPrettyPrinter("bigDecimalTransformer", value.asInstanceOf[AnyRef], t, xformed.asInstanceOf[AnyRef]))
      xformed
    }
  }

  def bigIntTransformer = {
    case (path, t @ TypeRefType(_, symbol, _), ctx, value) if isBigInt(symbol.path) => {
      val xformed = value match {
        case s: String                => BigInt(x = s, radix = 10)
        case ba: Array[Byte]          => BigInt(ba)
        case bi: BigInt               => bi
        case bi: java.math.BigInteger => BigInt(bi.byteValue())
        case l: Long                  => BigInt(l)
        case i: Int                   => BigInt(i)
      }
      //      log.info(TransformPrettyPrinter("bigIntTransformer", value.asInstanceOf[AnyRef], t, xformed.asInstanceOf[AnyRef]))
      xformed
    }
  }

  def caseClassTransformer = {
    case (path, t @ TypeRefType(_, symbol, _), ctx, IsDbo(dbo)) => {
      //      log.info("""
      //
      //      caseClassTransformer:
      //      path: %s
      //      trt: %s
      //      value: %s
      //      %s
      //
      //      """, path, t, ClassPrettyPrinter(value.asInstanceOf[AnyRef]), value)

      val grater = (ctx.extractTypeHint(dbo) match {
        case Some(typeHint) => ctx.lookup_?(typeHint)
        case None           => ctx.lookup_?(symbol.path) orElse ctx.lookup_?(path)
      }).getOrElse(throw GraterFromDboGlitch(symbol.path, dbo)(ctx))
      val xformed = grater.asObject(dbo)
      log.info(TransformPrettyPrinter("caseClassTransformer", dbo, t, xformed.asInstanceOf[AnyRef]))
      xformed
    }
  }

  def charTransformer = emptyPf

  def dateTimeTransformer = emptyPf

  def enumTransformer = emptyPf

  def floatTransformer = emptyPf

  def mapTransformer = emptyPf

  def optionTransformer = {
    case (path, IsOption(t @ TypeRefType(_, _, _)), ctx, value) => {
      val xformed = Option(transform(path, t, ctx, value))
      //      log.info(TransformPrettyPrinter("optionTransformer", value.asInstanceOf[AnyRef], t, xformed.asInstanceOf[AnyRef]))
      xformed
    }
  }

  def traitLikeTransformer = emptyPf

  def traversableTransformer = emptyPf
}

