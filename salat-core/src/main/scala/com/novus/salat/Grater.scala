/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         Grater.scala
 * Last modified: 2012-12-06 22:29:03 EST
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
package com.novus.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.novus.salat.{ Field => SField }

import java.lang.reflect.{ InvocationTargetException, Method }

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util._

import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging
import org.json4s._
import org.json4s.native.JsonMethods._
import com.novus.salat.json.{ FromJValue, ToJField }
import org.json4s.native.JsonParser

// TODO: create companion object to serve as factory for grater creation - there
// is not reason for this logic to be wodged in Context

abstract class Grater[X <: AnyRef](val clazz: Class[X])(implicit val ctx: Context) extends Logging {

  ctx.accept(this)

  def asDBObject(o: X): DBObject

  def asObject[A <% MongoDBObject](dbo: A): X

  def toMap(o: X): Map[String, Any]

  def fromMap(m: Map[String, Any]): X

  def toJSON(o: X): JObject

  def toPrettyJSON(o: X): String = pretty(render(toJSON(o)))

  def toCompactJSON(o: X): String = compact(render(toJSON(o)))

  def toJSONArray(t: Traversable[X]): JArray = JArray(t.map(toJSON(_)).toList)

  def toPrettyJSONArray(t: Traversable[X]): String = pretty(render(toJSONArray(t)))

  def toCompactJSONArray(t: Traversable[X]): String = compact(render(toJSONArray(t)))

  def fromJSON(j: JObject): X

  def fromJSON(s: String): X = JsonParser.parse(s) match {
    case j: JObject  => fromJSON(j)
    case arr: JArray => sys.error("fromJSON: requires a JSON object, but you have input an array - use fromJSONArray instead!")
    case x => sys.error("""
  fromJSON: input string parses to unsupported type '%s' !

  %s

                     """.format(x.getClass.getName, s))
  }

  def fromJSONArray(j: JArray): List[X] = j.arr.map {
    case o: JObject => fromJSON(o)
    case x: JValue  => sys.error("fromJSONArray: unexpected element type '%s'\n%s\n".format(x, compact(render(x))))
  }

  def fromJSONArray(s: String): List[X] = JsonParser.parse(s) match {
    case j: JArray  => fromJSONArray(j)
    case o: JObject => List(fromJSON(o))
    case x => sys.error("""
    fromJSON: input string parses to unsupported type '%s' !

    %s

                     """.format(x.getClass.getName, s))
  }

  def iterateOut[T](o: X, outputNulls: Boolean)(f: ((String, Any)) => T): Iterator[T]

  type OutHandler = PartialFunction[(Any, SField), Option[(String, Any)]]

  def toBSON(o: X): Array[Byte] = org.bson.BSON.encode(asDBObject(o))

  def fromBSON(bytes: Array[Byte]): X = {
    val decoded = org.bson.BSON.decode(bytes)
    val mdbo = com.novus.salat.bson.BSONObjectToMongoDbObject(decoded)
    asObject(mdbo)
  }

  override def toString = "%s(%s @ %s)".format(getClass.getSimpleName, clazz, ctx)

  override def equals(that: Any) = that.isInstanceOf[Grater[_]] && that.hashCode == this.hashCode
}

abstract class ConcreteGrater[X <: CaseClass](clazz: Class[X])(implicit ctx: Context) extends Grater[X](clazz)(ctx) {

  lazy val ca = ClassAnalyzer(clazz, ctx.classLoaders)

  lazy val useTypeHint = {
    ctx.typeHintStrategy.when == TypeHintFrequency.Always ||
      (ctx.typeHintStrategy.when == TypeHintFrequency.WhenNecessary && ca.requiresTypeHint)
  }

