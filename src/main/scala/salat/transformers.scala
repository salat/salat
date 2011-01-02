package com.bumnetworks.salat.transformers

import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import java.math.{BigDecimal => JavaBigDecimal, RoundingMode, MathContext}

import com.bumnetworks.salat._
import com.bumnetworks.salat.global.mathCtx
import com.mongodb.casbah.Imports._

object `package` {
  type Transformer = PartialFunction[(Type, Any), Any]
  type MaterializedTransformer = Function1[(Type, Any), Option[Any]]
}

object out extends CasbahLogging {
  def Fallback(implicit ctx: Context): Transformer = {
    case (t, x) => {
      log.warning("left alone: %s @ %s", x, t)
      x
    }
  }

  def JBigDecimalToDouble(implicit ctx: Context): Transformer = {
    case (TypeRefType(_, symbol, _), x) if symbol.path == classOf[JavaBigDecimal].getName =>
      x match {
        case jbd: JavaBigDecimal => jbd.round(implicitly[MathContext]).doubleValue
      }
  }

  def SBigDecimalToDouble(implicit ctx: Context): Transformer = {
    case (TypeRefType(_, symbol, _), x) if symbol.path == classOf[ScalaBigDecimal] =>
      x match {
	case sbd: ScalaBigDecimal => sbd(implicitly[MathContext]).toDouble
      }
  }

  def InContext(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), o) if ctx.graters.contains(symbol.path) =>
      ctx.graters(symbol.path).asInstanceOf[Grater[AnyRef with Product]].asDBObject(o.asInstanceOf[AnyRef with Product])
  }

  def *(implicit ctx: Context) =
    (InContext _) :: (SBigDecimalToDouble _) :: (JBigDecimalToDouble _) :: Nil
}

object in extends CasbahLogging {
  def Fallback(implicit ctx: Context): Transformer = {
    case (t, x) => {
      log.warning("left alone: %s @ %s", x, t)
      x
    }
  }

  def DoubleToJBigDecimal(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), d: Double) if symbol.path == classOf[JavaBigDecimal].getName =>
      new JavaBigDecimal(d.toString, implicitly[MathContext])
  }

  def DoubleToSBigDecimal(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), d: Double) if symbol.path == classOf[ScalaBigDecimal].getName =>
      ScalaBigDecimal(d.toString, implicitly[MathContext])
  }

  def InContext(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), dbo: DBObject) if ctx.graters.contains(symbol.path) =>
      ctx.graters(symbol.path).asInstanceOf[Grater[AnyRef with Product]].asObject(dbo).asInstanceOf[AnyRef with Product]
  }
}
