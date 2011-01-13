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
package com.novus.salat.transformers

import java.lang.reflect.Method
import java.math.MathContext

import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.global.mathCtx
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging

abstract class Transformer(val path: String, val t: TypeRefType)(implicit val ctx: Context) {
  def transform(value: Any): Any = value
  def before(value: Any): Option[Any] = Some(value)
  def after(value: Any): Option[Any] = Some(value)

  def transform_!(x: Any): Option[Any] =
    before(x) match {
      case Some(x) => after(transform(x))
      case _ => None
    }
}

trait InContextTransformer {
  self: Transformer =>
    val grater: Option[Grater[_ <: CaseClass]]
}


