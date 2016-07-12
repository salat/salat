/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-util
 * Class:         ClassAnalyzer.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */

package com.novus.salat

import java.lang.reflect.{Constructor, Method}

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util.{MissingExpectedType, MissingPickledSig, _}

import scala.tools.scalap.scalax.rules.scalasig.{MethodSymbol, NullaryMethodType, PolyType, TypeRefType, _}

object ClassAnalyzer extends Logging {

  val ModuleFieldName = "MODULE$"

  val ClassLoaders = Vector(this.getClass.getClassLoader)

  def findSym[A](clazz: Class[A], classLoaders: Iterable[ClassLoader]): SymbolInfoSymbol = {
    val _sig = ScalaSigUtil.parseScalaSig0(clazz, classLoaders)
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

  // annotations on a getter don't actually inherit from a trait or an abstract superclass,
  // but dragging them down manually allows for much nicer behaviour - this way you can specify @Persist or @Key
  // on a trait and have it work all the way down
  def interestingClass(clazz: Class[_]) = clazz match {
    case clazz if clazz == null => false // inconceivably, this happens!
    case clazz if clazz.getName.startsWith("java.") => false
    case clazz if clazz.getName.startsWith("javax.") => false
    case clazz if clazz.getName.startsWith("scala.") => false
    case clazz if clazz.getEnclosingClass != null => false // filter out nested traits and superclasses
    case _ => true
  }

  def findAnnotatedMethodSymbol[A](clazz: Class[A], annotation: Class[_ <: java.lang.annotation.Annotation], allTheChildren: Seq[Symbol]) = {
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
              case None     => sys.error("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
            }
        }
      }
  }

  //  def findAnnotatedFields[A](clazz: Class[A], annotation: Class[_ <: java.lang.annotation.Annotation]) = {
  //      clazz
  //        .getDeclaredMethods.toList
  //        .filter(_.isAnnotationPresent(annotation))
  //        .filterNot(m => m.annotated_?[Ignore])
  //        .map {
  //          case m: Method => m -> {
  //            log.trace("findAnnotatedFields: clazz=%s, m=%s", clazz, m.getName)
  //            // do use allTheChildren here: we want to pull down annotations from traits and/or superclass
  //            allTheChildren
  //              .filter(f => f.name == m.getName && f.isAccessor)
  //              .map(_.asInstanceOf[MethodSymbol])
  //              .headOption match {
  //                case Some(ms) => SField(-1, ms.name, typeRefType(ms), m) // TODO: -1 magic number for idx which is required but not used
  //                case None     => throw new RuntimeException("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
  //              }
  //          }
  //        }
  //    }

  def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _)                           => tr
    case NullaryMethodType(tr @ TypeRefType(_, _, _))                     => tr
    case NullaryMethodType(ExistentialType(tr @ TypeRefType(_, _, _), _)) => tr
  }

  def companionClass(clazz: Class[_], classLoaders: Iterable[ClassLoader]) = {
    val path = if (clazz.getName.endsWith("$")) clazz.getName else "%s$".format(clazz.getName)
    val c = resolveClass(path, classLoaders)
    if (c.isDefined) c.get else sys.error("Could not resolve clazz='%s'".format(path))
  }

  def companionObject(clazz: Class[_], classLoaders: Iterable[ClassLoader]) =
    companionClass(clazz, classLoaders).getField(ModuleFieldName).get(null)

}

case class ClassAnalyzer[A](
    clazz:        Class[A],
    classLoaders: Iterable[ClassLoader] = ClassAnalyzer.ClassLoaders
) extends Logging {

  import ClassAnalyzer._

  val sym = findSym(clazz, classLoaders)
  lazy val companionClass = ClassAnalyzer.companionClass(clazz, classLoaders)
  lazy val companionObject = ClassAnalyzer.companionObject(clazz, classLoaders)
  lazy val constructor: Constructor[A] = BestAvailableConstructor(clazz)

  lazy val interestingInterfaces: List[(Class[_], SymbolInfoSymbol)] = {
    val interfaces = clazz.getInterfaces // this should return an empty array, but...  sometimes returns null!
    if (interfaces != null && interfaces.nonEmpty) {
      interfaces.filter(interestingClass(_)).map {
        interface =>
          interface -> findSym(interface, classLoaders)
      }.toList
    }
    else Nil
  }

  lazy val interestingSuperclass: List[(Class[_], SymbolInfoSymbol)] = clazz.getSuperclass match {
    case superClazz if interestingClass(superClazz) => List((superClazz, findSym(superClazz, classLoaders)))
    case _ => Nil
  }

  lazy val requiresTypeHint = {
    clazz.annotated_?[Salat] ||
      interestingInterfaces.exists(_._1.annotated_?[Salat]) ||
      interestingSuperclass.exists(_._1.annotated_?[Salat])
  }

  // for use when you just want to find something and whether it was declared in clazz, some trait clazz extends, or clazz' own superclass
  // is not a concern
  lazy val allTheChildren = sym.children ++
    interestingInterfaces.map(_._2.children).flatten ++
    interestingSuperclass.map(_._2.children).flatten

  lazy val keyOverridesFromAbove = {
    val key = classOf[Key]
    val builder = Map.newBuilder[MethodSymbol, String]
    val annotated = interestingInterfaces.map(i => findAnnotatedMethodSymbol(i._1, key, allTheChildren)).flatten ++
      interestingSuperclass.map(i => findAnnotatedMethodSymbol(i._1, key, allTheChildren)).flatten
    for ((method, ms) <- annotated) {
      method.annotation[Key].map(_.value) match {
        case Some(key) => builder += ms -> key
        case None      =>
      }
    }
    builder.result
  }

  //  lazy val extraFieldsToPersist = {
  //      val persist = classOf[Persist]
  //      val fromClazz = findAnnotatedFields(clazz, persist)
  //      // not necessary to look directly on trait, is necessary to look directly on superclass
  //      val fromSuperclass = interestingSuperclass.flatMap(i => findAnnotatedFields(i._1, persist))
  //
  //      fromClazz ::: fromSuperclass
  //    }

}
