/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.transformers

import java.lang.reflect.Method
import scala.collection.immutable.{ List => IList, Map => IMap }
import scala.collection.mutable.{ Map => MMap }
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{ BigDecimal => ScalaBigDecimal }
import com.novus.salat.annotations.util._

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.util._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging
import org.scala_tools.time.Imports._

package object in {

  def select(pt: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformer = {
    pt match {
      case IsOption(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToSBigDecimal

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with ByteArrayToBigInt

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with StringToChar

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DoubleToFloat

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DateToJodaDateTime

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with OptionInjector with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with OptionInjector with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with OptionInjector
      }

      case IsTraversable(t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with TraversableInjector {
            val parentType = pt
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with ByteArrayToBigInt with TraversableInjector {
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

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with TraversableInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with TraversableInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with TraversableInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with TraversableInjector {
          val parentType = pt
          val grater = ctx.lookup_?(symbol.path)
        }
      }

      case IsArray(t @ TypeRefType(_, _, _)) => {

        // IsArray: t=TypeRefType(ThisType(scala),
        // scala.Array
        // ,List(TypeRefType(ThisType(com.novus.salat.test.model),com.novus.salat.test.model.Thingy,List())))

        println("IsArray: t=%s".format(t))
        //        println("IsArray: typeArgs=%s", arr.typeArgs)
        //
        //        val t = arr.typeArgs.
        //          filter(_.isInstanceOf[TypeRefType]).
        //          headOption.
        //          getOrElse(throw new Error("Ugh. Arrays.")).
        //          asInstanceOf[TypeRefType]
        //        match {
        //          case x: TypeRefType => x
        //          case _ => throw new Error("help")
        //        }

        t match {
          case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
            new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with ArrayInjector {
              val parentType = pt
            }

          case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
            new Transformer(symbol.path, t)(ctx) with LongToInt with ArrayInjector {
              val parentType = pt
            }

          case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
            new Transformer(symbol.path, t)(ctx) with ByteArrayToBigInt with ArrayInjector {
              val parentType = pt
            }

          case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
            new Transformer(symbol.path, t)(ctx) with StringToChar with ArrayInjector {
              val parentType = pt
            }

          case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
            new Transformer(symbol.path, t)(ctx) with DateToJodaDateTime with ArrayInjector {
              val parentType = pt
            }

          case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
            new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with ArrayInjector {
              val parentType = pt
            }
          }

          case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
            new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with ArrayInjector {
              val parentType = pt
              val grater = ctx.lookup_?(symbol.path)
            }

          case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
            new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with ArrayInjector {
              val parentType = pt
              val grater = ctx.lookup_?(symbol.path)
            }

          case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with ArrayInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }
        }
      }

      case IsMap(_, t @ TypeRefType(_, _, _)) => t match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with DoubleToSBigDecimal with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with LongToInt with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, t)(ctx) with ByteArrayToBigInt with MapInjector {
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

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater with MapInjector {
            val parentType = pt
          }
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
          new Transformer(symbol.path, t)(ctx) with DBObjectToInContext with MapInjector {
            val parentType = pt
            val grater = ctx.lookup_?(symbol.path)
          }

        case TypeRefType(_, symbol, _) => new Transformer(symbol.path, t)(ctx) with MapInjector {
          val parentType = pt
          val grater = ctx.lookup_?(symbol.path)
        }
      }

      case TypeRefType(_, symbol, _) => pt match {
        case TypeRefType(_, symbol, _) if isBigDecimal(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToSBigDecimal

        case TypeRefType(_, symbol, _) if isInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with LongToInt

        case TypeRefType(_, symbol, _) if isBigInt(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with ByteArrayToBigInt

        case TypeRefType(_, symbol, _) if isChar(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with StringToChar

        case TypeRefType(_, symbol, _) if isFloat(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DoubleToFloat

        case TypeRefType(_, symbol, _) if isJodaDateTime(symbol.path) =>
          new Transformer(symbol.path, pt)(ctx) with DateToJodaDateTime

        case t @ TypeRefType(_, _, _) if IsEnum.unapply(t).isDefined => {
          new Transformer(IsEnum.unapply(t).get.symbol.path, t)(ctx) with EnumInflater
        }

        case TypeRefType(_, symbol, _) if hint || ctx.lookup_?(symbol.path).isDefined =>
          new Transformer(symbol.path, pt)(ctx) with DBObjectToInContext {
            val grater = ctx.lookup_?(symbol.path)
          }

        case t @ TypeRefType(_, symbol, _) if IsTraitLike.unapply(t).isDefined =>
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
  import scala.reflect.{ Manifest, ClassManifest }

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
        case e => None
      }
    }
  }

  trait DoubleToSBigDecimal extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case x: ScalaBigDecimal => x // it doesn't seem as if this could happen, BUT IT DOES.  ugh.
      case d: Double          => ScalaBigDecimal(d.toString, ctx.mathCtx)
      case l: Long            => ScalaBigDecimal(l.toString, ctx.mathCtx) // sometimes BSON handles a whole number big decimal as a Long...
      case i: Int             => ScalaBigDecimal(i.toString, ctx.mathCtx)
      case f: Float           => ScalaBigDecimal(f.toString, ctx.mathCtx)
      case s: Short           => ScalaBigDecimal(s.toString, ctx.mathCtx)
    }
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

  trait ByteArrayToBigInt extends Transformer {
    self: Transformer =>

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case s: String                => BigInt(x = s, radix = 10)
      case ba: Array[Byte]          => BigInt(ba)
      case bi: BigInt               => bi
      case bi: java.math.BigInteger => bi
      case l: Long                  => BigInt(l)
      case i: Int                   => BigInt(i)
    }
  }

  trait DBObjectToInContext extends Transformer with InContextTransformer with Logging {
    self: Transformer =>
    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case mdbo: MongoDBObject => Some(mdbo)
      case _                   => None
    }

    private def transform0(dbo: MongoDBObject)(implicit ctx: Context) = (grater orElse ctx.lookup_?(path, dbo)) match {
      case Some(grater) => grater.asObject(dbo).asInstanceOf[CaseClass]
      case None         => throw GraterFromDboGlitch(path, dbo)(ctx)
    }

    override def transform(value: Any)(implicit ctx: Context): Any = value match {
      case dbo: DBObject       => transform0(dbo)
      case mdbo: MongoDBObject => transform0(mdbo)
    }
  }

  trait OptionInjector extends Transformer {
    self: Transformer =>
    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case value if value != null => Some(Some(value))
      case _                      => Some(None)
    }
  }

  trait TraversableInjector extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbl: BasicDBList => {
        val list: MongoDBList = dbl
        Some(list.toList)
      }
      case _ => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case traversable: Traversable[_] => Some(traversableImpl(parentType, traversable.map {
        el => super.transform(el)
      }))
      case _ => None
    }

    val parentType: TypeRefType
  }

  trait MapInjector extends Transformer {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbo: DBObject => {
        val mdbo: MongoDBObject = dbo
        Some(mdbo)
      }
      case _ => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case mdbo: MongoDBObject => {
        val builder = MongoDBObject.newBuilder
        mdbo.foreach {
          case (k, v) => builder += k -> super.transform(v)
        }
        Some(mapImpl(parentType, builder.result))
      }
      case _ => None
    }

    val parentType: TypeRefType
  }

  trait EnumInflater extends Transformer with Logging {
    self: Transformer =>

    val clazz = getClassNamed(path).getOrElse(throw new Error(
      "Could not resolve enum='%s' in any of the %d classpaths in ctx='%s'".
        format(path, ctx.classLoaders.size, ctx.name)))
    val companion: Any = clazz.companionObject

    val withName: Method = {
      val ms = clazz.getDeclaredMethods
      ms.filter(_.getName == "withName").head
    }
    val applyInt: Method = {
      val ms = clazz.getDeclaredMethods
      ms.filter(_.getName == "apply").head
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

    override def transform(value: Any)(implicit ctx: Context): Any = {
      val strategy = getClassNamed_!(path).annotation[com.novus.salat.annotations.raw.EnumAs].
        map(_.strategy()).getOrElse(ctx.defaultEnumStrategy)

      (strategy, value) match {
        case (EnumStrategy.BY_VALUE, name: String) => withName.invoke(companion, name)
        case (EnumStrategy.BY_ID, id: Int)         => applyInt.invoke(companion, id.asInstanceOf[Integer])
        case (EnumStrategy.BY_ID, idAsString: String) => idAsString match {
          case IsInt(id) => applyInt.invoke(companion, id.asInstanceOf[Integer])
          case _         => throw EnumInflaterGlitch(clazz, strategy, value)
        }
        case _ => throw EnumInflaterGlitch(clazz, strategy, value)
      }
    }

  }

  trait ArrayInjector extends Transformer with Logging {
    self: Transformer =>
    override def transform(value: Any)(implicit ctx: Context): Any = value

    override def before(value: Any)(implicit ctx: Context): Option[Any] = value match {
      case dbl: BasicDBList => {
        val list: MongoDBList = dbl
        Some(list.toList)
      }
      case _ => None
    }

    override def after(value: Any)(implicit ctx: Context): Option[Any] = {

      value match {
        case list: List[_] => {
          // this is not the most glamourous thing i have ever done, mind you.
          val arr = t.symbol.path match {
            case x if x.endsWith(".Boolean") => list.toArray(ClassManifest.Boolean.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".BigInt") => list.map(super.transform(_).asInstanceOf[AnyRef]).
              toArray(ClassManifest.fromClass(classOf[BigInt]).asInstanceOf[ClassManifest[AnyRef]])
            case x if x.endsWith(".BigDecimal") => list.map(super.transform(_).asInstanceOf[AnyRef]).
              toArray(ClassManifest.fromClass(classOf[BigDecimal]).asInstanceOf[ClassManifest[AnyRef]])
            case x if x.endsWith(".Byte")   => list.toArray(ClassManifest.Byte.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".Char")   => list.toArray(ClassManifest.Char.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".Double") => list.toArray(ClassManifest.Double.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".Float")  => list.toArray(ClassManifest.Float.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".Int")    => list.toArray(Manifest.Int.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".Long")   => list.toArray(Manifest.Long.asInstanceOf[ClassManifest[Any]])
            case x if x.endsWith(".Short")  => list.toArray(ClassManifest.Short.asInstanceOf[ClassManifest[Any]])
            case _ => list.map(super.transform(_).asInstanceOf[AnyRef]).
              toArray(ClassManifest.classType(getClassNamed_!(t.symbol.path)(ctx)))
          }

          log.info("""

after:
RAW INPUTS
  t: %s
  t.symbol.path: %s
  parentType: %s
  parentType.symbol.path: %s
  value: %s
ARRAY
  %s
  size: %d
  %s

                """, t, t.symbol.path, parentType, parentType.symbol.path, value, arr.getClass.getName, arr.size, arr.mkString("\n"))

          Option(arr)
        }
        case _ => None
      }
    }

    val parentType: TypeRefType
  }

}
