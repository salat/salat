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
import com.mongodb.casbah.Imports._

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util._
import com.mongodb.casbah.commons.Logging
import java.lang.reflect.{InvocationTargetException, Constructor, Method}

class MissingPickledSig(clazz: Class[_]) extends Error("Failed to parse pickled Scala signature from: %s".format(clazz))
class MissingExpectedType(clazz: Class[_]) extends Error("Parsed pickled Scala signature, but no expected type found: %s"
                                                         .format(clazz))
class MissingTopLevelClass(clazz: Class[_]) extends Error("Parsed pickled scala signature but found no top level class for: %s"
                                                          .format(clazz))
class NestingGlitch(clazz: Class[_], owner: String, outer: String, inner: String) extends Error("Didn't find owner=%s, outer=%s, inner=%s in pickled scala sig for %s"
                                                                                                .format(owner, outer, inner, clazz))

abstract class Grater[X <: CaseClass](val clazz: Class[X])(implicit val ctx: Context) extends Logging {
  ctx.accept(this)

  protected def parseScalaSig[A](clazz: Class[A]): Option[ScalaSig] = {
    // TODO: provide a cogent explanation for the process that is going on here, document the fork logic on the wiki
    val firstPass = ScalaSigParser.parse(clazz)
    log.trace("parseScalaSig: FIRST PASS on %s\n%s", clazz, firstPass)
    firstPass match {
      case Some(x) => {
        log.trace("1")
        Some(x)
      }
      case None if clazz.getName.endsWith("$") => {
        log.trace("2")
        val clayy = Class.forName(clazz.getName.replaceFirst("\\$$", ""))
        val secondPass = ScalaSigParser.parse(clayy)
        log.trace("parseScalaSig: SECOND PASS on %s\n%s", clayy, secondPass)
        secondPass
      }
      case x => x
    }
  }

  protected def findSym[A](clazz: Class[A]) = {
    val pss = parseScalaSig(clazz)
    log.trace("parseScalaSig: clazz=%\n%s", clazz, pss)
    pss match {
      case Some(x) => {
        val topLevelClasses = x.topLevelClasses
        log.trace("parseScalaSig: found top-level classes %s", topLevelClasses)
        topLevelClasses.headOption match {
          case Some(tlc) => {
            log.trace("parseScalaSig: returning top-level class %s", tlc)
            tlc
          }
          case None => {
            val topLevelObjects = x.topLevelObjects
            log.trace("parseScalaSig: found top-level objects %s", topLevelObjects)
            topLevelObjects.headOption match {
              case Some(tlo) => {
                log.trace("parseScalaSig: returning top-level object %s", tlo)
                tlo
              }
              case _ => throw new MissingExpectedType(clazz)
            }
          }
        }
      }
      case None => throw new MissingPickledSig(clazz)
    }
  }

  protected lazy val sym = findSym(clazz)

  // annotations on a getter don't actually inherit from a trait or an abstract superclass,
  // but dragging them down manually allows for much nicer behaviour - this way you can specify @Persist or @Key
  // on a trait and have it work all the way down
  protected val IgnoreTheseInterfaces: List[Class[_]] = List(classOf[ScalaObject], classOf[Product], classOf[java.io.Serializable])
  protected val IgnoreThisSuperclass: List[Class[_]] = List(classOf[Object])
  protected lazy val interestingInterfaces: List[(Class[_], SymbolInfoSymbol)] = (clazz.getInterfaces.toList diff IgnoreTheseInterfaces).map {
    i => (i, findSym(i))
  }
  protected lazy val interestingSuperclass: List[(Class[_], SymbolInfoSymbol)] = (List[Class[_]](clazz.getSuperclass) diff IgnoreThisSuperclass).map {
    i => (i, findSym(i))
  }
  lazy val requiresTypeHint = {
    clazz.annotated_?[Salat] || interestingInterfaces.map(_._1.annotated_?[Salat]).contains(true) || interestingSuperclass.map(_._1.annotated_?[Salat]).contains(true)
  }

  // for use when you just want to find something and whether it was declared in clazz, some trait clazz extends, or clazz' own superclass
  // is not a concern
  protected lazy val allTheChildren: Seq[Symbol] = sym.children ++ interestingInterfaces.map(_._2.children).flatten ++ interestingSuperclass.map(_._2.children).flatten

  protected lazy val indexedFields = {
    // don't use allTheChildren here!  this is the indexed fields for clazz and clazz alone
    sym.children
    .filter(c => c.isCaseAccessor && !c.isPrivate)
    .map(_.asInstanceOf[MethodSymbol])
    .zipWithIndex
    .map {
      case (ms, idx) =>
        Field(idx, keyOverridesFromAbove.get(ms).getOrElse(ms.name), typeRefType(ms), clazz.getMethod(ms.name))
    }
  }

