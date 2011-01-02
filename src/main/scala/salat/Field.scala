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

  lazy val valueType = typeRefType match {
    case IsOption(t) => t
    case IsSeq(t) => t
    case IsMap(_, t) => t
    case _ => typeRefType
  }

  lazy val inTransformer  =  in.pickTransformer(valueType)
  lazy val outTransformer = out.pickTransformer(valueType)

  def in_!(value: Any): Option[Any] = {
    typeRefType match {
      case _ => value match {
        case v if v != null => {
          val transformed = inTransformer.apply(valueType, v)
          typeRefType match {
	    case IsOption(_) => Some(transformed)
	    case _ => transformed
          }
        }
        case _ => typeRefType match {
          case IsOption(_) => Some(None)
          case _ => None
        }
      }
    }
  }

  def out_!(element: Any): Option[Any] = {
    typeRefType match {
      case IsOption(_) => element match {
        case Some(value) => outTransformer.apply(valueType, value)
        case _ => None
      }

      case IsSeq(_) =>
        element match {
          case seq: Seq[_] =>
            Some(MongoDBList(seq.map {
              case el =>
                outTransformer.apply(valueType, el) match {
                  case Some(value) => value
                  case _ => None // XXX: this isn't DWIM and should never happen.
                }
            } : _*))
          case _ => None
        }

      case IsMap(_, _) =>
        element match {
          case map: scala.collection.Map[String, _] =>
            Some(map.foldLeft(MongoDBObject()) {
              case (dbo, (k, el)) =>
                outTransformer.apply(valueType, el) match {
                  case Some(value) => dbo ++ MongoDBObject((k match { case s: String => s case x => x.toString }) -> value)
                  case _ => dbo
                }
            })
          case _ => None
        }
      case _ => outTransformer.apply(typeRefType, element)
    }
  }
}
