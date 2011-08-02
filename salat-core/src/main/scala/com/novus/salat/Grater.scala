/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 *
 */
package com.novus.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.novus.salat.{Field => SField}
import scala.reflect.generic.ByteCodecs
import scala.reflect.ScalaSignature

import java.lang.reflect.{InvocationTargetException, Constructor, Method}

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util._

import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging

abstract class Grater[X <: AnyRef](val clazz: Class[X])(implicit val ctx: Context) extends Logging {

  ctx.accept(this)

  def asDBObject(o: X): DBObject
  def asObject[A <% MongoDBObject](dbo: A): X
  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T]

  type OutHandler = PartialFunction[(Any, SField), Option[(String, Any)]]

  protected def outField: OutHandler = {
    case (_, field) if field.ignore => None
    case (null, _) => None
    case (element, field) => {
      field.out_!(element) match {
        case Some(None) => None
        case Some(serialized) => {
          val key = ctx.determineFieldName(clazz, field)
          val value = serialized match {
            case Some(unwrapped) => unwrapped
            case _ => serialized
          }
          Some(key -> value)
        }

        case _ => None
      }
    }
  }

  override def toString = "%s(%s @ %s)".format(getClass.getSimpleName, clazz, ctx)
  override def equals(that: Any) = that.isInstanceOf[Grater[_]] && that.hashCode == this.hashCode
}

abstract class ConcreteGrater[X <: CaseClass](clazz: Class[X])(implicit ctx: Context) extends Grater[X](clazz)(ctx) {

  protected def findSym[A](clazz: Class[A]) = {
    ScalaSigUtil.parseScalaSig0(clazz).
      map(x => x.topLevelClasses.headOption.
      getOrElse(x.topLevelObjects.headOption.
      getOrElse(throw MissingExpectedType(clazz)))
    ).getOrElse(throw MissingPickledSig(clazz))
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

  protected lazy val requiresTypeHint = {
    clazz.annotated_?[Salat] || interestingInterfaces.map(_._1.annotated_?[Salat]).contains(true) || interestingSuperclass.map(_._1.annotated_?[Salat]).contains(true)
  }

  // for use when you just want to find something and whether it was declared in clazz, some trait clazz extends, or clazz' own superclass
  // is not a concern
  protected lazy val allTheChildren: Seq[Symbol] = sym.children ++ interestingInterfaces.map(_._2.children).flatten ++ interestingSuperclass.map(_._2.children).flatten

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
          case None => throw new RuntimeException("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
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
          case None => throw new RuntimeException("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
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
        case None =>
      }
    }
    builder.result
  }

  protected lazy val companionClass = clazz.companionClass
  protected lazy val companionObject = clazz.companionObject


  protected lazy val constructor: Constructor[X] = BestAvailableConstructor(clazz)

  protected lazy val defaults: Seq[Option[Method]] = indexedFields.map {
    field => try {
      Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
    } catch {
      case _ => None
    }
  }

  protected def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr@TypeRefType(_, _, _), _) => tr
  }

  def iterateOut[T](o: X)(f: ((String, Any)) => T): Iterator[T] = {
    val fromConstructor = o.productIterator.zip(indexedFields.iterator)
    val withPersist = extraFieldsToPersist.iterator.map {
      case (m, field) => m.invoke(o) -> field
    }
    (fromConstructor ++ withPersist).map(outField).filter(_.isDefined).map(_.get).map(f)
  }

  def asDBObject(o: X): DBObject = {
    val builder = MongoDBObject.newBuilder

    // handle type hinting, where necessary
    if (ctx.typeHintStrategy.when == TypeHintFrequency.Always ||
      (ctx.typeHintStrategy.when == TypeHintFrequency.WhenNecessary && requiresTypeHint)) {
      builder += ctx.typeHintStrategy.typeHint -> ctx.typeHintStrategy.encode(clazz.getName)
    }

    iterateOut(o) {
      case (key, value) => builder += key -> value
    }.toList

    builder.result
  }

  protected def safeDefault(field: SField) =
    defaults(field.idx).map(m => Some(m.invoke(companionObject))).getOrElse(None) match {
      case yes@Some(default) => yes
      case _ => field.typeRefType match {
        case IsOption(_) => Some(None)
        case _ => throw new Exception("%s requires value for '%s'".format(clazz, field.name))
      }
    }

  def asObject[A <% MongoDBObject](dbo: A): X =
    if (sym.isModule) {
      companionObject.asInstanceOf[X]
    }
    else {
      val args = indexedFields.map {
        case field if field.ignore => safeDefault(field)
        case field => {
          dbo.get(ctx.determineFieldName(clazz, field)) match {
            case Some(value) => {
              field.in_!(value)
            }
            case _ => safeDefault(field)
          }
        }
      }.map(_.get.asInstanceOf[AnyRef]) // TODO: if raw get blows up, throw a more informative error

      try {
        constructor.newInstance(args: _*)
      }
      catch {
        // when something bad happens feeding args into constructor, catch these exceptions and
        // wrap them in a custom exception that will provide detailed information about what's happening.
        case e: InstantiationException => throw ToObjectGlitch(this, sym, constructor, args, e)
        case e: IllegalAccessException => throw ToObjectGlitch(this, sym, constructor, args, e)
        case e: IllegalArgumentException => throw ToObjectGlitch(this, sym, constructor, args, e)
        case e: InvocationTargetException => throw ToObjectGlitch(this, sym, constructor, args, e)
        case e => throw e
      }
    }

  override def hashCode = sym.path.hashCode
}
