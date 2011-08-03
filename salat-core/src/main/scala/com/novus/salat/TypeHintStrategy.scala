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
import java.util.regex.Pattern
import scala.collection.mutable.ConcurrentMap
import scala.collection.JavaConversions.JConcurrentMapWrapper
import java.util.concurrent.ConcurrentHashMap
import com.novus.salat.util.{Logging, ClassPrettyPrinter}

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

case class BinaryTypeHintStrategy(when: TypeHintFrequency.Value, typeHint: String = TypeHint,
                                  encoding: TypeHintEncoding = TypeHintEncoding.UsAsciiEncoding) extends TypeHintStrategy with Logging {

  private val PossibleBigInt = Pattern.compile("^[-]?\\d+$")

  protected[salat] val toTypeHint: ConcurrentMap[String, BigInt] = JConcurrentMapWrapper(new ConcurrentHashMap[String, BigInt]())
  protected[salat] val fromTypeHint: ConcurrentMap[BigInt, String] = JConcurrentMapWrapper(new ConcurrentHashMap[BigInt, String]())

  assume(when == TypeHintFrequency.Never || (typeHint != null && typeHint.nonEmpty),
    "Type hint stratregy '%s' requires a type hint but you have supplied none!".format(when))

  override def toString = when match {
    case TypeHintFrequency.Never => "BinaryTypeHintStrategy: when='%s'".format(when)
    case _ => "BinaryTypeHintStrategy: when='%s', typeHint='%s', encoding='%s'".format(when, typeHint, encoding.toString)
  }

  protected[salat] def decodeAndMemoize(bi: BigInt) = {
    fromTypeHint.get(bi).getOrElse {
      val decoded = encoding.format(encoding.decode(bi))
      log.info("fromTypeHint: put %s ---> '%s'", bi, decoded)
      fromTypeHint.put(bi, decoded)
      if (!toTypeHint.contains(decoded)) {
        log.info("toTypeHint: put '%s' ---> %s", decoded, bi)
        toTypeHint.put(decoded, bi)
      }
      decoded
    }
  }

  def decode(in: Any) = in match {
    case b: org.bson.types.Binary => decodeAndMemoize(BigInt(b.getData)) // getData() returns byte[] of the bindata
    case ba: Array[Byte] => decodeAndMemoize(BigInt(ba))
    case bi: BigInt => decodeAndMemoize(bi)
//    case s: String if PossibleBigInt.matcher(s).matches() => encoding.format(encoding.decode(BigInt(s, 10)))  // TODO: hmmm, performance suckage
    case s: String => s // TODO: ??? for backwards compatibility or just a bad idea?
    case x => throw new Error("BinaryTypeHintStrategy: don't know what to do with in='%s' (%s)".format(x, ClassPrettyPrinter(x.asInstanceOf[AnyRef])))
  }

  // TODO: add a feature to choose whether or not case objects are encoded
  def encode(in: String) = {
//    if (in.endsWith("$")) in else encoding.encode(in)
    if (in.endsWith("$")) {
      in
    }
    else {
      toTypeHint.get(in).getOrElse {
        val encoded = encoding.encode(in)
        log.info("toTypeHint: put '%s' ---> %s", in, encoded)
        toTypeHint.put(in, encoded)
        if (!fromTypeHint.contains(encoded)) {
          log.info("fromTypeHint: put %s ---> '%s'", encoded, in)
          fromTypeHint.put(encoded, in)
        }
        encoded
      }.toByteArray
    }
  }
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