  protected def outField(outputNulls: Boolean): OutHandler = {
    case (_, field) if field.ignore => None
    case (null, field) if outputNulls => {
      //      log.debug("Outputting null value for field '"+cachedFieldName(field)+"'")
      Some((cachedFieldName(field), null))
    }
    case (null, _) => None
    case (element, field) => {
      field.out_!(element) match {
        case Some(None) => None
        case Some(serialized) => {
          //          log.info("""
          //
          //                    field.name = '%s'
          //                    value = %s  [%s]
          //                    default = %s [%s]
          //                    value == default? %s
          //
          //                    """, field.name,
          //            serialized,
          //            serialized.asInstanceOf[AnyRef].getClass.getName,
          //            safeDefault(field),
          //            safeDefault(field).map(_.asInstanceOf[AnyRef].getClass.getName).getOrElse("N/A"),
          //            (safeDefault(field).map(dv => dv == serialized).getOrElse(false)))
          val key = cachedFieldName(field)
          serialized match {
            case serialized if ctx.suppressDefaultArgs && !ctx.neverSuppressTheseFields.contains(key) && defaultArg(field).suppress(element) => None
            case serialized => {
              val value = serialized match {
                case Some(unwrapped) => unwrapped
                case _               => serialized
              }
              Some(key -> value)
            }
          }
        }
        case _ => None
      }
    }
  }

  protected[salat] lazy val indexedFields = {
    // don't use allTheChildren here!  this is the indexed fields for clazz and clazz alone
    ca.sym.children
      .filter(c => c.isCaseAccessor && !c.isPrivate)
      .map(_.asInstanceOf[MethodSymbol])
      .zipWithIndex
      .map {
        case (ms, idx) => {
          //        log.info("indexedFields: clazz=%s, ms=%s, idx=%s", clazz, ms, idx)
          SField(idx = idx,
            name = if (ca.keyOverridesFromAbove.contains(ms)) ca.keyOverridesFromAbove(ms) else ms.name,
            t = ClassAnalyzer.typeRefType(ms),
            method = clazz.getMethod(ms.name))
        }

      }
  }

  protected def findAnnotatedFields[A](clazz: Class[A], annotation: Class[_ <: java.lang.annotation.Annotation]) = {
    clazz
      .getDeclaredMethods.toList
      .filter(_.isAnnotationPresent(annotation))
      .filterNot(m => m.annotated_?[Ignore])
      .map {
        case m: Method => m -> {
          log.trace("findAnnotatedFields: clazz=%s, m=%s", clazz, m.getName)
          // do use allTheChildren here: we want to pull down annotations from traits and/or superclass
          ca.allTheChildren
            .filter(f => f.name == m.getName && f.isAccessor)
            .map(_.asInstanceOf[MethodSymbol])
            .headOption match {
              case Some(ms) => SField(-1, ms.name, ClassAnalyzer.typeRefType(ms), m) // TODO: -1 magic number for idx which is required but not used
              case None     => sys.error("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
            }
        }
      }
  }

  protected lazy val extraFieldsToPersist = {
    val persist = classOf[Persist]
    val fromClazz = findAnnotatedFields(clazz, persist)
    // not necessary to look directly on trait, is necessary to look directly on superclass
    val fromSuperclass = ca.interestingSuperclass.flatMap(i => findAnnotatedFields(i._1, persist))

    fromClazz ::: fromSuperclass
  }

  def iterateOut[T](o: X, outputNulls: Boolean)(f: ((String, Any)) => T): Iterator[T] = {
    val fromConstructor = o.productIterator.zip(indexedFields.iterator)
    val withPersist = extraFieldsToPersist.iterator.map {
      case (m, field) => m.invoke(o) -> field
    }
    (fromConstructor ++ withPersist).map {
      case x @ (fieldVal, field) =>
        val out = {
          val o = outField(outputNulls)(x)
          if (o.isEmpty && fieldVal == null) Option((cachedFieldName(field), null)) else o
        }
        //        log.debug(
        //          """
        //            |iterateOut:
        //            |                   clazz: %s
        //            |                   field: %s
        //            |                fieldVal: %s
        //            |                     out: %s
        //            |
        //            |
        //          """.stripMargin, clazz.getName, field, fieldVal, out)
        out
    }.filter(_.isDefined).map(_.get).map(f)
  }

  lazy val fieldNameMap = (indexedFields.filterNot(_.ignore) ++ extraFieldsToPersist.map(_._2)).map {
    field =>
      field -> ctx.determineFieldName(clazz, field.name)
  }.toMap

  def cachedFieldName(field: SField) = fieldNameMap.getOrElse(field, field.name)

  def toJSON(o: X) = {
    val builder = List.newBuilder[JField]
    builder ++= ToJField.typeHint(clazz, useTypeHint)
    iterateOut(o, ctx.jsonConfig.outputNullValues) {
      case (key, value) => {
        val jField = ToJField(key, value)

        //        log.debug(
        //          """
        //                    |toJSON:
        //                    |
        //                    |           key: %s
        //                    |           value: %s [%s]
        //                    |           jfield: %s [%s]
        //                    |
        //                  """.stripMargin, key, value, if (value != null) value.getClass.getName else "null", jField, jField.getClass.getName)

        builder += jField
      }
    }.toList
    val j = JObject(builder.result())
    //    log.debug(
    //      """
    //        |toJSON:
    //        |
    //        |                  input: %s
    //        |                  output:
    //        |%s
    //        |
    //      """.stripMargin, o, j)
    j
  }

  def asDBObject(o: X): DBObject = {
    val builder = MongoDBObject.newBuilder

    // handle type hinting, where necessary
    if (useTypeHint) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(clazz.getName)
    }

    iterateOut(o, false) {
      case (key, value) => builder += key -> value
    }.toList

    builder.result()
  }

