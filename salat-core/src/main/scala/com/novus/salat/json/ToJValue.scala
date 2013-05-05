/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ToJValue.scala
 * Last modified: 2012-12-06 22:49:46 EST
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

package com.novus.salat.json

import org.json4s._
import org.json4s.native.JsonMethods._
import com.mongodb.casbah.Imports._
import org.joda.time.{ DateTimeZone, DateTime }
import com.novus.salat.{ Field => SField, _ }
import com.novus.salat.util.Logging
import java.net.URL
import com.novus.salat.TypeFinder
import com.novus.salat.StringTypeHintStrategy
import scala.tools.scalap.scalax.rules.scalasig.{ SingleType, TypeRefType }
import org.bson.types.BSONTimestamp

object MapToJSON extends Logging {

  val empty = compact(render(JObject(Nil)))

  def apply(m: Map[String, _])(implicit ctx: Context): String = compact(render(mapToJObject(m)))

  def mapToJObject(m: Map[String, Any])(implicit ctx: Context): JObject = {
    JObject(m.map {
      case (k, v) => ToJField(k, v)
    }.toList)
  }

  def apply(iter: Iterable[Map[String, _]])(implicit ctx: Context): String = {
    compact(render(JArray(iter.map(mapToJObject(_)).toList)))
  }
}

object ToJField extends Logging {
  def typeHint[X](clazz: Class[X], useTypeHint: Boolean)(implicit ctx: Context) = {
    val th = if (useTypeHint) {
      val field = ctx.typeHintStrategy match {
        case s: StringTypeHintStrategy => JString(clazz.getName)
        case x                         => sys.error("typeHint: unsupported type hint strategy '%s'".format(x))
      }
      JField(ctx.typeHintStrategy.typeHint, field) :: Nil
    }
    else Nil
    log.trace("typeHint: clazz='%s', useTypeHint=%s, th=%s", clazz.getName, useTypeHint, th.mkString("[", ",", "]"))
    th
  }

  def apply(key: String, value: Any)(implicit ctx: Context): JField = {
    JField(key, ToJValue(value))
  }
}

object ToJValue extends Logging {
  def apply(o: Any)(implicit ctx: Context): JValue = o.asInstanceOf[AnyRef] match {
    case t: MongoDBList              => JArray(t.map(apply(_)).toList)
    case t: BasicDBList              => JArray(t.map(apply(_)).toList)
    case dbo: DBObject               => JObject(wrapDBObj(dbo).toList.map(v => JField(v._1, apply(v._2))))
    case ba: Array[Byte]             => JArray(ba.toList.map(JInt(_)))
    case m: Map[_, _]                => JObject(m.toList.map(v => JField(v._1.toString, apply(v._2))))
    case m: java.util.Map[_, _]      => JObject(scala.collection.JavaConversions.mapAsScalaMap(m).toList.map(v => JField(v._1.toString, apply(v._2))))
    case iter: Iterable[_]           => JArray(iter.map(apply(_)).toList)
    case iter: java.lang.Iterable[_] => JArray(scala.collection.JavaConversions.iterableAsScalaIterable(iter).map(apply(_)).toList)
    case x                           => serialize(x)
  }

  def serialize(o: Any)(implicit ctx: Context) = {
    val v = o match {
      case s: String => JString(s)
      case c: Char => JString(c.toString)
      case d: Double => JDouble(d)
      case f: Float => JDouble(f.toDouble)
      case s: Short => JDouble(s.toDouble)
      case bd: BigDecimal => JDouble(bd.toDouble)
      case i: Int => JInt(i)
      case bi: BigInt => JInt(bi)
      case l: Long => JInt(l)
      case b: Boolean => JBool(b)
      case d: java.util.Date => ctx.jsonConfig.dateStrategy.out(d)
      case d: DateTime => ctx.jsonConfig.dateStrategy.out(d)
      case tz: java.util.TimeZone => ctx.jsonConfig.timeZoneStrategy.out(tz)
      case tz: DateTimeZone => ctx.jsonConfig.timeZoneStrategy.out(tz)
      case o: ObjectId => ctx.jsonConfig.objectIdStrategy.out(o)
      case u: java.net.URL => JString(u.toString) // might as well
      case n if n == null && ctx.jsonConfig.outputNullValues => JNull
      case ts: BSONTimestamp => ctx.jsonConfig.bsonTimestampStrategy.out(ts)
      case x: AnyRef => sys.error("serialize: Unsupported JSON transformation for class='%s', value='%s'".format(x.getClass.getName, x))
    }

    //    log.debug(
    //      """
    //        |serialize:
    //        | o.getClass.getName: %s
    //        | o: %s
    //        | v: %s
    //      """.stripMargin, o.asInstanceOf[AnyRef].getClass.getName, o, v)

    v
  }
}

