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

import java.math.{ RoundingMode => JRoundingMode, MathContext => JMathContext }
import scala.collection.mutable.{ ConcurrentMap }
import scala.collection.JavaConversions.JConcurrentMapWrapper
import scala.collection.JavaConversions.JCollectionWrapper

import com.novus.salat.util._
import com.novus.salat.{ Field => SField }
import com.novus.salat.annotations._
import com.novus.salat.annotations.util._
import java.lang.reflect.Modifier
import com.mongodb.casbah.commons.MongoDBObject
import java.util.concurrent.{ CopyOnWriteArrayList, ConcurrentHashMap }

trait Context extends Logging {

  /**Name of the context */
  val name: String

  /**Concurrent hashmap of classname to Grater */
  private[salat] val graters: ConcurrentMap[String, Grater[_ <: AnyRef]] = JConcurrentMapWrapper(new ConcurrentHashMap[String, Grater[_ <: AnyRef]]())

  /**Mutable seq of classloaders */
  private[salat] var classLoaders: Vector[ClassLoader] = Vector(this.getClass.getClassLoader)

  /**Global key remapping - for instance, always serialize "id" to "_id" */
  private[salat] val globalKeyOverrides: ConcurrentMap[String, String] = JConcurrentMapWrapper(new ConcurrentHashMap[String, String]())

  /**Per class key overrides - map key is (clazz.getName, field name) */
  private[salat] val perClassKeyOverrides: ConcurrentMap[(String, String), String] = JConcurrentMapWrapper(new ConcurrentHashMap[(String, String), String]())

  val typeHintStrategy: TypeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)

  /**Enum handling strategy is defined at the context-level, but can be overridden at the individual enum level */
  val defaultEnumStrategy = EnumStrategy.BY_VALUE

  val mathCtx = new JMathContext(17, JRoundingMode.HALF_UP)

  /**Don't serialize any field whose value matches the supplied default args */
  val suppressDefaultArgs: Boolean = false

  // TODO: BigDecimal handling strategy: binary vs double
  // TODO: BigInt handling strategy: binary vs int

  val diagnostics: ContextDiagnosticOptions = ContextDiagnosticOptions(logGraterCreation = true)

  def registerClassLoader(cl: ClassLoader) {
    classLoaders = cl +: classLoaders
    log.info("registerClassLoader: ctx='%s' registering classloader='%s' (total: %d)", name, cl.toString, classLoaders.size)
  }

  def determineFieldName(clazz: Class[_], field: SField): String = determineFieldName(clazz, field.name)

  def determineFieldName(clazz: Class[_], fieldName: String): String = {
    assume(fieldName != null && fieldName.nonEmpty, "determineFieldName: bad candy clazz='%s' field=%s".format(clazz.getName, fieldName))

    globalKeyOverrides.get(fieldName).
      getOrElse(perClassKeyOverrides.get((clazz.getName, fieldName)).
        getOrElse(fieldName))
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
    // think twice, register once
    assume(remapThis != null && remapThis.nonEmpty, "registerGlobalKeyOverride: key remapThis must be supplied!")
    assume(toThisInstead != null && toThisInstead.nonEmpty, "registerGlobalKeyOverride: value toThisInstead must be supplied!")
    // for obvious reasons, we are not allowing a key override to be registered more than once
    assume(!globalKeyOverrides.contains(remapThis), "registerGlobalKeyOverride: context=%s already has a global key override for key='%s' with value='%s'"
      .format(name, remapThis, globalKeyOverrides.get(remapThis)))
    globalKeyOverrides += remapThis -> toThisInstead
    log.info("registerGlobalKeyOverride: context=%s will globally remap key='%s' to '%s'", name, remapThis, toThisInstead)
  }

  def accept(grater: Grater[_ <: AnyRef]) {
    if (!graters.contains(grater.clazz.getName)) {
      graters += grater.clazz.getName -> grater
      //      log.info("Context(%s) accepted %s", name.getOrElse("<no name>"), grater)
      log.debug("accept: ctx='%s' accepted grater[%s]", name, grater.clazz.getName)
    }
  }

  // TODO: This check needs to be a little bit less naive. There are
  // other types (Joda Time, anyone?) that are either directly
  // interoperable with MongoDB, or are handled by Casbah's BSON
  // encoders.
  protected[salat] def suitable_?(clazz: String) = clazz match {
    case c if clazz.startsWith("scala.") => false
    case c if clazz.startsWith("java.")  => false
    case c if clazz.startsWith("javax.") => false
    //    case c if getClassNamed(c).map(_.getEnclosingClass != null).getOrElse(false) => false
    case _                               => true
  }

  private[salat] def lookup_?[X <: AnyRef](c: String): Option[Grater[_ <: AnyRef]] = graters.get(c) orElse {
    if (suitable_?(c)) {
      resolveClass(c, classLoaders) match {
        case IsCaseClass(clazz) => Some((new ConcreteGrater[CaseClass](clazz)(this) {}).
          asInstanceOf[Grater[_ <: AnyRef]])
        case Some(clazz) if Modifier.isAbstract(clazz.getModifiers) => Some((
          new ProxyGrater(clazz.asInstanceOf[Class[X]])(this) {}).
          asInstanceOf[Grater[_ <: AnyRef]])
        case Some(clazz) if clazz.isInterface => Some((
          new ProxyGrater(clazz.asInstanceOf[Class[X]])(this) {}).asInstanceOf[Grater[_ <: AnyRef]])
        case _ => None
      }
    }
    else None
  }

  def lookup(c: String): Grater[_ <: AnyRef] = lookup_?(c).getOrElse(throw GraterGlitch(c)(this))

  def lookup[A <: CaseClass: Manifest]: Grater[A] = lookup(manifest[A].erasure.getName).asInstanceOf[Grater[A]]

  def lookup(c: String, clazz: CaseClass): Grater[_ <: AnyRef] = lookup_?(c).getOrElse(lookup(clazz.getClass.getName))

  def lookup_?(c: String, dbo: MongoDBObject): Option[Grater[_ <: AnyRef]] = 
    lookup_?(c) orElse extractTypeHint(dbo).flatMap(lookup_?(_))

  def lookup(dbo: MongoDBObject): Grater[_ <: AnyRef] = extractTypeHint(dbo).map(lookup(_)).getOrElse(throw MissingTypeHint(dbo)(this))

  def extractTypeHint(dbo: MongoDBObject): Option[String] = {
    dbo.get(typeHintStrategy.typeHint).map(typeHintStrategy.decode(_))
  }
}

case class ContextDiagnosticOptions(logGraterCreation: Boolean = false)
