package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._

import com.bumnetworks.salat.transformers._
import com.mongodb.casbah.Imports._

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

case class Field(idx: Int, name: String, typeRefType: TypeRefType)(implicit val ctx: Context) extends CasbahLogging {
  import Field._

  @deprecated("not useful, because conditional type unwrapping has moved to transformers")
  lazy val valueType = typeRefType match {
    case IsOption(t) => t
    case IsSeq(t) => t
    case IsMap(_, t) => t
    case _ => typeRefType
  }

  lazy val inTransformer  =  in.pickTransformer(typeRefType).lift
  lazy val outTransformer = out.pickTransformer(typeRefType).lift

  def in_!(value: Any): Option[Any] = inTransformer.apply(typeRefType, value)
  def out_!(element: Any): Option[Any] = outTransformer.apply(typeRefType, element)
}
