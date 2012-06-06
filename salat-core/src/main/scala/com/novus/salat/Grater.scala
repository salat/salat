/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         Grater.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
package com.novus.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.novus.salat.{ Field => SField }

import java.lang.reflect.{ InvocationTargetException, Constructor, Method }

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util._

import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging
import net.liftweb.json._
import java.util.Date
import org.joda.time.DateTime
import java.net.URL

// TODO: create companion object to serve as factory for grater creation - there
// is not reason for this logic to be wodged in Context

abstract class Grater[X <: AnyRef](val clazz: Class[X])(implicit val ctx: Context) extends Logging {

  ctx.accept(this)

  def asDBObject(o: X): DBObject

  def asObject[A <% MongoDBObject](dbo: A): X

  def toJSON(o: X): JObject

  def toMap(o: X): Map[String, Any]

  def fromMap(m: Map[String, Any]): X

  def toPrettyJSON(o: X): String = pretty(render(toJSON(o)))

  def toCompactJSON(o: X): String = compact(render(toJSON(o)))

  def fromJSON(j: JObject): X

  def fromJSON(s: String): X = JsonParser.parse(s) match {
    case j: JObject => fromJSON(j)
    case x => error("""
  fromJSON: input string parses to unsupported type '%s' !

  %s

                     """.format(x.getClass.getName, s))
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T]

  type OutHandler = PartialFunction[(Any, SField), Option[(String, Any)]]

  override def toString = "%s(%s @ %s)".format(getClass.getSimpleName, clazz, ctx)

  override def equals(that: Any) = that.isInstanceOf[Grater[_]] && that.hashCode == this.hashCode
}

abstract class ConcreteGrater[X <: CaseClass](clazz: Class[X])(implicit ctx: Context) extends Grater[X](clazz)(ctx) {

  protected def findSym[A](clazz: Class[A]): SymbolInfoSymbol = {
    val _sig = ScalaSigUtil.parseScalaSig0(clazz, ctx.classLoaders)
    if (_sig.isDefined) {
      val sig = _sig.get
      if (sig.topLevelClasses.nonEmpty) {
        sig.topLevelClasses.head
      }
      else if (sig.topLevelObjects.nonEmpty) {
        sig.topLevelObjects.head
      }
      else throw MissingExpectedType(clazz)
    }
    else throw MissingPickledSig(clazz)
  }

  protected lazy val sym = findSym(clazz)

  // annotations on a getter don't actually inherit from a trait or an abstract superclass,
  // but dragging them down manually allows for much nicer behaviour - this way you can specify @Persist or @Key
  // on a trait and have it work all the way down
  protected def interestingClass(clazz: Class[_]) = clazz match {
    case clazz if clazz == null => false // inconceivably, this happens!
    case clazz if clazz.getName.startsWith("java.") => false
    case clazz if clazz.getName.startsWith("javax.") => false
    case clazz if clazz.getName.startsWith("scala.") => false
    case clazz if clazz.getEnclosingClass != null => false // filter out nested traits and superclasses
    case _ => true
  }

  protected lazy val interestingInterfaces: List[(Class[_], SymbolInfoSymbol)] = {
    val interfaces = clazz.getInterfaces // this should return an empty array, but...  sometimes returns null!
    if (interfaces != null && interfaces.nonEmpty) {
      val builder = List.newBuilder[(Class[_], SymbolInfoSymbol)]
      for (interface <- interfaces) {
        if (interestingClass(interface)) {
          builder += ((interface, findSym(interface)))
        }
      }
      builder.result()
    }
    else Nil
  }
  protected lazy val interestingSuperclass: List[(Class[_], SymbolInfoSymbol)] = clazz.getSuperclass match {
    case superClazz if interestingClass(superClazz) => List((superClazz, findSym(superClazz)))
    case _ => Nil
  }

