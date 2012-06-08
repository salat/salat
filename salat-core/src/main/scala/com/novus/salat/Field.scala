/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         Field.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
package com.novus.salat

import java.lang.reflect.Method

import scala.math.{ BigDecimal => ScalaBigDecimal }
import scala.tools.scalap.scalax.rules.scalasig._

import com.novus.salat.transformers._
import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._

import com.novus.salat.util.Logging

object Field {
  def apply(idx: Int, name: String, t: TypeRefType, method: Method)(implicit ctx: Context): Field = {
    val _name = {
      val n = method.annotation[Key].map(_.value)
      if (n.isDefined) n.get else name
    }
    val _in = in.select(t, method.annotated_?[Salat])
    val _out = out.select(t, method.annotated_?[Salat])
    val ignore = method.annotation[Ignore].isDefined

    new Field(idx = idx,
      name = _name,
      typeRefType = t,
      in = _in,
      out = _out,
      ignore = ignore) {}
  }
}

sealed abstract class Field(val idx: Int,
                            val name: String,
                            val typeRefType: TypeRefType,
                            val in: Transformer,
                            val out: Transformer,
                            val ignore: Boolean)(implicit val ctx: Context) extends Logging {

  def in_!(value: Any) = in.transform_!(value)
  def out_!(value: Any) = out.transform_!(value)

  lazy val tf = TypeFinder(typeRefType)

  //  override def toString = "Field[%d/%s]".format(idx, name)
  override def toString = """

  Field
  idx: %d
  name: '%s'
  typeRefType: %s
    prefix: %s
    symbol: %s
    typeArgs: %s

  """.format(idx, name, typeRefType,
    typeRefType.prefix, typeRefType.symbol, typeRefType.typeArgs)
}
