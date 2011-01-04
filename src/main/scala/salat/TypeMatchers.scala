package com.bumnetworks.salat

import scala.math.{BigDecimal => ScalaBigDecimal}
import scala.tools.scalap.scalax.rules.scalasig._

import com.bumnetworks.salat.transformers._
import com.mongodb.casbah.Imports._

private object TypeMatchers {
  def matchesOneType(t: Type, name: String): Option[Type] = t match {
    case TypeRefType(_, symbol, List(arg)) if symbol.path == name => Some(arg)
    case _ => None
  }
}

object IsOption {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, "scala.Option")
}

object IsMap {
  def unapply(t: Type): Option[(Type, Type)] =
    t match {
      case TypeRefType(_, symbol, k :: v :: Nil) if symbol.path.endsWith(".Map") => Some(k -> v)
      case _ => None
    }
}

object IsSeq {
  def unapply(t: Type): Option[Type] =
    t match {
      case TypeRefType(_, symbol, List(e)) =>
        if (symbol.path.endsWith(".Seq")) Some(e)
        else if (symbol.path.endsWith(".List")) Some(e)
        else None
      case _ => None
    }
}

object IsScalaBigDecimal {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, classOf[ScalaBigDecimal].getName)
}

object IsTrait extends CasbahLogging {
  def unapply(t: TypeRefType): Option[Type] = t match {
    case t @ TypeRefType(_, symbol, _) => {
      try {
	val parsed = ScalaSigParser.parse(Class.forName(symbol.path)).get.topLevelClasses.head
	if (parsed.isTrait) Some(t)
	else None
      }
      catch {
	case _ => None
      }
    }
    case _ => None
  }
}