  def findAnnotatedMethodSymbol[A](clazz: Class[A], annotation: Class[_ <: java.lang.annotation.Annotation]) = {
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

  def findAnnotatedFields[A](clazz: Class[A], annotation: Class[_ <: java.lang.annotation.Annotation]) = {
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
          case Some(ms) => com.novus.salat.Field(-1, ms.name, typeRefType(ms), m) // TODO: -1 magic number for idx which is required but not used
          case None => throw new RuntimeException("Could not find ScalaSig method symbol for method=%s in clazz=%s".format(m.getName, clazz.getName))
        }
      }
    }
  }

  lazy val extraFieldsToPersist = {
    val persist = classOf[Persist]
    val fromClazz = findAnnotatedFields(clazz, persist)
    // not necessary to look directly on trait, is necessary to look directly on superclass
    val fromSuperclass = interestingSuperclass.map(i => findAnnotatedFields(i._1, persist)).flatten

    fromClazz ++ fromSuperclass
  }

  lazy val keyOverridesFromAbove = {
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

  lazy val companionClass = clazz.companionClass
  lazy val companionObject = clazz.companionObject


  protected lazy val constructor: Constructor[X] = {
    val cl = clazz.getConstructors.asInstanceOf[Array[Constructor[X]]]
    if (cl.size > 1) {  // shouldn't ever happen as case class by definition has only a single constructor
      throw new RuntimeException("constructor: clazz=%s, expected 1 constructor but found %d\n%s".format(clazz, cl.size, cl.mkString("\n")))
    }
    val c = cl.headOption.getOrElse(throw new MissingConstructor(sym))
//    log.trace("constructor: clazz=%s ---> constructor=%s", clazz, c)
    c
  }


  protected lazy val defaults: Seq[Option[Method]] = indexedFields.map {
    field => try {
      Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
    } catch {
      case _ => None
    }
  }

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

  type OutHandler = PartialFunction[(Any, Field), Option[(String, Any)]]
  protected def outField: OutHandler = {
    case (_, field) if field.ignore => None
    case (null, _) => None
    case (element, field) => {
      field.out_!(element) match {
        case Some(None) => None
        case Some(serialized) =>
          Some(ctx.keyOverrides.get(field.name).getOrElse(field.name) -> (serialized match {
            case Some(unwrapped) => unwrapped
            case _ => serialized
          }))
        case _ => None
      }
    }
  }

  def asDBObject(o: X): DBObject = {
    val builder = MongoDBObject.newBuilder
    ctx.typeHintStrategy match {
      case TypeHintStrategy(TypeHintFrequency.Always, hint) => builder += hint -> clazz.getName
      case TypeHintStrategy(TypeHintFrequency.WhenNecessary, hint) if requiresTypeHint => builder += hint -> clazz.getName
      case _ =>
    }

    iterateOut(o) {
      case (key, value) => builder += key -> value
    }.toList

    builder.result
  }

  protected def safeDefault(field: Field) =
    defaults(field.idx).map(m => Some(m.invoke(companionObject))).getOrElse(None) match {
      case yes @ Some(default) => yes
      case _ => field.typeRefType match {
        case IsOption(_) => Some(None)
        case _ => throw new Exception("%s requires value for '%s'".format(clazz, field.name))
      }
    }

  def asObject(dbo: MongoDBObject): X =
    if (sym.isModule) {
      companionObject.asInstanceOf[X]
    }
    else {
      val args = indexedFields.map {
        case field if field.ignore => safeDefault(field)
        case field => {
          dbo.get(ctx.keyOverrides.get(field.name).getOrElse(field.name)) match {
          case Some(value) => {
            field.in_!(value)
          }
          case _ => safeDefault(field)
          }
        }
      }.map(_.get.asInstanceOf[AnyRef])

      try {
        constructor.newInstance(args: _*)
      }
      catch {
        // when something bad happens feeding args into constructor, catch these exceptions and
        // wrap them in a custom exception that will provide detailed information about what's happening.
        case e: InstantiationException => throw new ToObjectGlitch(this, sym, constructor, args, e)
        case e: IllegalAccessException => throw new ToObjectGlitch(this, sym, constructor, args, e)
        case e: IllegalArgumentException => throw new ToObjectGlitch(this, sym, constructor, args, e)
        case e: InvocationTargetException => throw new ToObjectGlitch(this, sym, constructor, args, e)
        case e => throw e
      }
    }


  override def toString = "Grater(%s @ %s)".format(clazz, ctx)

  override def equals(that: Any) = that.isInstanceOf[Grater[_]] && that.hashCode == this.hashCode

  override def hashCode = sym.path.hashCode
}

class MissingConstructor(sym: SymbolInfoSymbol) extends Error("Couldn't find a constructor for %s".format(sym.path))
class ToObjectGlitch[X<:CaseClass](grater: Grater[X], sym: SymbolInfoSymbol, constructor: Constructor[X], args: Seq[AnyRef], cause: Throwable) extends Error(
  """

  %s

  %s toObject failed on:
  SYM: %s
  CONSTRUCTOR: %s
  ARGS:
  %s

  """.format(cause.getMessage, grater.toString, sym.path, constructor, ArgsPrettyPrinter(args)), cause)
