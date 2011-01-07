package com.bumnetworks.salat

import scala.math.{BigDecimal => ScalaBigDecimal}
import scala.tools.scalap.scalax.rules.scalasig._

import com.bumnetworks.salat.annotations.raw._
import com.bumnetworks.salat.annotations.util._
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
  private def noAnnotationOnTrait(t: Class[_]) =
    throw new Exception("NB: trait %s must be annotated with @com.bumnetworks.salat.annotations.Salat " +
                        "in order to be picked up by this library. See the docs for more details.".format(t.getName))

  def unapply(t: TypeRefType): Option[Type] = t match {
    case t @ TypeRefType(_, symbol, _) => {
      try {
        getClassNamed(symbol.path) match {
          case Some(clazz: Class[_]) => {
            val parsed = ScalaSigParser.parse(clazz).get.topLevelClasses.head
            if (parsed.isTrait) {
              if (clazz.annotated_?[Salat]) Some(t)
              else noAnnotationOnTrait(clazz)
            } else None
          }
          case _ => None
        }
      }
      catch {
        case _ => None
      }
    }
    case _ => None
  }
}
