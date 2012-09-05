/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ToJValue.scala
 * Last modified: 2012-06-28 15:37:34 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */

package com.novus.salat.json

import net.liftweb.json._
import com.mongodb.casbah.Imports._
import org.joda.time.DateTime
import com.novus.salat.{ Field => SField, _ }
import com.novus.salat.util.Logging
import java.net.URL
import com.novus.salat.TypeFinder
import com.novus.salat.StringTypeHintStrategy
import scala.tools.scalap.scalax.rules.scalasig.{ TypeRefType, SingleType }
import org.bson.types.BSONTimestamp

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
    case t: MongoDBList => JArray(t.map(apply(_)).toList)
    case t: BasicDBList => JArray(t.map(apply(_)).toList)
    case dbo: DBObject  => JObject(wrapDBObj(dbo).toList.map(v => JField(v._1, apply(v._2))))
    case m: Map[_, _]   => JObject(m.toList.map(v => JField(v._1.toString, apply(v._2))))
    case x              => serialize(x)
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

  def apply(j: Option[JValue], field: SField, childType: Option[TypeRefType] = None)(implicit ctx: Context): Option[Any] = j.map {
    case j: JArray => field.typeRefType match {
      case IsTraversable(childType: TypeRefType) => j.arr.flatMap(v => apply(Some(v), field, Some(childType)))
      case notTraversable                        => sys.error("FromJValue: expected types for Traversable but instead got:\n%s".format(notTraversable))
    }
    case o: JObject if field.tf.isMap && childType.isEmpty => field.typeRefType match {
      case IsMap(_, childType: TypeRefType) => {
        //        val builder = Map.newBuilder[String, Any]
        //        o.obj.foreach {
        //          v =>
        //            apply(Some(v.value), field, Some(childType)).foreach {
        //              value =>
        //                builder += v.name -> value
        //            }
        //        }

        o.obj.map(v => (v.name, apply(Some(v.value), field, Some(childType)))).collect {
          case (key, Some(value)) => key -> value
        }.toMap
      }
      case notMap => sys.error("FromJValue: expected types for Map but instead got:\n%s".format(notMap))
    }
    case o: JObject if field.tf.isOption && childType.isEmpty => field.typeRefType.typeArgs match {
      case List(childType: TypeRefType) => apply(j, field, Some(childType))
      case notOption                    => sys.error("FromJValue: expected type for Option but instead got:\n%s".format(notOption))
    }
    case o: JObject if field.tf.isOid => deserialize(o, field.tf)
    case o: JObject if field.tf.isDate || field.tf.isDateTime => deserialize(o, field.tf)
    case o: JObject if field.tf.isBSONTimestamp => deserialize(o, field.tf)
    case o: JObject => ctx.lookup(if (childType.isDefined) childType.get.symbol.path else field.typeRefType.symbol.path).fromJSON(o)
    case x => deserialize(x, if (childType.isDefined) TypeFinder(childType.get) else field.tf)
  }

  def deserialize(j: JValue, tf: TypeFinder)(implicit ctx: Context): Any = {
    val v = j match {
      case v if tf.isDateTime      => ctx.jsonConfig.dateStrategy.toDateTime(v)
      case v if tf.isDate          => ctx.jsonConfig.dateStrategy.toDate(v)
      case v if tf.isOid           => ctx.jsonConfig.objectIdStrategy.in(v)
      case v if tf.isBSONTimestamp => ctx.jsonConfig.bsonTimestampStrategy.in(v)
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
      case i: JInt                       => i.values.intValue()
      case b: JBool                      => b.values
      case JsonAST.JNull                 => null
      case x: AnyRef                     => sys.error("deserialize: unsupported JSON transformation for class='%s', value='%s'".format(x.getClass.getName, x))
    }
    //    log.debug(
    //      """
    //            |deserialize:
    //            | tf.t.symbol.path: %s
    //            | jValue: %s
    //            | v.getClass: %s
    //            | v: %s
    //          """.stripMargin, tf.t.symbol.path, j, v.asInstanceOf[AnyRef].getClass, v)
    v
  }
}
