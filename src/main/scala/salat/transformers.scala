package com.bumnetworks.salat.transformers

import java.math.MathContext

import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig.TypeRefType
import scala.math.{BigDecimal => ScalaBigDecimal}

import com.bumnetworks.salat._
import com.bumnetworks.salat.impls._
import com.bumnetworks.salat.global.mathCtx
import com.mongodb.casbah.Imports._

abstract class Transformer(val path: String, val t: TypeRefType)(implicit val ctx: Context) {
  def transform(value: Any): Any = value
  def before(value: Any): Option[Any] = Some(value)
  def after(value: Any): Option[Any] = Some(value)

  def transform_!(x: Any): Option[Any] =
    before(x) match {
      case Some(x) => after(transform(x))
      case _ => None
    }
}

package object out extends CasbahLogging {
  def select(t: TypeRefType)(implicit ctx: Context): Transformer = {
    t match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with SBigDecimalToDouble

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionExtractor
      }

      case IsSeq(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with SeqExtractor

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with SeqExtractor

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(t.symbol.path, t)(ctx) with InContextToDBObject with SeqExtractor

        case TypeRefType(_, symbol, _) =>
	  new Transformer(symbol.path, t)(ctx) with SeqExtractor
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with MapExtractor

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapExtractor
      }

      case TypeRefType(_, symbol, _) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, t)(ctx) with InContextToDBObject

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) {}
      }
    }
  }

  trait SBigDecimalToDouble extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value match {
        case sbd: ScalaBigDecimal => sbd(global.mathCtx).toDouble
      }
  }

  trait InContextToDBObject extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value match {
        case cc: CaseClass => ctx.lookup(path, cc) match {
          case Some(grater) => grater.asInstanceOf[Grater[CaseClass]].asDBObject(cc)
          case _ =>
            throw new Exception("expected to find grater for path %s and %s but found none".format(path, cc.getClass))
        }
        case _ => MongoDBObject("failed-to-convert" -> value.toString)
      }
  }

  trait OptionExtractor extends Transformer {
    self: Transformer =>
      override def before(value: Any): Option[Any] = value match {
        case Some(value) => Some(super.transform(value))
        case _ => None
      }
  }

  trait SeqExtractor extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value

    override def after(value: Any): Option[Any] = value match {
      case seq: Seq[_] =>
        Some(MongoDBList(seq.map {
          case el => super.transform(el)
        } : _*))
      case _ => None
    }
  }

  trait MapExtractor extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value

    override def after(value: Any): Option[Any] = value match {
      case map: Map[String, _] => {
        val builder = MongoDBObject.newBuilder
        map.foreach {
          case (k, el) =>
            builder += (k match { case s: String => s case x => x.toString }) -> super.transform(el)
        }
        Some(builder.result)
      }
      case _ => None
    }
  }
}

package object in {
  def select(pt: TypeRefType)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToSBigDecimal

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }

      case IsSeq(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with SeqInjector { val parentType = pt }

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with SeqInjector { val parentType = pt }

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with SeqInjector { val parentType = pt }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with SeqInjector { val parentType = pt }
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with MapInjector { val parentType = pt }

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector { val parentType = pt }

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector { val parentType = pt }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapInjector { val parentType = pt }
      }

      case TypeRefType(_, symbol, _) => pt match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToSBigDecimal

        case TypeRefType(_, symbol, _) if ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext

	case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
	  new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, pt)(ctx) {}
      }
    }
  }

  trait DoubleToSBigDecimal extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value match {
        case d: Double => ScalaBigDecimal(d.toString, global.mathCtx)
      }
  }

  trait DBObjectToInContext extends Transformer {
    self: Transformer =>
      override def before(value: Any): Option[Any] = value match {
        case dbo: DBObject => {
          val mdbo: MongoDBObject = dbo
          Some(mdbo)
        }
        case mdbo: MongoDBObject => Some(mdbo)
        case _ => None
      }

    private def transform0(dbo: MongoDBObject)(implicit ctx: Context) =
      ctx.lookup(path, dbo).map {
        grater => grater.asObject(dbo).asInstanceOf[CaseClass]
      }.getOrElse(throw new Exception("no grater found for '%s' OR '%s'".format(path, dbo(ctx.typeHint.getOrElse(TypeHint)))))

    override def transform(value: Any): Any = value match {
      case dbo: DBObject => transform0(dbo)
      case mdbo: MongoDBObject => transform0(mdbo)
    }
  }

  trait OptionInjector extends Transformer {
    self: Transformer =>
      override def after(value: Any): Option[Any] = value match {
        case value if value != null => Some(Some(value))
        case _ => Some(None)
      }
  }

  trait SeqInjector extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value

    override def before(value: Any): Option[Any] = value match {
      case dbl: BasicDBList => {
        val list: MongoDBList = dbl
        Some(list.toList)
      }
      case _ => None
    }

    override def after(value: Any): Option[Any] = value match {
      case list: Seq[Any] => Some(seqImpl(parentType, list.map { el => super.transform(el) }))
      case _ => None
    }

    val parentType: TypeRefType
  }

  trait MapInjector extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value

    override def before(value: Any): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case _ => None
    }

    override def after(value: Any): Option[Any] = value match {
      case mdbo: MongoDBObject => {
        val builder = MongoDBObject.newBuilder
        mdbo.foreach {
          case (k, v) => builder += k -> super.transform(v)
        }
        Some(mapImpl(parentType, builder.result).asInstanceOf[Map[String, _]])
      }
      case _ => None
    }

    val parentType: TypeRefType
  }
}
