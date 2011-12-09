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
package com.novus.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.novus.salat.{ Field => SField }

import java.lang.reflect.{ Constructor, Method }

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util._

import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging

// TODO: create companion object to serve as factory for grater creation - there
// is not reason for this logic to be wodged in Context

abstract class Grater[X <: AnyRef](val clazz: Class[X])(implicit val ctx: Context) extends Logging {

  ctx.accept(this)

  @deprecated("Use ctx.toDBObject instead") def asDBObject(o: X): DBObject

  @deprecated("Use ctx.fromDBObject instead") def asObject[A <% MongoDBObject](dbo: A): X

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T]

  type OutHandler = PartialFunction[(Any, SField), Option[(String, Any)]]

  override def toString = "%s(%s @ %s)".format(getClass.getSimpleName, clazz, ctx.name)

  override def equals(that: Any) = that.isInstanceOf[Grater[_]] && that.hashCode == this.hashCode

  protected[salat] lazy val requiresTypeHint = true

}

abstract class ConcreteGrater[X <: CaseClass](clazz: Class[X])(implicit ctx: Context, m: Manifest[X]) extends Grater[X](clazz)(ctx) {

  protected def findSym[A](clazz: Class[A]) = {
    ScalaSigUtil.parseScalaSig0(clazz, ctx.classLoaders).
      map(x => x.topLevelClasses.headOption.
        getOrElse(x.topLevelObjects.headOption.
          getOrElse(throw MissingExpectedType(clazz)))).
      getOrElse(throw MissingPickledSig(clazz))
  }

  protected[salat] lazy val sym = findSym(clazz)

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

  protected[salat] override lazy val requiresTypeHint = {
    clazz.annotated_?[Salat] ||
      interestingInterfaces.map(_._1.annotated_?[Salat]).contains(true) ||
      interestingSuperclass.map(_._1.annotated_?[Salat]).contains(true)
  }

  // for use when you just want to find something and whether it was declared in clazz, some trait clazz extends, or clazz' own superclass
  // is not a concern
  protected lazy val allTheChildren: Seq[Symbol] = sym.children ++ interestingInterfaces.map(_._2.children).flatten ++ interestingSuperclass.map(_._2.children).flatten

  protected def outField(in: (Any, SField)): Option[(String, Any)] = {
    val v: Option[(String, Any)] = in match {
      case (_, field) if field.ignore => {
        log.info("outField: field='%s', ignoring...", field.name)
        None
      }
      case (null, _) => {
        log.info("outField: field='%s', value is null")
        None
      }
      case (elem, field) => {
        val out = field.out_!(elem)
        log.info("outField:\nFIELD: %s\nELEM: %s\nOUT: %s", field.name, elem, out)
        out match {
          case Some(None) => None
          case Some(serialized) => {
            log.info("""
            
            field.name = '%s'
            value = %s  [%s]
            default = %s [%s]
            value == default? %s
            
                                """, field.name,
              serialized,
              serialized.asInstanceOf[AnyRef].getClass.getName,
              safeDefault(field),
              safeDefault(field).map(_.asInstanceOf[AnyRef].getClass.getName).getOrElse("N/A"),
              (safeDefault(field).map(dv => dv == serialized).getOrElse(false)))

            serialized match {
              case serialized if ctx.suppressDefaultArgs && defaultArg(field).suppress(serialized) => None
              case serialized => {
                val key = ctx.determineFieldName(clazz, field)
                val value = serialized match {
                  case Some(unwrapped) => unwrapped
                  case _               => serialized
                }
                Some(key -> value)
              }
            }
          }
          case x => {
            log.info("outField: field='%s', not sure what to do with value='%s', suppressing...", field.name, x)
            None
          }
        }
      }
    }

    log.info("outField: field='%s'\nIN: %s\nOUT: %s", in._2.name, in._1, v)
    v
  }

  //  protected def outField(in: (Any, SField)): Option[(String, Any)] = in match {
  //    case (_, field) if field.ignore => None
  //    case (null, _) => None
  //    case (element, field) => {
  //      field.out_!(element) match {
  //        case Some(None) => None
  //        case Some(serialized) => {
  //          log.info("""
  //
  //field.name = '%s'
  //value = %s  [%s]
  //default = %s [%s]
  //value == default? %s
  //
  //                    """, field.name,
  //            serialized,
  //            serialized.asInstanceOf[AnyRef].getClass.getName,
  //            safeDefault(field),
  //            safeDefault(field).map(_.asInstanceOf[AnyRef].getClass.getName).getOrElse("N/A"),
  //            (safeDefault(field).map(dv => dv == serialized).getOrElse(false)))
  //
  //          serialized match {
  //            case serialized if ctx.suppressDefaultArgs && defaultArg(field).suppress(serialized) => None
  //            case serialized => {
  //              val key = ctx.determineFieldName(clazz, field)
  //              val value = serialized match {
  //                case Some(unwrapped) => unwrapped
  //                case _ => serialized
  //              }
  //              Some(key -> value)
  //            }
  //          }
  //        }
  //        case _ => None
  //      }
  //    }
  //  }

