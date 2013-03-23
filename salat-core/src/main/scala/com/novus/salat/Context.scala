/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         Context.scala
 * Last modified: 2012-12-06 22:51:54 EST
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

import com.mongodb.casbah.Imports._
import com.novus.salat.annotations.util._
import com.novus.salat.json.JSONConfig
import com.novus.salat.util._
import com.novus.salat.{ Field => SField }
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import org.json4s.JsonAST.JObject

trait Context extends ContextLifecycle with Logging {

  /**Name of the context */
  val name: String

  /**Concurrent hashmap of classname to Grater */
  private[salat] val graters: scala.collection.concurrent.Map[String, Grater[_ <: AnyRef]] = scala.collection.convert.Wrappers.JConcurrentMapWrapper(new ConcurrentHashMap[String, Grater[_ <: AnyRef]]())

  /**Mutable seq of classloaders */
  private[salat] var classLoaders: Vector[ClassLoader] = Vector(this.getClass.getClassLoader)

  /**Global key remapping - for instance, always serialize "id" to "_id" */
  private[salat] val globalKeyOverrides: scala.collection.concurrent.Map[String, String] = scala.collection.convert.Wrappers.JConcurrentMapWrapper(new ConcurrentHashMap[String, String]())

  /**Per class key overrides - map key is (clazz.getName, field name) */
  private[salat] val perClassKeyOverrides: scala.collection.concurrent.Map[(String, String), String] = scala.collection.convert.Wrappers.JConcurrentMapWrapper(new ConcurrentHashMap[(String, String), String]())

