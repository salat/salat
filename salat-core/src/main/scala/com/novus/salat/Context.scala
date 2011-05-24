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

import java.math.{RoundingMode, MathContext}
import scala.collection.mutable.{Map => MMap, HashMap}
import com.mongodb.casbah.commons.Logging
import com.mongodb.casbah.Imports._

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import java.lang.reflect.Modifier
import com.novus.salat.util.MissingGraterExplanation
import com.novus.salat.{Field => SField}

case class TypeHintStrategy(when: TypeHintFrequency.Value, typeHint: String = TypeHint) {
  assume(when != null, "Context requires non-null value for type hint strategy instead of %s!".format(when))
  assume(when == TypeHintFrequency.Never || (typeHint != null && typeHint.nonEmpty),
    "Type hint stratregy %s requires a type hint but you have supplied none!".format(when))
}

trait Context extends Logging {
  private[salat] val graters: MMap[String, Grater[_ <: CaseClass]] = HashMap.empty

  val name: Option[String]
  // TODO: make this an MSeq and private to [salat] - what on earth could I have been thinking here?
  implicit var classLoaders: Seq[ClassLoader] = Seq(getClass.getClassLoader)

  val typeHintStrategy: TypeHintStrategy = TypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)

  // sets up a default enum strategy of using toString to serialize/deserialize enums
  val defaultEnumStrategy = EnumStrategy.BY_VALUE
  // global @Key overrides - careful with that axe, Eugene
  private[salat] val globalKeyOverrides: MMap[String, String] = HashMap.empty
  // per-class key overrides - map key is (clazz.getName, field name)
  private[salat] val perClassKeyOverrides: MMap[(String, String), String] = HashMap.empty

  val mathCtx = new MathContext(17, RoundingMode.HALF_UP)

  def registerClassLoader(cl: ClassLoader): Unit = {
    // any explicitly-registered classloader is assumed to take priority over the boot time classloader
    classLoaders = (Seq.newBuilder[ClassLoader] += cl ++= classLoaders).result
    log.info("Context: registering classloader %d", classLoaders.size)
  }

  def determineFieldName(clazz: Class[_], field: SField): String = {
    assume(field.name != null && field.name.nonEmpty, "determineFieldName: bad candy field=%s".format(field))

    globalKeyOverrides.get(field.name).
      getOrElse(perClassKeyOverrides.get((clazz.getName, field.name)).
      getOrElse(field.name))
  }

  def registerPerClassKeyOverride(clazz: Class[_], remapThis: String, toThisInstead: String) {
    // for obvious reasons, we are not allowing a key override to be registered more than once
    assume(!perClassKeyOverrides.contains((clazz.getName, remapThis)), "registerPerClassKeyOverride: context=%s already has a global key override for clazz='%s'/key='%s' with value='%s'"
      .format(name, clazz.getName, remapThis, perClassKeyOverrides.get((clazz.getName, remapThis))))
    // think twice, register once
    assume(remapThis != null && remapThis.nonEmpty, "registerPerClassKeyOverride: clazz='%s', key remapThis must be supplied!".format(clazz.getName))
    assume(toThisInstead != null && toThisInstead.nonEmpty,
      "registerPerClassKeyOverride: clazz='%s', key remapThis='%s' - value toThisInstead must be supplied!".format(clazz.getName, remapThis))
    perClassKeyOverrides += (clazz.getName, remapThis) -> toThisInstead
    log.info("perClassKeyOverrides: context=%s will remap key='%s' to '%s' for all instance of clazz='%s'", name, remapThis, toThisInstead, clazz.getName)
  }

  def registerGlobalKeyOverride(remapThis: String, toThisInstead: String) {
    // for obvious reasons, we are not allowing a key override to be registered more than once
    assume(!globalKeyOverrides.contains(remapThis), "registerGlobalKeyOverride: context=%s already has a global key override for key='%s' with value='%s'"
      .format(name, remapThis, globalKeyOverrides.get(remapThis)))
    // think twice, register once
    assume(remapThis != null && remapThis.nonEmpty, "registerGlobalKeyOverride: key remapThis must be supplied!")
    assume(toThisInstead != null && toThisInstead.nonEmpty, "registerGlobalKeyOverride: value toThisInstead must be supplied!")
    globalKeyOverrides += remapThis -> toThisInstead
    log.info("registerGlobalKeyOverride: context=%s will globally remap key='%s' to '%s'", name, remapThis, toThisInstead)
  }

  def accept(grater: Grater[_ <: CaseClass]): Unit =
    if (!graters.contains(grater.clazz.getName)) {
      graters += grater.clazz.getName -> grater
      log.info("Context(%s) accepted %s", name.getOrElse("<no name>"), grater)
    }

  // TODO: This check needs to be a little bit less naive. There are
  // other types (Joda Time, anyone?) that are either directly
  // interoperable with MongoDB, or are handled by Casbah's BSON
  // encoders.
  protected def suitable_?(clazz: String): Boolean = {
    val s = !(clazz.startsWith("scala.") ||
      clazz.startsWith("java.") ||
      clazz.startsWith("javax.")) ||
      getClassNamed(clazz)(this).map(_.annotated_?[Salat]).getOrElse(false)
//    log.info("suitable_?: clazz=%s, suitable=%s", clazz, s)
    s
  }


  protected def suitable_?(clazz: Class[_]): Boolean = suitable_?(clazz.getName)

  protected def generate_?(c: String): Option[Grater[_ <: CaseClass]] = {
    if (suitable_?(c)) {
      val cc = getCaseClass(c)(this)
//      log.info("generate_?: c=%s, case class=%s", c, cc.getOrElse("[NOT FOUND]"))
      cc match {
        case  Some(clazz) if (clazz.isInterface) => {
//          log.warning("generate_?: clazz=%s is interface, no grater found", clazz)
          None
        }
        case Some(clazz) if Modifier.isAbstract(clazz.getModifiers()) => {
//          log.warning("generate_?: clazz=%s is abstract, no grater found", clazz)
          None
        }
        case Some(clazz) => {
//          log.info("generate_?: creating Grater[CaseClass] for clazz=%s", clazz)
          Some({ new ConcreteGrater[CaseClass](clazz)(this) {} }.asInstanceOf[Grater[CaseClass]])
        }
        case unknown => {
//          log.warning("generate_?: no idea what to do with cc=%s", unknown)
          None
        }
      }
    }
    else None
  }

  protected def generate(clazz: String): Grater[_ <: CaseClass] = try {
    // if this blows up, we'll catch and rethrow with additional information
    val caseClass = getCaseClass(clazz)(this).map(_.asInstanceOf[Class[CaseClass]]).get
    val grater = new ConcreteGrater[CaseClass](caseClass)(this) {}
    grater.asInstanceOf[Grater[CaseClass]]
  }
  catch {
    case e => {
      log.error(e, "generate: failed on clazz='%s'".format(clazz))
      throw GraterGlitch(clazz)(this)
    }
  }

  def lookup(clazz: String): Option[Grater[_ <: CaseClass]] = graters.get(clazz) match {
    case yes @ Some(_) => yes
    case _ => generate_?(clazz)
  }

  def lookup_!(clazz: String): Grater[_ <: CaseClass] = lookup(clazz).getOrElse(generate(clazz))

  def lookup_![X <: CaseClass : Manifest]: Grater[X] =
    lookup_!(manifest[X].erasure.getName).asInstanceOf[Grater[X]]

  def extractTypeHint(dbo: MongoDBObject): Option[String] =
    if (dbo.underlying.isInstanceOf[BasicDBObject]) dbo.get(typeHintStrategy.typeHint) match {
      case Some(hint: String) => Some(hint)
      case _ => None
    } else None

  def lookup(x: CaseClass): Option[Grater[_ <: CaseClass]] = lookup(x.getClass.getName)

  def lookup(clazz: String, x: CaseClass): Option[Grater[_ <: CaseClass]] =
    lookup(clazz) match {
      case yes @ Some(grater) => yes
      case _ => lookup(x)
    }

  def lookup_!(clazz: String, x: CaseClass): Grater[_ <: CaseClass] =
    lookup(clazz, x).getOrElse(generate(x.getClass.getName))

  def lookup(clazz: String, dbo: MongoDBObject): Option[Grater[_ <: CaseClass]] =
    lookup(dbo) match {
      case yes @ Some(grater) => yes
      case _ => lookup(clazz)
    }

  def lookup(dbo: MongoDBObject): Option[Grater[_ <: CaseClass]] =
    extractTypeHint(dbo) match {
      case Some(hint: String) => graters.get(hint) match {
        case Some(g) => Some(g)
        case None => generate_?(hint)
      }
      case _ => None
    }

  def lookup_!(dbo: MongoDBObject): Grater[_ <: CaseClass] = {
    lookup(dbo).getOrElse(generate(extractTypeHint(dbo).getOrElse(throw MissingTypeHint(dbo)(this))))
  }
}

case class GraterFromDboGlitch(path: String, dbo: MongoDBObject)(implicit ctx: Context) extends Error(MissingGraterExplanation(path, dbo)(ctx))
case class GraterGlitch(path: String)(implicit ctx: Context) extends Error(MissingGraterExplanation(path)(ctx))
case class MissingTypeHint(dbo: MongoDBObject)(implicit ctx: Context) extends Error("""

 NO TYPE HINT FOUND!

 Expected type hint key: %s

 DBO:
 %s

 """.format(ctx.typeHintStrategy.typeHint, dbo.toString()))