  protected[salat] lazy val indexedFields = {
    // don't use allTheChildren here!  this is the indexed fields for clazz and clazz alone
    sym.children
      .filter(c => c.isCaseAccessor && !c.isPrivate)
      .map(_.asInstanceOf[MethodSymbol])
      .zipWithIndex
      .map {
        case (ms, idx) => {
          //        log.info("indexedFields: clazz=%s, ms=%s, idx=%s", clazz, ms, idx)
          SField(idx, keyOverridesFromAbove.get(ms).getOrElse(ms.name), typeRefType(ms), clazz.getMethod(ms.name))
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
    val fromSuperclass = interestingSuperclass.map(i => findAnnotatedFields(i._1, persist)).flatten

    fromClazz ++ fromSuperclass
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
  protected[salat] lazy val companionObject = clazz.companionObject

  protected[salat] lazy val constructor: Constructor[X] = BestAvailableConstructor(clazz)

  protected def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] = {
    val fromConstructor = o.productIterator.zip(indexedFields.iterator).toList
    val withPersist = extraFieldsToPersist.iterator.map {
      case (m, field) => m.invoke(o) -> field
    }.toList
    val list = fromConstructor ++ withPersist
    log.info("iterateOut: found %d\n%s", list.size, list.map(v => "K: %s\nV: %s".format(v._1, v._2.name)).mkString("\n"))
    val toOut = list.map {
      next =>

        val v = outField(next)
        log.info("next:\nIN: K=%s, V=%s\nOUT: K=%s V=%s\n", next._1, next._2.name, v.map(_._1).getOrElse(""), v.map(_._2).getOrElse(""))
        v
    }
    val flatten = toOut.flatten
    log.info("iterateOut: found %d\n%s", flatten.size, flatten.map(v => "K: %s\nV: %s".format(v._1, v._2)).mkString("\n"))
    flatten.map(f(_)).iterator
  }

  @deprecated("Use ctx.toDBObject instead") def asDBObject(o: X): DBObject = ctx.toDBObject[X](o)

  @deprecated("Use ctx.fromDBObject instead") def asObject[B <% MongoDBObject](dbo: B): X = {
    ctx.fromDBObject[X](unwrapDBObj(dbo))
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

  protected[salat] def defaultArg(field: SField): DefaultArg = {
    betterDefaults.get(field).getOrElse {
      throw new Exception("Grater error: clazz='%s' field '%s' needs to register presence or absence of default values".
        format(clazz, field.name))
    }
  }

  protected[salat] def safeDefault(field: SField) = {
    defaultArg(field).safeValue
  }

  protected[salat] lazy val betterDefaults = {
    val builder = Map.newBuilder[SField, DefaultArg]
    for (field <- indexedFields) {
      val defaultMethod = try {
        Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
      }
      catch {
        case _ => None // indicates no default value was supplied
      }
      builder += field -> DefaultArg(clazz, field, defaultMethod.map(m => Some(m.invoke(companionObject))).getOrElse(None))
    }
    // pad out with extra fields to persist
    extraFieldsToPersist.foreach(f => builder += f._2 -> DefaultArg(clazz, f._2, None))
    builder.result()
  }
}

case class DefaultArg(clazz: Class[_], field: SField, value: Option[AnyRef])(implicit val ctx: Context) {

  protected[salat] def suppress(serialized: Any) = if (ctx.suppressDefaultArgs && field.name != "_id") {
    value.map {
      v =>
        serialized match {
          case serialized: BasicDBList if serialized.isEmpty && isEmptyTraversable => true
          case serialized: BasicDBObject if serialized.isEmpty && isEmptyMap => true
          case serialized => serialized == v
        }
    }.getOrElse(false)
  }
  else false

  protected[salat] lazy val safeValue: Some[AnyRef] = value match {
    case v @ Some(_) => v
    case _ => field.typeRefType match {
      case IsOption(_) => Some(None)
      case _           => throw new Exception("%s requires value for '%s'".format(clazz, field.name))
    }
  }

  protected[salat] lazy val isEmptyMap = value match {
    case Some(m: Map[_, _]) if m.asInstanceOf[Map[_, _]].isEmpty => true
    case _ => false
  }

  protected[salat] lazy val isEmptyTraversable = value match {
    case Some(t: Traversable[_]) if t.asInstanceOf[Traversable[_]].isEmpty => true
    case _ => false
  }
}