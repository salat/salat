package com.bumnetworks.salat.transformers

import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}
import java.math.MathContext

import com.bumnetworks.salat._
import com.bumnetworks.salat.global.mathCtx
import com.mongodb.casbah.Imports._

object `package` {
  type Transformer = PartialFunction[(Type, Any), Any]
  type MaterializedTransformer = Function1[(Type, Any), Option[Any]]
}

trait CanPickTransformer {
  def Fallback(implicit ctx: Context): Transformer
  def *(implicit ctx: Context): Seq[Transformer]

  def pickTransformer(t: Type)(implicit ctx: Context): MaterializedTransformer =
    *.foldLeft(Fallback) {
      case (accumulate, pf) =>
        if (pf.isDefinedAt(t -> null)) pf orElse accumulate
        else accumulate
    }.lift
}

object out extends CasbahLogging with CanPickTransformer {
  def Fallback(implicit ctx: Context): Transformer = {
    case (t, x) => x
  }

  def SBigDecimalToDouble(implicit ctx: Context): Transformer = {
    case (TypeRefType(_, symbol, _), x) if symbol.path == classOf[ScalaBigDecimal].getName =>
      x match {
        case sbd: ScalaBigDecimal => sbd(implicitly[MathContext]).toDouble
      }
  }

  def InContext(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), o) if ctx.graters.contains(symbol.path) =>
      ctx.graters(symbol.path).asInstanceOf[Grater[CaseClass]].asDBObject(o.asInstanceOf[CaseClass])
  }

  def *(implicit ctx: Context) =
    (InContext _) :: (SBigDecimalToDouble _) :: Nil
}

object in extends CasbahLogging with CanPickTransformer {
  def Fallback(implicit ctx: Context): Transformer = {
    case (t, x) => x
  }

  def DoubleToSBigDecimal(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), x) if symbol.path == classOf[ScalaBigDecimal].getName =>
      x match {
        case d: Double => ScalaBigDecimal(d.toString, implicitly[MathContext])
      }
  }

  def InContext(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), x) if ctx.lookup(symbol.path).isDefined =>
      x match {
        case dbo: DBObject => ctx.lookup(symbol.path, dbo).map {
          grater => grater.asObject(dbo).asInstanceOf[CaseClass]
        }.getOrElse(throw new Exception("no grater found for '%s' OR '%s'".format(symbol.path, dbo(ctx.typeHint))))
      }
  }

  def *(implicit ctx: Context) =
    (InContext _) :: (DoubleToSBigDecimal _) :: Nil
}
