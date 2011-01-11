package com.bumnetworks.salat.transformers

import java.lang.reflect.Method
import java.math.MathContext

import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._
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

trait InContextTransformer {
  self: Transformer =>
    val grater: Option[Grater[_ <: CaseClass]]
}

package object out extends CasbahLogging {
  def select(t: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    t match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with SBigDecimalToDouble

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with OptionExtractor with EnumStringifier
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionExtractor with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionExtractor
      }

      case IsSeq(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with SeqExtractor

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier with SeqExtractor
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with SeqExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(t.symbol.path, t)(ctx) with InContextToDBObject with SeqExtractor {
            val grater = ctx.lookup(t.symbol.path)
          }

        case TypeRefType(_, symbol, _) =>
          new Transformer(symbol.path, t)(ctx) with SeqExtractor
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble with MapExtractor

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined =>
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier with MapExtractor

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject with MapExtractor {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapExtractor
      }

      case TypeRefType(_, symbol, _) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with SBigDecimalToDouble

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumStringifier
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with InContextToDBObject {
            val grater = ctx.lookup(symbol.path)
          }

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

  trait InContextToDBObject extends Transformer with InContextTransformer {
    self: Transformer =>
      override def transform(value: Any): Any = value match {
        case cc: CaseClass => ctx.lookup_!(path, cc).asInstanceOf[Grater[CaseClass]].asDBObject(cc)
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

  trait EnumStringifier extends Transformer {
    self: Transformer =>
      override def transform(value: Any): Any = value match {
        case ev: Enumeration#Value => ev.toString
      }
  }
}

package object in {
  def select(pt: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToSBigDecimal

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with OptionInjector with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }

      case IsSeq(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with SeqInjector { val parentType = pt }

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with SeqInjector { val parentType = pt }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with SeqInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with SeqInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with SeqInjector {
          val parentType = pt
          val grater = ctx.lookup(symbol.path)
        }
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with MapInjector { val parentType = pt }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapInjector {
          val parentType = pt
          val grater = ctx.lookup(symbol.path)
        }
      }

      case TypeRefType(_, symbol, _) => pt match {
        case TypeRefType(_, symbol, _) if symbol.path == classOf[ScalaBigDecimal].getName =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToSBigDecimal

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTrait.unapply(t).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup(symbol.path)
          }

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

  trait DBObjectToInContext extends Transformer with InContextTransformer {
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
      (grater orElse ctx.lookup(path, dbo)).map {
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

  trait EnumInflater extends Transformer {
    self: Transformer =>

      val clazz = Class.forName(path)
    val companion: Any = clazz.companionObject
    val withName: Method = {
      val ms = clazz.getDeclaredMethods
      ms.filter(_.getName == "withName").head
    }

    override def transform(value: Any): Any = value match {
      case name: String => withName.invoke(companion, name)
    }
  }
}