  def asObject[A <% MongoDBObject](dbo: A): X =
    if (ca.sym.isModule) {
      ca.companionObject.asInstanceOf[X]
    }
    else {
      val args = indexedFields.map {
        case field if field.ignore => safeDefault(field)
        case field => {
          val name = cachedFieldName(field)
          val value = dbo.get(name)
          //          log.info(
          //            """
          //asObject: %s
          //  field: %s
          //  name: %s
          //  dbo.get("%s"): %s
          //  dbo.get("%s").flatMap(field.in_!(_)): %s
          //  safeDefault: %s
          //            """, clazz.getName, field.name, name, name, value, name, value.flatMap(field.in_!(_)), defaultArg(field).value)

          value.flatMap(field.in_!(_)) orElse safeDefault(field)
        }
      }.map(_.get.asInstanceOf[AnyRef]) // TODO: if raw get blows up, throw a more informative error

      feedArgsToConstructor(args)
    }

  def toMap(o: X): Map[String, Any] = {
    val builder = Map.newBuilder[String, Any]
    // handle type hinting, where necessary
    if (useTypeHint) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(clazz.getName)
    }

    (indexedFields zip o.productIterator.toSeq).filterNot(_._1.ignore).foreach {
      case (field, value) =>
        builder += cachedFieldName(field) -> value
    }
    extraFieldsToPersist.foreach {
      case (m, field) =>
        builder += cachedFieldName(field) -> m.invoke(o)
    }

