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
package com.novus.salat.transform

import com.novus.salat._
import scala.collection.mutable.{ Map => MMap }
import com.novus.salat.util.Logging
import scala.tools.scalap.scalax.rules.scalasig.TypeRefType

trait TransformerChain extends Logging {

  // tip of the hat to dpp in http://scala-programming-language.1934581.n4.nabble.com/quot-Empty-quot-PartialFunction-td2399351.html
  protected[transform] val emptyPf: PartialFunction[(String, TypeRefType, Context, Any), Any] = scala.collection.immutable.Map()

  protected[transform] val customTransformers = MMap.empty[String, PartialFunction[(String, TypeRefType, Context, Any), Any]]

  def register(label: String, f: PartialFunction[(String, TypeRefType, Context, Any), Any]) {
    customTransformers.put(label, f)
    log.info("register: ADDED transformer '%s'")
  }

  def unregister(label: String) {
    customTransformers.remove(label)
    log.info("unregister: REMOVED transformer '%s'", label)
  }

  def processCustom: PartialFunction[(String, TypeRefType, Context, Any), Any] = {
    // tip of the hat to http://scalaeveryday.com/?p=67 - note that 2.9 reduce delegates to reduceLeft
    customTransformers.values.reduceLeftOption(_ orElse _).getOrElse(emptyPf)
  }

  /**
   * Placeholder for transformations that are handled further down the stack by BSON.
   */
  def straightThrough: PartialFunction[(String, TypeRefType, Context, Any), Any] = {
    case (path, typeRefType, ctx, value) => value
  }

  def bigDecimalTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def bigIntTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def floatTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def charTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def enumTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def caseClassTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def traitLikeTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]
  def dateTimeTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]

  def valueTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any] = bigDecimalTransformer orElse
    bigIntTransformer orElse
    floatTransformer orElse
    charTransformer orElse
    enumTransformer orElse
    caseClassTransformer orElse
    traitLikeTransformer orElse
    dateTimeTransformer orElse
    straightThrough

  def optionTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]

  def mapTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]

  def traversableTransformer: PartialFunction[(String, TypeRefType, Context, Any), Any]

  def transform = optionTransformer orElse
    mapTransformer orElse
    traversableTransformer orElse
    valueTransformer

}