object FromJValue extends Logging {

  var trigger = 0
  def apply(jj: Option[JValue], field: TypeFinder, childType: Option[TypeRefType] = None)(implicit ctx: Context): Option[Any] = {
    val v = jj.map {
      case j: JArray =>
        field match {
          case t if t.isBitSet => {
            val bs = scala.collection.mutable.BitSet.empty
            j.arr.foreach {
              case JInt(bi) => bs.add(bi.toInt)
              case x        => sys.error("expected JInt got %s\n%s".format(x.getClass.getName, x))
            }
            // TODO: move this into TypeMatchers
            val v = field.path match {
              case "scala.collection.BitSet"           => scala.collection.BitSet.empty ++ bs
              case "scala.collection.immutable.BitSet" => scala.collection.immutable.BitSet.empty ++ bs
              case "scala.collection.mutable.BitSet"   => bs
              case x                                   => sys.error("unexpected TypeRefType %s".format(field.t))
            }
            //          log.debug("RETURNING: v=%s", v)
            v
          }
          case t if t.isOption => { // Option[List] means JObject is a JArray but field type is an Option
            val childType = field.t.typeArgs.head.asInstanceOf[TypeRefType]
            val nextChild = { if (childType.typeArgs.size > 0) Some(childType.typeArgs.head.asInstanceOf[TypeRefType]) else None }
            apply(jj, TypeFinder(childType), nextChild)
          }
          case t if t.isTraversable => {
            field.t match {
              case IsMap(_, childType: TypeRefType) => j.arr.flatMap(v => apply(Some(v), TypeFinder(childType), None))
              case _ => {
                val childType = field.t.typeArgs.head.asInstanceOf[TypeRefType]
                val nextChild = { if (childType.typeArgs.size > 0) Some(childType.typeArgs.head.asInstanceOf[TypeRefType]) else None }
                j.arr.flatMap(v => apply(Some(v), TypeFinder(childType), nextChild))
              }
            }
          }
          case notTraversable => sys.error("FromJValue: expected types for Traversable but instead got:\n%s".format(notTraversable))
        }
      case o: JObject if field.isMap => field.t match {
        case IsMap(_, childType: TypeRefType) => {
          val nextChild = { if (childType.typeArgs.size > 0) Some(childType.typeArgs.head.asInstanceOf[TypeRefType]) else None }
          o.obj.map(v => (v._1, {
            val childTF = TypeFinder(childType)
            val nextVal = apply(Some(v._2), childTF, nextChild)
            if (childTF.isOption) Some(nextVal) else nextVal
          })).collect {
            case (key, Some(value)) => key -> value
          }.toMap
        }
        case notMap => sys.error("FromJValue: expected types for Map but instead got:\n%s".format(notMap))
      }
      case v: JValue if field.isOption && childType.isEmpty => field.t.typeArgs.toList match {
        case List(ct: TypeRefType) => {
          ct match {
            case IsMap(_, childType: TypeRefType) => apply(jj, TypeFinder(ct), Some(childType))
            case IsOption(childType: TypeRefType) => Some(apply(jj, TypeFinder(ct), Some(childType)))
            case _ => {
              val childTf = TypeFinder(ct)
              val nextChild = { if (ct.typeArgs.size > 0) Some(ct.typeArgs.head.asInstanceOf[TypeRefType]) else None }
              if (childTf.directlyDeserialize) deserialize(v, childTf) else apply(Some(v), childTf, None)
            }
          }
        }
        case notOption => sys.error("FromJValue: expected type for Option but instead got:\n%s".format(notOption))
      }
      case o: JObject if field.isOid => deserialize(o, field)
      case v: JValue if field.isDate || field.isDateTime => deserialize(v, field)
      case tz: JValue if field.isTimeZone || field.isDateTimeZone => deserialize(tz, field)
      case o: JInt if field.isDate || field.isDateTime => deserialize(o, field)
      case o: JObject if field.isBSONTimestamp => deserialize(o, field)
      case o: JObject => ctx.lookup(if (childType.isDefined) childType.get.symbol.path else field.path).fromJSON(o)
      case x => deserialize(x, if (childType.isDefined) TypeFinder(childType.get) else field)
    }
    // log.debug(
    //   """
    //            | FromJValue:
    //            |                                      j: %s
    //            |                             field.name: %s
    //            |                      field.typeRefType: %s
    //            |             field.typeRefType.typeArgs: [%s]
    //            |                      field.tf.isOption: %s
    //            | field.tf.isDate || field.tf.isDateTime: %s
    //            |                  field.tf.isBigDecimal: %s
    //            |                              childType: %s
    //            |                                      v: %s
    //            |
    //          """.stripMargin, j, field.name, field.typeRefType, field.typeRefType.typeArgs.mkString(", "), field.tf.isOption, field.tf.isDate || field.tf.isDateTime, field.tf.isBigDecimal, childType, v)
    v
  }

