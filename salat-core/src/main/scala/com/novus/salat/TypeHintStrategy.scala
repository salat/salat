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

import com.novus.salat.util.encoding.TypeHintEncoding
import com.novus.salat.util.ClassPrettyPrinter

// TODO: oof.  this is not OO design at its most graceful.  refactor it!

trait TypeHintStrategy {
  val when: TypeHintFrequency.Value
  val typeHint: String

  def encode(in: String): Any

  def decode(in: Any): String
}

object NeverTypeHint extends TypeHintStrategy {
  val when = TypeHintFrequency.Never
  val typeHint = "_"

  def encode(in: String) = null

  def decode(in: Any) = null
}

case class StringTypeHintStrategy(when: TypeHintFrequency.Value, typeHint: String = TypeHint) extends TypeHintStrategy {

  assume(when == TypeHintFrequency.Never || (typeHint != null && typeHint.nonEmpty),
    "Type hint stratregy '%s' requires a type hint but you have supplied none!".format(when))

  override def toString = when match {
    case TypeHintFrequency.Never => "StringTypeHintStrategy: when='%s'".format(when)
    case _ => "StringTypeHintStrategy: when='%s', typeHint='%s'".format(when, typeHint)
  }

  def decode(in: Any) = in match {
    case s: String if (s != null) => s
    case x => throw new Error("Can't encode supplied value '%s'".format(x))
  }

  def encode(in: String) = in
}

case class BinaryTypeHintStrategy(when: TypeHintFrequency.Value, typeHint: String = TypeHint, encoding: TypeHintEncoding = TypeHintEncoding.UsAsciiEncoding) extends TypeHintStrategy {
  assume(when == TypeHintFrequency.Never || (typeHint != null && typeHint.nonEmpty),
    "Type hint stratregy '%s' requires a type hint but you have supplied none!".format(when))

  override def toString = when match {
    case TypeHintFrequency.Never => "BinaryTypeHintStrategy: when='%s'".format(when)
    case _ => "BinaryTypeHintStrategy: when='%s', typeHint='%s', encoding='%s'".format(when, typeHint, encoding.toString)
  }

  def decode(in: Any) = in match {
    case bi: BigInt => encoding.format(encoding.decode(bi))
    case s: String => s // TODO: ??? for backwards compatibility or just a bad idea?
    case x => throw new Error("BinaryTypeHintStrategy: don't know what to do with in='%s' (%s)".format(x, ClassPrettyPrinter(x.asInstanceOf[AnyRef])))
  }

  def encode(in: String) = null
}

object IsAlways {
  def unapply(t: TypeHintStrategy): Option[String] = t.when match {
    case TypeHintFrequency.Always => Option(t.typeHint)
    case _ => None
  }
}

object IsWhenNecessary {
  def unapply(t: TypeHintStrategy): Option[String] = t.when match {
    case TypeHintFrequency.WhenNecessary => Some(t.typeHint)
    case _ => None
  }
}