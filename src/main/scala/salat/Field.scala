package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._

object Field {
}

object IsOption {
  def unapply(t: Type): Option[Type] =
    t match {
      case TypeRefType(_, symbol, List(arg)) if symbol.path == "scala.Option" => Some(arg)
      case _ => None
    }
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

case class Field(ms: MethodSymbol, typeRefType: TypeRefType) {
  import Field._
}
