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

trait Context extends Logging {
  private[salat] val graters: MMap[String, Grater[_ <: CaseClass]] = HashMap.empty

  val name: Option[String]
  val typeHint: Option[String] = Some(TypeHint)

  def accept(grater: Grater[_ <: CaseClass]): Unit =
    if (!graters.contains(grater.clazz.getName)) {
      graters += grater.clazz.getName -> grater
      log.trace("Context(%s) accepted Grater[%s]", name.getOrElse("<no name>"), grater.clazz)
    }

  // XXX: This check needs to be a little bit less naive. There are
  // other types (Joda Time, anyone?) that are either directly
  // interoperable with MongoDB, or are handled by Casbah's BSON
  // encoders.
  protected def suitable_?(clazz: String): Boolean =
     !(clazz.startsWith("scala.") || clazz.startsWith("java.") || clazz.startsWith("javax.")) || getClassNamed(clazz).map(_.annotated_?[Salat]).getOrElse(false)

  protected def suitable_?(clazz: Class[_]): Boolean = suitable_?(clazz.getName)

  protected def generate_?(c: String): Option[Grater[_ <: CaseClass]] =
    if (suitable_?(c)) {
      getCaseClass(c) match {
        case Some(clazz) =>
          if (clazz.isInterface) None
          else Some({ new Grater[CaseClass](clazz)(this) {} }.asInstanceOf[Grater[CaseClass]])
        case _ => None
      }
    } else None

  protected def generate(clazz: String): Grater[_ <: CaseClass] =
    { new Grater[CaseClass](getCaseClass(clazz).map(_.asInstanceOf[Class[CaseClass]]).get)(this) {} }.asInstanceOf[Grater[CaseClass]]

  def lookup(clazz: String): Option[Grater[_ <: CaseClass]] = graters.get(clazz) match {
    case yes @ Some(_) => yes
    case _ => generate_?(clazz)
  }

  def lookup_!(clazz: String): Grater[_ <: CaseClass] =
    lookup(clazz).getOrElse(generate(clazz))

  def lookup_![X <: CaseClass : Manifest]: Grater[X] =
    lookup_!(manifest[X].erasure.getName).asInstanceOf[Grater[X]]

  protected def extractTypeHint(dbo: MongoDBObject): Option[String] =
    if (dbo.underlying.isInstanceOf[BasicDBObject]) dbo.get(typeHint.getOrElse(TypeHint)) match {
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
      case Some(hint: String) => graters.get(hint)
      case _ => None
    }

  def lookup_!(dbo: MongoDBObject): Grater[_ <: CaseClass] =
    generate(extractTypeHint(dbo).getOrElse(throw new Exception("no type hint found")))
}