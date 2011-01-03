package com.bumnetworks.salat.transformers

import java.math.MathContext

import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import com.bumnetworks.salat._
import com.bumnetworks.salat.global.mathCtx
import com.mongodb.casbah.Imports._

object `package` extends CasbahLogging {
  type Transformer = PartialFunction[(Type, Any), Any]
  type MaterializedTransformer = Function1[(Type, Any), Option[Any]]

  object ImplClasses {
    val IListClass = classOf[IList[_]].getName
    val BufferClass = classOf[Buffer[_]].getName
    val SeqClass = classOf[scala.collection.Seq[_]].getName

    val IMapClass = classOf[IMap[_,_]].getName
    val MMapClass = classOf[MMap[_,_]].getName
  }

  def seqImpl(name: String, real: collection.Seq[_]): scala.collection.Seq[_] = name match {
    case ImplClasses.IListClass => IList.empty ++ real
    case ImplClasses.BufferClass => Buffer.empty ++ real
    case ImplClasses.SeqClass => IList.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Seq[_] impl for %s".format(x))
  }

  def seqImpl(t: Type, real: collection.Seq[_]): scala.collection.Seq[_] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.package.Seq" => seqImpl(ImplClasses.SeqClass, real)
        case "scala.package.List" => seqImpl(ImplClasses.IListClass, real)
        case x => seqImpl(x, real)
      }
    }

  def mapImpl(name: String, real: collection.Map[_,_]): scala.collection.Map[_,_] = name match {
    case ImplClasses.IMapClass => IMap.empty ++ real
    case ImplClasses.MMapClass => MMap.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Map[_,_] impl for %s".format(x))
  }

  def mapImpl(t: Type, real: collection.Map[_,_]): scala.collection.Map[_,_] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
	case "scala.Predef.Map" => mapImpl(ImplClasses.IMapClass, real)
        case x => mapImpl(x, real)
      }
    }
}

trait CanPickTransformer {
  def Fallback(implicit ctx: Context): Transformer
  def *(implicit ctx: Context): Seq[Transformer]

  def pickTransformer(t: Type)(implicit ctx: Context): Transformer =
    *.foldLeft(Fallback) {
      case (accumulate, pf) =>
        if (pf.isDefinedAt(t -> null)) pf orElse accumulate
        else accumulate
    }
}

object out extends CasbahLogging with CanPickTransformer {
  def Fallback(implicit ctx: Context): Transformer = {
    case (t, x) => x
  }

  def OptionExtractor(implicit ctx: Context): Transformer = {
    case (IsOption(underlying), x) => x match {
      case Some(value) => pickTransformer(underlying).lift.apply(underlying, value).get
      case _ => None
    }
  }

  def SeqExtractor(implicit ctx: Context): Transformer = {
    case (IsSeq(underlying), x) => x match {
      case seq: Seq[_] => {
        Some(MongoDBList(seq.map {
          case el => pickTransformer(underlying).lift.apply(underlying, el) match {
            case Some(value) => value
            case _ => None // XXX: this isn't DWIM and should never happen.
          }
        } : _*))
      }
    }
  }

  def MapExtractor(implicit ctx: Context): Transformer = {
    case (IsMap(_, underlying), x) => x match {
      case map: scala.collection.Map[String, _] =>
        Some(map.foldLeft(MongoDBObject()) {
          case (dbo, (k, el)) =>
            pickTransformer(underlying).lift.apply(underlying, el) match {
              case Some(value) => dbo ++ MongoDBObject((k match { case s: String => s case x => x.toString }) -> value)
              case _ => dbo
            }
        })
      case _ => None
    }
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
    (OptionExtractor _) :: (SeqExtractor _ ) :: (MapExtractor _) :: (InContext _) :: (SBigDecimalToDouble _) :: Nil
}

object in extends CasbahLogging with CanPickTransformer {
  def Fallback(implicit ctx: Context): Transformer = {
    case (t, x) => x
  }

  def OptionInjector(implicit ctx: Context): Transformer = {
    case (IsOption(underlying), x) => x match {
      case x if x != null => Some(pickTransformer(underlying).lift.apply(underlying, x))
      case _ => None
    }
  }

  def SeqInjector(implicit ctx: Context): Transformer = {
    case (wrapper @ IsSeq(underlying), x) => x match {
      case x: BasicDBList => {
        val list: MongoDBList = x
        seqImpl(wrapper, list.toList.map {
          el => pickTransformer(underlying).lift.apply(underlying, el).get
        })
      }
    }
  }

  def MapInjector(implicit ctx: Context): Transformer = {
    case (wrapper @ IsMap(_, underlying), x) => x match {
      case x: DBObject if x != null => {
        val dbo: MongoDBObject = x
	mapImpl(wrapper, dbo.map {
          case (k, v) => k -> pickTransformer(underlying).lift.apply(underlying, v).get
        }.asInstanceOf[collection.Map[_,_]])
      }
    }
  }

  def DoubleToSBigDecimal(implicit ctx: Context): Transformer = {
    case (t @ TypeRefType(_, symbol, _), x) if symbol.path == classOf[ScalaBigDecimal].getName =>
      x match {
        case d: Double => ScalaBigDecimal(d.toString, implicitly[MathContext])
        case _ => throw new Exception("expected Double, got: %s @ %s".format(x, x.asInstanceOf[AnyRef].getClass))
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
    (OptionInjector _ ) :: (SeqInjector _) :: (MapInjector _) :: (InContext _) :: (DoubleToSBigDecimal _) :: Nil
}