    builder.result()
  }

  private def feedArgsToConstructor(args: Seq[AnyRef]): X = {
    try {
      ca.constructor.newInstance(args: _*)
    }
    catch {
      // when something bad happens feeding args into constructor, catch these exceptions and
      // wrap them in a custom exception that will provide detailed information about what's happening.
      case e: InstantiationException    => throw ToObjectGlitch(this, ca.sym, ca.constructor, args, e)
      case e: IllegalAccessException    => throw ToObjectGlitch(this, ca.sym, ca.constructor, args, e)
      case e: IllegalArgumentException  => throw ToObjectGlitch(this, ca.sym, ca.constructor, args, e)
      case e: InvocationTargetException => throw ToObjectGlitch(this, ca.sym, ca.constructor, args, e)
      case e: Throwable                 => throw e
    }
  }

  def fromJSON(j: JObject) = {
    if (ca.sym.isModule) {
      ca.companionObject.asInstanceOf[X]
    }
    else {
      val values = j.obj.map(v => (v._1, v._2)).toMap
      val args = indexedFields.map {
        case field if field.ignore => safeDefault(field)
        case field => {
          val name = cachedFieldName(field)
          val rawValue = values.get(name)
          val value = FromJValue(rawValue, field)
          //        log.info(
          //          """
          //fromJSON: %s
          //  field: %s
          //  name: %s
          //  values.get("%s"): %s
          //  fromJsonTransform(rawValue, field): %s
          //  fromJsonTransform(rawValue, field).flatMap(field.in_!(_)): %s
          //  safeDefault: %s
          //                      """, clazz.getName, field.name, name, name, rawValue, value, value.flatMap(field.in_!(_)), defaultArg(field).value)

          value.flatMap(field.in_!) orElse safeDefault(field)
        }
      }
      feedArgsToConstructor(args.flatten.asInstanceOf[Seq[AnyRef]])
    }
  }

  def fromMap(m: Map[String, Any]): X = {
    val args = indexedFields.map {
      field =>
        if (field.ignore) {
          safeDefault(field).get
        }
        else (m.get(cachedFieldName(field)) orElse safeDefault(field)).get.asInstanceOf[AnyRef]
    }
    feedArgsToConstructor(args)
  }

  override def hashCode = ca.sym.path.hashCode

  protected lazy val defaults: Seq[Option[Method]] = indexedFields.map {
    field =>
      try {
        Some(ca.companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
      }
      catch {
        case _: Throwable => None
      }
  }

  def defaultArg(field: SField): DefaultArg = {
    if (field.name == "_id") {
      DefaultArg(clazz, field, Some(ca.companionClass.getMethod("apply$default$%d".format(field.idx + 1)).invoke(ca.companionObject)))
    }
    else if (betterDefaults.contains(field)) {
      betterDefaults(field)
    }
    else sys.error("Grater error: clazz='%s' field '%s' needs to register presence or absence of default values".
      format(clazz, field.name))
  }

  protected[salat] def safeDefault(field: SField) = {
    defaultArg(field).safeValue
  }

  protected[salat] lazy val betterDefaults = {
    val builder = Map.newBuilder[SField, DefaultArg]
    for (field <- indexedFields.filterNot(_.name == "_id")) {
      val defaultMethod = try {
        // Some(null) is actually "desirable" here because it allows using null as a default value for an ignored field
        Some(ca.companionClass.getMethod("apply$default$%d".format(field.idx + 1)).invoke(ca.companionObject))
      }
      catch {
        case _: Throwable => None // indicates no default value was supplied
      }

      builder += field -> DefaultArg(clazz, field, defaultMethod)
    }
    // pad out with extra fields to persist
    extraFieldsToPersist.foreach(f => builder += f._2 -> DefaultArg(clazz, f._2, None))
    builder.result()
  }
}

case class DefaultArg(clazz: Class[_], field: SField, value: Option[AnyRef])(implicit val ctx: Context) extends Logging {

  def suppress(element: Any) = if (ctx.suppressDefaultArgs && field.name != "_id") {
    val result = value.exists {
      v =>
        element match {
          case element: MongoDBList if element.isEmpty && isEmptyTraversable => true
          case element: BasicDBList if element.isEmpty && isEmptyTraversable => true
          case element: BasicDBObject if element.isEmpty && isEmptyMap => true
          case element => element == v
        }
    }

    result
  }
  else false

  lazy val safeValue: Some[AnyRef] = value match {
    case v @ Some(_) => v
    case _ => field.typeRefType match {
      case IsOption(_) => Some(None)
      case _           => throw new Exception("%s requires value for '%s'".format(clazz, field.name))
    }
  }

  lazy val isEmptyMap = value match {
    case Some(m: Map[_, _]) if m.asInstanceOf[Map[_, _]].isEmpty => true
    case _ => false
  }

  lazy val isEmptyTraversable = value match {
    case Some(t: Traversable[_]) if t.asInstanceOf[Traversable[_]].isEmpty => true
    case _ => false
  }
}
