package com.novus.salat

import scala.math.{BigDecimal => ScalaBigDecimal}
import com.novus.salat.util.Logging
import scala.tools.scalap.scalax.rules.scalasig.{SingleType, TypeRefType, Type}

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

object IsTraversable {
  def unapply(t: Type): Option[Type] =
    t match {
      case TypeRefType(_, symbol, List(e)) =>
        if (symbol.path.endsWith(".Seq")) Some(e)
        else if (symbol.path.endsWith(".List")) Some(e)
        else if (symbol.path.endsWith(".Set")) Some(e)
        else if (symbol.path.endsWith(".Buffer")) Some(e)
        else if (symbol.path.endsWith(".ArrayBuffer")) Some(e)
        else if (symbol.path.endsWith(".Vector")) Some(e)
        else if (symbol.path.endsWith(".IndexedSeq")) Some(e)
        else if (symbol.path.endsWith(".LinkedList")) Some(e)
        else if (symbol.path.endsWith(".DoubleLinkedList")) Some(e)
        else None
      case _ => None
    }
}

object IsScalaBigDecimal {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, classOf[ScalaBigDecimal].getName)
}

object IsEnum extends Logging {
  def unapply(t: TypeRefType): Option[SingleType] = {
    t match {
      case TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" =>
	Some(prefix)
      case _ => None
    }
  }
}

object IsInt {
  def unapply(s: String): Option[Int] = s match {
    case s if s != null && s.nonEmpty => try {
      Some(s.toInt)
    }
    catch {
      case _: java.lang.NumberFormatException => None
    }
    case _ => None
  }
}