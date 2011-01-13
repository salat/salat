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

import java.lang.reflect.Method

import scala.math.{BigDecimal => ScalaBigDecimal}
import scala.tools.scalap.scalax.rules.scalasig._

import com.novus.salat.transformers._
import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging

object Field {
  def apply(idx: Int, name: String, t: TypeRefType, method: Method)(implicit ctx: Context): Field = {
    val _in = in.select(t, method.annotated_?[Salat])
    val _out = out.select(t, method.annotated_?[Salat])

    new Field(idx, method.annotation[Key].map(_.value).getOrElse(name),
              t, _in, _out, method.annotation[Ignore].map(_ => true).getOrElse(false)) {}
  }
}

sealed abstract class Field(val idx: Int, val name: String, val typeRefType: TypeRefType,
                            val in: Transformer, val out: Transformer, val ignore: Boolean)(implicit val ctx: Context) extends Logging {
  def in_!(value: Any) = in.transform_!(value)
  def out_!(value: Any) = out.transform_!(value)
}