  protected def requiresTypeHint = {
    clazz.annotated_?[Salat] || interestingInterfaces.map(_._1.annotated_?[Salat]).contains(true) || interestingSuperclass.map(_._1.annotated_?[Salat]).contains(true)
  }

  protected lazy val useTypeHint = {
    ctx.typeHintStrategy.when == TypeHintFrequency.Always ||
      (ctx.typeHintStrategy.when == TypeHintFrequency.WhenNecessary && requiresTypeHint)
  }

  // for use when you just want to find something and whether it was declared in clazz, some trait clazz extends, or clazz' own superclass
  // is not a concern
  protected lazy val allTheChildren: Seq[Symbol] = sym.children ++ interestingInterfaces.map(_._2.children).flatten ++ interestingSuperclass.map(_._2.children).flatten

  protected def outField: OutHandler = {
    case (_, field) if field.ignore => None
    case (null, _)                  => None
    case (element, field) => {
      field.out_!(element) match {
        case Some(None) => None
        case Some(serialized) => {
          //          log.info("""
          //
          //          field.name = '%s'
          //          value = %s  [%s]
          //          default = %s [%s]
          //          value == default? %s
          //
          //          """, field.name,
          //            serialized,
          //            serialized.asInstanceOf[AnyRef].getClass.getName,
          //            safeDefault(field),
          //            safeDefault(field).map(_.asInstanceOf[AnyRef].getClass.getName).getOrElse("N/A"),
          //            (safeDefault(field).map(dv => dv == serialized).getOrElse(false)))

          serialized match {
            case serialized if ctx.suppressDefaultArgs && defaultArg(field).suppress(serialized) => None
            case serialized => {
              val key = cachedFieldName(field)
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
    sym.children
      .filter(c => c.isCaseAccessor && !c.isPrivate)
      .map(_.asInstanceOf[MethodSymbol])
      .zipWithIndex
      .map {
        case (ms, idx) => {
          //        log.info("indexedFields: clazz=%s, ms=%s, idx=%s", clazz, ms, idx)
          SField(idx = idx,
            name = if (keyOverridesFromAbove.contains(ms)) keyOverridesFromAbove(ms) else ms.name,
            t = typeRefType(ms),
            method = clazz.getMethod(ms.name))
        }

      }
  }

  protected def findAnnotatedMethodSymbol[A](clazz: Class[A], annotation: Class[_ <: java.lang.annotation.Annotation]) = {
    clazz
      .getDeclaredMethods.toList
      .filter(_.isAnnotationPresent(annotation))
      .filterNot(m => m.annotated_?[Ignore])
      .map {
        case m: Method => m -> {
          log.trace("findAnnotatedFields: clazz=%s, m=%s", clazz, m.getName)
          // do use allTheChildren here: we want to pull down annotations from traits and/or superclass
          allTheChildren
            .filter(f => f.name == m.getName && f.isAccessor)
            .map(_.asInstanceOf[MethodSymbol])
            .headOption match {
              case Some(ms) => ms
              case None     => throw new RuntimeException("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
            }
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
          allTheChildren
            .filter(f => f.name == m.getName && f.isAccessor)
            .map(_.asInstanceOf[MethodSymbol])
            .headOption match {
              case Some(ms) => SField(-1, ms.name, typeRefType(ms), m) // TODO: -1 magic number for idx which is required but not used
              case None     => throw new RuntimeException("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
            }
        }
      }
  }

  protected lazy val extraFieldsToPersist = {
    val persist = classOf[Persist]
    val fromClazz = findAnnotatedFields(clazz, persist)
    // not necessary to look directly on trait, is necessary to look directly on superclass
    val fromSuperclass = interestingSuperclass.flatMap(i => findAnnotatedFields(i._1, persist))

    fromClazz ::: fromSuperclass
  }

  protected lazy val keyOverridesFromAbove = {
    val key = classOf[Key]
    val builder = Map.newBuilder[MethodSymbol, String]
    val annotated = interestingInterfaces.map(i => findAnnotatedMethodSymbol(i._1, key)).flatten ++
      interestingSuperclass.map(i => findAnnotatedMethodSymbol(i._1, key)).flatten
    for ((method, ms) <- annotated) {
      method.annotation[Key].map(_.value) match {
        case Some(key) => builder += ms -> key
        case None      =>
      }
    }
    builder.result
  }

  protected lazy val companionClass = clazz.companionClass
  protected lazy val companionObject = clazz.companionObject

  protected[salat] lazy val constructor: Constructor[X] = BestAvailableConstructor(clazz)

  protected def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] = {
    val fromConstructor = o.productIterator.zip(indexedFields.iterator)
    val withPersist = extraFieldsToPersist.iterator.map {
      case (m, field) => m.invoke(o) -> field
    }
    (fromConstructor ++ withPersist).map(outField).filter(_.isDefined).map(_.get).map(f)
  }

  lazy val fieldNameMap = (indexedFields.filterNot(_.ignore) ++ extraFieldsToPersist.map(_._2)).map {
    field =>
      field -> ctx.determineFieldName(clazz, field.name)
  }.toMap

  def cachedFieldName(field: SField) = fieldNameMap.getOrElse(field, field.name)

  protected[salat] def fromJsonTransform(j: Option[JValue], field: SField): Option[AnyRef] = j.map {
    case j: JArray                 => j.arr.map(fromJsonTranform0(_, field))
    case o: JObject if field.isMap => o.obj.map(v => (v.name, fromJsonTranform0(v.value, field))).toMap
    case x                         => fromJsonTranform0(x, field)
  }

  protected[salat] def jsonTranform(o: Any): JValue = o.asInstanceOf[AnyRef] match {
    case t: BasicDBList => JArray(t.map(jsonTranform(_)).toList)
    case dbo: DBObject  => JObject(wrapDBObj(dbo).toList.map(v => JField(v._1, jsonTranform(v._2))))
    case m: Map[_, _]   => JObject(m.toList.map(v => JField(v._1.toString, jsonTranform(v._2))))
    case x              => jsonTranform0(x)
  }

  protected[salat] def fromJsonTranform0(j: JValue, field: SField): AnyRef = j match {
    case v if field.isDateTime => ctx.jsonConfig.dateStrategy.toDateTime(v)
    case v if field.isDate => ctx.jsonConfig.dateStrategy.toDate(v)
    case s: JString if TypeMatchers.matches(field.typeRefType, "java.net.URL") => new URL(s.values)
    case s: JString => s.values
    case d: JDouble => d.values.asInstanceOf[AnyRef]
    case i: JInt => i.values.intValue().asInstanceOf[AnyRef]
    case b: JBool => b.values.asInstanceOf[AnyRef]
    case o: JObject if field.isOid => new ObjectId(o.values.getOrElse("$oid",
      error("fromJsonTranform0: unexpected OID input class='%s', value='%s'".format(o.getClass.getName, o.values))).
      toString)
    case JsonAST.JNull => null
    case x: AnyRef     => error("Unsupported JSON transformation for class='%s', value='%s'".format(x.getClass.getName, x))
  }

  protected[salat] def jsonTranform0(o: Any) = o match {
    case s: String => JString(s)
    case d: Double => JDouble(d)
    case i: Int => JInt(i)
    case b: Boolean => JBool(b)
    case d: Date => ctx.jsonConfig.dateStrategy.out(d)
    case d: DateTime => ctx.jsonConfig.dateStrategy.out(d)
    case o: ObjectId => JObject(List(JField("$oid", JString(o.toString))))
    case u: java.net.URL => JString(u.toString) // might as well
    case n if n == null && ctx.jsonConfig.outputNullValues => JNull
    case x: AnyRef => error("Unsupported JSON transformation for class='%s', value='%s'".format(x.getClass.getName, x))
  }

  def toJSON(o: X) = {
    val builder = List.newBuilder[JField]
    if (useTypeHint) {
      val field: JValue = if (ctx.typeHintStrategy.isInstanceOf[StringTypeHintStrategy]) {
        JString(clazz.getName)
      }
      else error("toJSON: unsupported type hint strategy '%s'".format(ctx.typeHintStrategy))
      builder += JField(ctx.typeHintStrategy.typeHint, field)
    }

    iterateOut(o) {
      case (key, value) => {
        //        log.debug("ADDING: k='%s', v='%s'", key, value)
        builder += JField(key, jsonTranform(value))
      }
    }.toList

    JObject(builder.result())
  }

  def asDBObject(o: X): DBObject = {
    val builder = MongoDBObject.newBuilder

    // handle type hinting, where necessary
    if (useTypeHint) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(clazz.getName)
    }

    iterateOut(o) {
      case (key, value) => builder += key -> value
    }.toList

    builder.result()
  }

  def asObject[A <% MongoDBObject](dbo: A): X =
    if (sym.isModule) {
      companionObject.asInstanceOf[X]
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
      constructor.newInstance(args: _*)
    }
    catch {
      // when something bad happens feeding args into constructor, catch these exceptions and
      // wrap them in a custom exception that will provide detailed information about what's happening.
      case e: InstantiationException    => throw ToObjectGlitch(this, sym, constructor, args, e)
      case e: IllegalAccessException    => throw ToObjectGlitch(this, sym, constructor, args, e)
      case e: IllegalArgumentException  => throw ToObjectGlitch(this, sym, constructor, args, e)
      case e: InvocationTargetException => throw ToObjectGlitch(this, sym, constructor, args, e)
      case e                            => throw e
    }
  }

  def fromJSON(j: JObject) = {
    val values = j.obj.map(v => (v.name, v.value)).toMap
    val args = indexedFields.map {
      case field if field.ignore => safeDefault(field)
      case field => {
        val name = cachedFieldName(field)
        val rawValue = values.get(name)
        val value = fromJsonTransform(rawValue, field)
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

        value.flatMap(field.in_!(_)) orElse safeDefault(field)
      }
    }
    feedArgsToConstructor(args.flatten.asInstanceOf[Seq[AnyRef]])
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

  override def hashCode = sym.path.hashCode

  protected lazy val defaults: Seq[Option[Method]] = indexedFields.map {
    field =>
      try {
        Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
      }
      catch {
        case _ => None
      }
  }

  def defaultArg(field: SField): DefaultArg = {
    if (betterDefaults.contains(field)) {
      betterDefaults(field)
    }
    else error("Grater error: clazz='%s' field '%s' needs to register presence or absence of default values".
      format(clazz, field.name))
  }

  protected[salat] def safeDefault(field: SField) = {
    defaultArg(field).safeValue
  }

  protected[salat] lazy val betterDefaults = {
    val builder = Map.newBuilder[SField, DefaultArg]
    for (field <- indexedFields) {
      val defaultMethod = try {
        // Some(null) is actually "desirable" here because it allows using null as a default value for an ignored field
        Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)).invoke(companionObject))
      }
      catch {
        case _ => None // indicates no default value was supplied
      }

      builder += field -> DefaultArg(clazz, field, defaultMethod)
    }
    // pad out with extra fields to persist
    extraFieldsToPersist.foreach(f => builder += f._2 -> DefaultArg(clazz, f._2, None))
    builder.result()
  }
}

case class DefaultArg(clazz: Class[_], field: SField, value: Option[AnyRef])(implicit val ctx: Context) {

  def suppress(serialized: Any) = if (ctx.suppressDefaultArgs && field.name != "_id") {
    value.exists {
      v =>
        serialized match {
          case serialized: BasicDBList if serialized.isEmpty && isEmptyTraversable => true
          case serialized: BasicDBObject if serialized.isEmpty && isEmptyMap => true
          case serialized => serialized == v
        }
    }
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
