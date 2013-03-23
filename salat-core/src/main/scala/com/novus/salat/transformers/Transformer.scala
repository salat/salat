/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         Transformer.scala
 * Last modified: 2012-10-15 20:40:59 EDT
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
package com.novus.salat.transformers

import com.novus.salat._
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{ BigDecimal => ScalaBigDecimal }

object `package` {
  def isBigDecimal(path: String) = path match {
    case "scala.math.BigDecimal"    => true
    case "scala.package.BigDecimal" => true
    case "scala.Predef.BigDecimal"  => true
    case "scala.BigDecimal"         => true
    case _                          => false
  }

  def isFloat(path: String) = path match {
    case "scala.Float"     => true
    case "java.lang.Float" => true
    case _                 => false
  }

  def isChar(path: String) = path match {
    case "scala.Char"          => true
    case "java.lang.Character" => true
    case _                     => false
  }

  def isBigInt(path: String) = path match {
    case "scala.package.BigInt" => true
    case "scala.math.BigInt"    => true
    case "java.math.BigInteger" => true
    case _                      => false
  }

  def isJodaDateTime(path: String) = path match {
    case "org.joda.time.DateTime" => true
    case "org.scala_tools.time.TypeImports.DateTime" => true
    case _ => false
  }

  def isJodaDateTimeZone(path: String) = path match {
    case "org.joda.time.DateTimeZone" => true
    case "org.scala_tools.time.TypeImports.DateTimeZone" => true
    case _ => false
  }

  def isInt(path: String) = path match {
    case "java.lang.Integer" => true
    case "scala.Int"         => true
    case _                   => false
  }
}

abstract class Transformer(val path: String, val t: TypeRefType, val resolveKey: Boolean = true)(implicit val ctx: Context) {
  def transform(value: Any)(implicit ctx: Context): Any = value
  def before(value: Any)(implicit ctx: Context): Option[Any] = Some(value)
  def after(value: Any)(implicit ctx: Context): Option[Any] = Some(value)
  def transform_!(x: Any)(implicit ctx: Context): Option[Any] = before(x).flatMap(x => after(transform(x)))
}

trait InContextTransformer {
  self: Transformer =>
  val grater: Option[Grater[_ <: AnyRef]]
}

abstract class UseCustomTransformer[A <: AnyRef, B <: AnyRef](val custom: CustomTransformer[A, B], override val path: String, override val t: TypeRefType, override val ctx: Context) extends Transformer(path, t)(ctx) {
  override def toString = "UseCustomTransformer: %s".format(custom.toString)
}

class CustomSerializer[A <: AnyRef, B <: AnyRef](override val custom: CustomTransformer[A, B], override val path: String, override val t: TypeRefType, override val ctx: Context) extends UseCustomTransformer(custom, path, t, ctx) {
  override def transform(value: Any)(implicit ctx: Context) = custom.in(value)
}

class CustomDeserializer[A <: AnyRef, B <: AnyRef](override val custom: CustomTransformer[A, B], override val path: String, override val t: TypeRefType, override val ctx: Context) extends UseCustomTransformer(custom, path, t, ctx) {
  override def transform(value: Any)(implicit ctx: Context) = custom.out(value)
}