  def deserialize(j: JValue, tf: TypeFinder)(implicit ctx: Context): Any = {
    val v = j match {
      case d if tf.isDateTime       => ctx.jsonConfig.dateStrategy.toDateTime(d)
      case d if tf.isDate           => ctx.jsonConfig.dateStrategy.toDate(d)
      case tz if tf.isTimeZone      => ctx.jsonConfig.timeZoneStrategy.toTimeZone(tz)
      case tz if tf.isDateTimeZone  => ctx.jsonConfig.timeZoneStrategy.toDateTimeZone(tz)
      case oid if tf.isOid          => ctx.jsonConfig.objectIdStrategy.in(oid)
      case bt if tf.isBSONTimestamp => ctx.jsonConfig.bsonTimestampStrategy.in(bt)
      case v if tf.t.symbol.path == "scala.Enumeration.Value" => {
        val enumTrans = new com.novus.salat.transformers.Transformer(tf.t.prefix.asInstanceOf[SingleType].symbol.path, tf.t)(ctx) with com.novus.salat.transformers.in.EnumInflater
        enumTrans.transform(v.values)
      }
      case s: JString if tf.isChar       => s.values.charAt(0)
      case s: JString if tf.isURL        => new URL(s.values)
      case s: JString                    => s.values
      case d: JDouble if tf.isBigDecimal => BigDecimal(d.values.toString, ctx.bigDecimalStrategy.mathCtx)
      case d: JDouble if tf.isFloat      => d.values.toFloat
      case d: JDouble if tf.isShort      => d.values.toShort
      case d: JDouble                    => d.values
      case i: JInt if tf.isBigInt        => i.values
      case i: JInt if tf.isLong          => i.values.toLong
      case v: JInt if tf.isValueClass    => v.values.toLong
      case i: JInt                       => i.values.intValue()
      case b: JBool                      => b.values
      case JsonAST.JNull                 => null
      case x: AnyRef                     => sys.error("deserialize: unsupported JSON transformation for class='%s', value='%s'".format(x.getClass.getName, x))
    }
    //    log.debug(
    //      """
    //                    |deserialize:
    //                    | tf.t.symbol.path: %s
    //                    | jValue: %s
    //                    | v.getClass: %s
    //                    | v: %s
    //                  """.stripMargin, tf.t.symbol.path, j, v.asInstanceOf[AnyRef].getClass, v)
    v
  }
}
