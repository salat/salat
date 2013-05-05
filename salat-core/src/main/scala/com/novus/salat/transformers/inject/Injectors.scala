/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         Injectors.scala
 * Last modified: 2013-01-07 22:43:52 EST
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */
package com.novus.salat.transformers

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations.util._
import com.novus.salat.impls._
import com.novus.salat.util._
import java.lang.reflect.Method
import scala.tools.scalap.scalax.rules.scalasig._
import scala.language.reflectiveCalls // compiler-recommended import

package object in extends Logging {

  def select(pt: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if ctx.caseObjectHierarchy.contains(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with CaseObjectInjector

        case TypeRefType(ThisType(_), symbol, _) if isValueClass_!(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with ValueClassInjector

        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with BigDecimalInjector

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with BigIntInjector

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with StringToChar

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DateToJodaDateTime

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with TimeZoneToJodaDateTimeZone

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" =>
          new Transformer(prefix.symbol.path, t)(ctx) with OptionInjector with EnumInflater

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if Types.isTraversable(t.symbol) || Types.isOption(t.symbol) || Types.isMap(t.symbol) => // Wrap nested List
          new Transformer(symbol.path, t)(ctx) with OptionWrappingInjector {
            val wrappedTransformer = select(t, false).asInstanceOf[Transformer]
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }
      case IsTraversable(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if ctx.caseObjectHierarchy.contains(symbol.path) => {
          new Transformer(t.symbol.path, t)(ctx) with CaseObjectInjector with TraversableInjector {
            val parentType = pt
          }
        }

        case TypeRefType(ThisType(_), symbol, _) if isValueClass_!(symbol.path) =>
          new Transformer(t.symbol.path, t)(ctx) with ValueClassInjector with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalInjector with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntInjector with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with StringToChar with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToFloat with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with TimeZoneToJodaDateTimeZone with TraversableInjector {
            val parentType = pt
          }

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumInflater with TraversableInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with TraversableInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if Types.isTraversable(t.symbol) || Types.isOption(t.symbol) || Types.isMap(t.symbol) => // Wrap nested List
          new Transformer(symbol.path, t)(ctx) with TraversableWrappingInjector {
            val wrappedTransformer = select(t).asInstanceOf[Transformer]
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with TraversableInjector {
          val parentType = pt
          val grater = ctx.lookup_?(symbol.path)
        }
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if ctx.caseObjectHierarchy.contains(symbol.path) =>
          new Transformer(t.symbol.path, t)(ctx) with CaseObjectInjector with MapInjector {
            val parentType = pt
          }

        case TypeRefType(ThisType(_), symbol, _) if isValueClass_!(symbol.path) =>
          {
            new Transformer(t.symbol.path, t)(ctx) with MapInjector with ValueClassInjector { // Note: Ordering of with clauses important here!
              val parentType = pt
            }
          }

        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigDecimalInjector with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with BigIntInjector with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with StringToChar with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToFloat with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with TimeZoneToJodaDateTimeZone with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumInflater with MapInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if Types.isMap(t.symbol) || Types.isOption(t.symbol) || Types.isTraversable(t.symbol) => // Wrap nested Map 
          new Transformer(symbol.path, t)(ctx) with MapWrappingInjector {
            val parentType = pt
            val wrappedTransformer = select(t).asInstanceOf[Transformer]
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapInjector {
          val parentType = pt
          val grater = ctx.lookup_?(symbol.path)
        }
      }

      case pt if ctx.caseObjectHierarchy.contains(pt.symbol.path) =>
        new Transformer(pt.symbol.path, pt)(ctx) with CaseObjectInjector

      case TypeRefType(ThisType(_), symbol, _) if isValueClass_!(symbol.path) =>
        new Transformer(symbol.path, pt)(ctx) with ValueClassMapper {
          override def transform(value: Any)(implicit ctx: Context): Any = {
            val vcClazz = vcc(symbol.path)
            val vcType = vType(vcClazz)
            typeMap(vcType, value)
          }
        }

      case TypeRefType(_, symbol, _) => pt match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with BigDecimalInjector

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with BigIntInjector

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with StringToChar

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToFloat

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DateToJodaDateTime

        case TypeRefType(_, symbol, _) if isJodaDateTimeZone(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with TimeZoneToJodaDateTimeZone

        case TypeRefType(_, symbol, _) if Types.isBitSet(symbol) =>
          new Transformer(symbol.path, pt)(ctx) with BitSetInjector

        case t @ TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" => {
          new Transformer(prefix.symbol.path, t)(ctx) with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, pt)(ctx) {}
      }
    }
  }
}

package in {

  import java.lang.Integer
  import org.joda.time.{ DateTimeZone, DateTime }
  import org.json4s.JsonAST.JArray

  trait LongToInt extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context) = value match {
      case l: Long   => l.intValue
      case d: Double => d.intValue // Mongo 1.8.3 shell quirk - fixed with NumberInt in 1.9.1 (see https://jira.mongodb.org/browse/SERVER-854)
      case f: Float  => f.intValue // Mongo 1.8.3 shell quirk - fixed with NumberInt in 1.9.1 (see https://jira.mongodb.org/browse/SERVER-854)
      case i: Int    => i
      case s: Short  => s.intValue
      case x: String => try {
        Integer.valueOf(x)
      }
      catch {
        case e: Throwable => None
      }
    }
  }

  trait BigDecimalInjector extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = ctx.bigDecimalStrategy.in(value)
  }

  trait DoubleToFloat extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case d: Double => d.toFloat
      case i: Int    => i.toFloat
      case l: Long   => l.toFloat
      case s: Short  => s.toFloat
    }
  }

  trait StringToChar extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case s: String if s != null && s.length == 1 => s.charAt(0)
    }
  }

  trait DateToJodaDateTime extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case d: java.util.Date if d != null => new DateTime(d)
      case dt: DateTime                   => dt
    }
  }

  trait TimeZoneToJodaDateTimeZone extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case tz: String if tz != null             => DateTimeZone.forID(tz)
      case tz: java.util.TimeZone if tz != null => DateTimeZone.forID(tz.getID)
      case tz: DateTimeZone                     => tz
    }
  }

  trait BigIntInjector extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = ctx.bigIntStrategy.in(value)
  }

  trait DBObjectToInContext extends Transformer with InContextTransformer with Logging {
    self: Transformer =>
    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case mdbo: MongoDBObject => Some(mdbo)
      case cc: CaseClass       => Some(cc)
      case _                   => None
    }

    private def transform0(dbo: MongoDBObject)(implicit ctx: Context) =
      (if (grater.isDefined) grater else ctx.lookup_?(path, dbo)) match {
        case Some(grater) => grater.asObject(dbo).asInstanceOf[CaseClass]
        case None         => throw GraterFromDboGlitch(path, dbo)(ctx)
      }

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case dbo: DBObject       => transform0(dbo)
      case mdbo: MongoDBObject => transform0(mdbo)
      case x                   => x
    }
  }

  trait CaseObjectInjector extends Transformer with Logging {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context) = value match {
      case s: String => fromPath(ctx.resolveCaseObjectOverrides.get(t.symbol.path, s).
        getOrElse(throw MissingCaseObjectOverride(t.symbol.path, value, ctx.name)))
      case dbo: DBObject       => fromPath(ctx.extractTypeHint(dbo).getOrElse(throw MissingTypeHint(dbo)))
      case mdbo: MongoDBObject => fromPath(ctx.extractTypeHint(mdbo).getOrElse(throw MissingTypeHint(mdbo)))
    }

    def fromPath(path: String) = ClassAnalyzer.companionObject(getClassNamed_!(path)(ctx), ctx.classLoaders)
  }

  // OK, ValueClasses are messy.  If they are a simple type they just use their base type as the value (i.e. don't create
  // a wrapper class instance for them), but... if they're contained in a collection we need to create new wrapper objects
  // for them.
  trait ValueClassInjector extends Transformer with ValueClassMapper with Logging {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context) = {
      val vcClazzConstructor = vcc(t.symbol.path)
      val vcType = vType(vcClazzConstructor)
        def unwrapMap[T, U](m: Map[T, U]) =
          { (vcType: String) =>
            m.map {
              case (k, v) => {
                (k, vcClazzConstructor.newInstance(typeMap(vcType, v)))
              }
            }.toMap
          }
      value match {
        case v: Option[_] => {
          Some(vcClazzConstructor.newInstance(typeMap(vcType, v.get)))
        }
        case m: Map[_, _]     => unwrapMap(m)(vcType)
        case dbo: DBObject    => unwrapMap(dbo.asInstanceOf[scala.collection.mutable.Map[_, _]].toMap)(vcType)
        case m: MongoDBObject => unwrapMap(m.asInstanceOf[scala.collection.mutable.Map[_, _]].toMap)(vcType)
        case v                => vcClazzConstructor.newInstance(typeMap(vcType, v))
      }
    }
  }
  trait ValueClassMapper {
    def vcc(path: String)(implicit ctx: Context) = Class.forName(path, false, ctx.classLoaders.head).getConstructors.head
    def vType(vClazz: java.lang.reflect.Constructor[_]) = vClazz.getParameterTypes.head.getName

    // This whole process is truly bizzare!  If a value is of class, say String, I can't just pass it to newInstance.
    // Nor could I get Class.cast to work.  For reasons beyond me the only reliable thing to do was determine the value's
    // class and then hard-cast it *to the same type* using asInstanceOf.  Completely gonzo, but I'm sure it has something
    // to do with deep, dark Scala internals I probably am better off not understanding.
    val LongClass = classOf[java.lang.Long]
    val FloatClass = classOf[Float]
    val DoubleClass = classOf[Double]
    val BooleanClass = classOf[Boolean]
    val StringClass = classOf[String]
    val IntClass = classOf[Integer]

    def typeMap(vTypeName: String, vv: Any) = vTypeName match {
      case "long"                          => vv.asInstanceOf[java.lang.Long]
      case "int" if (vv.isInstanceOf[Int]) => vv.asInstanceOf[java.lang.Integer]
      case "int"                           => vv.asInstanceOf[Long].toInt.asInstanceOf[java.lang.Integer]
      case "boolean"                       => vv.asInstanceOf[java.lang.Boolean]
      case "float"                         => vv.asInstanceOf[Double].toFloat.asInstanceOf[java.lang.Float]
      case "double"                        => vv.asInstanceOf[java.lang.Double]
      case "java.lang.String"              => vv.asInstanceOf[String]
    }
  }

  trait OptionInjector extends Transformer {
    self: Transformer =>
    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case value @ Some(x) if x != null => Some(value)
      case value if value != null       => Some(Some(value))
      case _                            => Some(None)
    }
  }
  trait OptionWrappingInjector extends Transformer {
    self: Transformer =>
    val wrappedTransformer: Transformer
    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dboV: DBObject if value != null => Some(wrappedTransformer.transform_!(value))
      case value if value != null          => Some(value)
      case _                               => Some(None)
    }
  }

  trait TraversableInjector extends Transformer with Logging {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case mdl: MongoDBList => Some(mdl.toList) // casbah_core 2.3.0_RC1 onwards
      case dbl: BasicDBList => {
        // previous to casbah_core 2.3.0
        val list: MongoDBList = dbl
        Some(list.toList)
      }
      case j: JArray  => Some(j.arr)
      case l: List[_] => Some(l)
      case _          => None
    }
    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case traversable: Traversable[_] => Some(traversableImpl(parentType, traversable.map {
        el => super.transform(el)
      }))
      case _ => None
    }
    val parentType: TypeRefType
  }
  trait TraversableWrappingInjector extends Transformer with Logging {
    self: Transformer =>
    val wrappedTransformer: Transformer
    override def transform(value: Any)(implicit ctx: Context): Any = value
    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case mdl: MongoDBList => Some(mdl.toList) // casbah_core 2.3.0_RC1 onwards
      case dbl: BasicDBList => {
        // previous to casbah_core 2.3.0
        val list: MongoDBList = dbl
        Some(list.toList)
      }
      case j: JArray  => Some(j.arr)
      case l: List[_] => Some(l)
      case _          => None
    }
    override def after(value: Any)(implicit ctx: Context): Option[Any] = {
      value match {
        case traversable: Traversable[_] => Some(traversable.map {
          el => wrappedTransformer.transform_!(el).get
        })
        case _ => None
      }
    }
  }

  trait BitSetInjector extends Transformer with Logging {
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case ba: Array[Byte] => {
        val bs = scala.collection.mutable.BitSet.empty
        ba.foreach(b => bs.add(b))
        Option(path match {
          case "scala.collection.BitSet"           => scala.collection.BitSet.empty ++ bs
          case "scala.collection.immutable.BitSet" => scala.collection.immutable.BitSet.empty ++ bs
          case "scala.collection.mutable.BitSet"   => bs
          case x                                   => sys.error("unexpected TypeRefType %s".format(t))
        })
      }
      case bs: scala.collection.BitSet => Some(bs)
      case _                           => None
    }
  }

  trait MapInjector extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case m: Map[_, _] => Some(m)
      case _            => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case mdbo: MongoDBObject => {
        val builder = MongoDBObject.newBuilder
        mdbo.foreach {
          case (k, v) => builder += k -> super.transform(v)
        }
        Some(mapImpl(parentType, builder.result))
      }
      case m: Map[_, _] => Some(mapImpl(parentType, m))
      case _            => None
    }

    val parentType: TypeRefType
  }

  trait MapWrappingInjector extends Transformer {
    self: Transformer =>
    val wrappedTransformer: Transformer
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] =
      value match {
        case dbo: DBObject => {
          val mdbo: MongoDBObject = dbo
          Some(mdbo)
        }
        case m: Map[_, _] => Some(m)
        case _            => None
      }
    override def after(value: Any)(implicit ctx: Context): Option[Any] =
      value match {
        case mdbo: MongoDBObject => {
          Some((for ((k, v) <- mdbo) yield {
            k -> wrappedTransformer.transform_!(v).get
          }).toMap)
        }
        case m: Map[_, _] => Some(mapImpl(parentType, m))
        case _            => None
      }
    val parentType: TypeRefType
  }

  trait EnumInflater extends Transformer with Logging {
    self: Transformer =>

    val clazz = getClassNamed_!(path)
    val companion: Any = clazz.companionObject
    val withName: Method = clazz.getDeclaredMethods.filter(_.getName == "withName").head
    val applyInt: Method = clazz.getDeclaredMethods.filter(_.getName == "apply").head

    override def transform(value: Any)(implicit ctx: Context): Any = {
      val strategy = {
        val s = getClassNamed_!(path).annotation[com.novus.salat.annotations.raw.EnumAs].map(_.strategy())
        if (s.isDefined) s.get else ctx.defaultEnumStrategy
      }

      (strategy, value) match {
        case (_, v: scala.Enumeration$Val)         => v // Can get called for some reason, even though we already have an enum, so pass-thru harmlessly
        case (EnumStrategy.BY_VALUE, name: String) => withName.invoke(companion, name)
        case (EnumStrategy.BY_ID, id: Int)         => applyInt.invoke(companion, id.asInstanceOf[Integer])
        case (EnumStrategy.BY_ID, idAsString: String) => idAsString match {
          case s: String if s != null && s.nonEmpty => try {
            applyInt.invoke(companion, s.toInt.asInstanceOf[Integer])
          }
          catch {
            case _: java.lang.NumberFormatException => None
          }
          case _ => throw EnumInflaterGlitch(clazz, strategy, value)
        }
        case _ => throw EnumInflaterGlitch(clazz, strategy, value)
      }
    }

  }

}
