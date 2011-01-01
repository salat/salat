package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import java.math.{BigDecimal => JavaBigDecimal, RoundingMode, MathContext}

import com.bumnetworks.salat.global.mathCtx
import com.mongodb.casbah.Imports._

package object transformers {
  type OutTransformer = PartialFunction[(Type, Any), Any]
  type InTransformer = PartialFunction[(Type, Any), Any]

  object out extends CasbahLogging {
    lazy val Fallback: OutTransformer = {
      case (t, x) => {
        log.warning("left alone: %s @ %s", x, t)
        x
      }
    }

    lazy val JBigDecimalToDouble: OutTransformer = {
      case (_, jbd: JavaBigDecimal) => jbd.round(implicitly[MathContext]).doubleValue
    }

    lazy val SBigDecimalToDouble: OutTransformer = {
      case (_, sbd: ScalaBigDecimal) => sbd(implicitly[MathContext]).toDouble
    }

    def InContext(implicit ctx: Context): OutTransformer = {
      case (t @ TypeRefType(_, symbol, _), o: Any) if ctx.graters.contains(symbol.path) =>
        ctx.graters(symbol.path).asInstanceOf[Grater[AnyRef]].asDBObject(o.asInstanceOf[AnyRef])
    }
  }

  object in extends CasbahLogging {
    lazy val Fallback: InTransformer = {
      case (t, x) => {
        log.warning("left alone: %s @ %s", x, t)
        x
      }
    }

    lazy val DoubleToJBigDecimal: InTransformer = {
      case (t @ TypeRefType(_, symbol, _), d: Double) if symbol.path == classOf[JavaBigDecimal].getName =>
        new JavaBigDecimal(d.toString, implicitly[MathContext])
    }

    lazy val DoubleToSBigDecimal: InTransformer = {
      case (t @ TypeRefType(_, symbol, _), d: Double) if symbol.path == classOf[ScalaBigDecimal].getName =>
        ScalaBigDecimal(d.toString, implicitly[MathContext])
    }

    def InContext(implicit ctx: Context): InTransformer = {
      case (t @ TypeRefType(_, symbol, _), dbo: DBObject) if ctx.graters.contains(symbol.path) =>
        ctx.graters(symbol.path).asInstanceOf[Grater[AnyRef]].asObject(dbo).asInstanceOf[AnyRef]
    }
  }
}