  val typeHintStrategy: TypeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = TypeHint)

  /**Enum handling strategy is defined at the context-level, but can be overridden at the individual enum level */
  val defaultEnumStrategy = EnumStrategy.BY_VALUE

  /**Don't serialize any field whose value matches the supplied default args */
  val suppressDefaultArgs: Boolean = false

  private[salat] val neverSuppressTheseFields = scala.collection.mutable.Set[String]("_id")

  def neverSuppressThisField(key: String) {
    neverSuppressTheseFields += key
    log.debug("neverSuppressThisField[%s]: never suppress field with key '%s'", name, key)
  }

  // BigDecimal handling strategy: binary vs double vs string
  val bigDecimalStrategy: BigDecimalStrategy = BigDecimalToDoubleStrategy()

  @deprecated("Use Context bigDecimalStrategy.mathCtx instead", "0.0.8") lazy val mathCtx = bigDecimalStrategy.mathCtx

  // BigInt handling strategy: binary vs int
  val bigIntStrategy: BigIntStrategy = BigIntToBinaryStrategy

  val jsonConfig: JSONConfig = JSONConfig()

  private[salat] val caseObjectOverrides = scala.collection.convert.Wrappers.JConcurrentMapWrapper(new ConcurrentHashMap[String, String]())
  private[salat] val resolveCaseObjectOverrides = scala.collection.convert.Wrappers.JConcurrentMapWrapper(new ConcurrentHashMap[(String, String), String]())
  private[salat] val caseObjectHierarchy = scala.collection.mutable.Set[String]()

  def registerCaseObjectOverride[A: Manifest, B <: A: Manifest](serializedValue: String) {

    val parentClazz = manifest[A].runtimeClass
    val caseObjectClazz = manifest[B].runtimeClass
    assume(!caseObjectOverrides.contains(caseObjectClazz.getName), "registerCaseObjectOverride: clazz='%s' already overriden with value '%s'".
      format(caseObjectClazz.getName, caseObjectOverrides.get(caseObjectClazz.getName)))

    caseObjectOverrides += caseObjectClazz.getName -> serializedValue
    resolveCaseObjectOverrides += (parentClazz.getName, serializedValue) -> caseObjectClazz.getName
    caseObjectHierarchy += parentClazz.getName
    log.info("registerCaseObjectOverride[%s]: %s <- %s will serialize as '%s'", name, parentClazz.getSimpleName, caseObjectClazz.getSimpleName, serializedValue)
  }

  def registerClassLoader(cl: ClassLoader) {
    classLoaders = cl +: classLoaders
    log.info("registerClassLoader: ctx='%s' registering classloader='%s' (total: %d)", name, cl.toString, classLoaders.size)
  }

  def determineFieldName(clazz: Class[_], field: SField): String = determineFieldName(clazz, field.name)

  def determineFieldName(clazz: Class[_], fieldName: String): String = {
    assume(fieldName != null && fieldName.nonEmpty, "determineFieldName: bad candy clazz='%s' field=%s".format(clazz.getName, fieldName))

    if (globalKeyOverrides.contains(fieldName)) {
      globalKeyOverrides(fieldName)
    }
    else if (perClassKeyOverrides.contains(clazz.getName, fieldName)) {
      perClassKeyOverrides(clazz.getName, fieldName)
    }
    else fieldName
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
      log.trace("accept: ctx='%s' accepted grater[%s]", name, grater.clazz.getName)
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

  protected[salat] def needsProxyGrater(clazz: Class[_]) = {
    val usedSalatAnnotation = clazz.annotated_?[com.novus.salat.annotations.raw.Salat]
    val isTrait = clazz.isInterface
    val isAbstractClazz = Modifier.isAbstract(clazz.getModifiers)
    val outcome = usedSalatAnnotation || isTrait || isAbstractClazz
    if (outcome) {
      log.trace("""

needsProxyGrater: clazz='%s'
  usedSalatAnnotation: %s
  isTrait: %s
  isAbstractClazz: %s
   OUTCOME: %s

    """, clazz.getName, usedSalatAnnotation, isTrait, isAbstractClazz, (outcome))
    }
    outcome
  }

  protected[salat] def lookup_?[X <: AnyRef](c: String): Option[Grater[_ <: AnyRef]] = {
    val g = graters.get(c)
    if (g.isDefined) {
      g
    }
    else {
      if (suitable_?(c)) {
        resolveClass(c, classLoaders) match {
          case Some(clazz) if needsProxyGrater(clazz) => {
            log.trace("lookup_?: creating proxy grater for clazz='%s'", clazz.getName)
            Some((new ProxyGrater(clazz.asInstanceOf[Class[X]])(this) {}).asInstanceOf[Grater[_ <: AnyRef]])
          }
          case Some(clazz) if isCaseClass(clazz) => {
            Some((new ConcreteGrater[CaseClass](clazz.asInstanceOf[Class[CaseClass]])(this) {}).asInstanceOf[Grater[_ <: AnyRef]])
          }
          case Some(clazz) if isValueClass(clazz) => {
            Some((new ProxyGrater(clazz.asInstanceOf[Class[X]])(this) {}).asInstanceOf[Grater[_ <: AnyRef]])
          }
          case _ => None
        }
      }
      else None
    }
  }

  def lookup(c: String): Grater[_ <: AnyRef] = {
    val g = lookup_?(c)
    if (g.isDefined) g.get else throw GraterGlitch(c)(this)
  }

  def lookup[A <: CaseClass: Manifest]: Grater[A] = lookup(manifest[A].runtimeClass.getName).asInstanceOf[Grater[A]]

  def lookup(c: String, clazz: CaseClass): Grater[_ <: AnyRef] = {
    val g = lookup_?(c)
    if (g.isDefined) g.get else lookup(clazz.getClass.getName)
  }

  def lookup_?(c: String, dbo: MongoDBObject): Option[Grater[_ <: AnyRef]] = {
    val g = lookup_?(c)
    if (g.isDefined) g else extractTypeHint(dbo).flatMap(lookup_?(_))
  }

  def lookup(m: Map[String, Any]): Grater[_ <: AnyRef] = {
    val g = extractTypeHint(m).map(lookup(_))
    if (g.isDefined) g.get else throw MissingTypeHint(m)(this)
  }

  def lookup(dbo: MongoDBObject): Grater[_ <: AnyRef] = {
    val g = extractTypeHint(dbo).map(lookup(_))
    if (g.isDefined) g.get else throw MissingTypeHint(dbo.iterator.toMap)(this)
  }

  @deprecated("Use lookup instead - will be removed for 0.0.9 release", "0.0.8") def lookup_!(dbo: MongoDBObject): Grater[_ <: AnyRef] = lookup(dbo)

  @deprecated("Use lookup instead - will be removed for 0.0.9 release", "0.0.8") def lookup_!(clazz: String, x: CaseClass): Grater[_ <: AnyRef] = lookup(clazz, x)

  @deprecated("Use lookup instead - will be removed for 0.0.9 release", "0.0.8") def lookup_![X <: CaseClass: Manifest]: Grater[X] = lookup[X]

  @deprecated("Use lookup instead - will be removed for 0.0.9 release", "0.0.8") def lookup_!(clazz: String): Grater[_ <: AnyRef] = lookup(clazz)

  def lookup(j: JObject): Grater[_ <: AnyRef] = {
    val g = extractTypeHint(j).map(lookup(_))
    if (g.isDefined) g.get else throw MissingTypeHint(j.values)(this)
  }

  def extractTypeHint(m: Map[String, Any]): Option[String] = {
    m.get(typeHintStrategy.typeHint).map(typeHintStrategy.decode(_)).filter {
      str =>
        resolveClass(str, classLoaders) match {
          case Some(clazz) if isCaseClass(clazz)  => true
          case Some(clazz) if isCaseObject(clazz) => true
          case Some(clazz) => {
            log.warning("""

    extractTypeHint: '%s' -> '%s'
      UNSUITABLE TYPE HINT!  Type hint must be a case class or a case object.

    MAP
    %s

                """, typeHintStrategy.typeHint, str, m.mkString("\n"))
            false
          }
          case _ => false
        }

    }
  }

  def extractTypeHint(dbo: MongoDBObject): Option[String] = {
    dbo.get(typeHintStrategy.typeHint).map(typeHintStrategy.decode(_)).filter {
      str =>
        resolveClass(str, classLoaders) match {
          case Some(clazz) if isCaseClass(clazz)  => true
          case Some(clazz) if isCaseObject(clazz) => true
          case Some(clazz) => {
            log.warning("""

extractTypeHint: '%s' -> '%s'
  UNSUITABLE TYPE HINT!  Type hint must be a case class or a case object.

DBO
%s

            """, typeHintStrategy.typeHint, str, dbo)
            false
          }
          case _ => false
        }

    }
  }

  def extractTypeHint(j: JObject): Option[String] = {
    // TODO: probably not the most idiomatic way to do this
    j.values.get(typeHintStrategy.typeHint).map(typeHintStrategy.decode(_))
  }
}

trait ContextLifecycle {
  self: Context =>

  def clearAllGraters() {
    log.info("clearGraters: clearing...")
    graters.clear()
  }

  def clearGrater(clazz: String): Option[Grater[_ <: AnyRef]] = {
    val g = graters.remove(clazz)
    log.debug("clearGrater: clazz='%s' - success=%s", clazz, g.isDefined)
    g
  }

  // TODO: better lifecycle handling - need a cheap and easy way to detect only changed pickled signatures
}