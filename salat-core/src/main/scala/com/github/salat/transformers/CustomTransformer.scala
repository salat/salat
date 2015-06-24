/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         CustomTransformer.scala
 * Last modified: 2015-06-23 20:48:17 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */

package com.github.salat.transformers

import com.github.salat.util.Logging

abstract class CustomTransformer[ModelObject <: AnyRef: Manifest, SerializedRepr <: AnyRef: Manifest]() extends Logging {

  final def in(value: Any): Any = {
    //    log.debug("%s\nin: value: %s", toString, value)
    value match {
      case Some(o: SerializedRepr) => deserialize(o)
      case o: SerializedRepr       => deserialize(o)
      case _                       => None
    }

  }
  final def out(value: Any): Option[SerializedRepr] = value match {
    case i: ModelObject => Option(serialize(i))
  }

  def path = manifest[ModelObject].runtimeClass.getName

  def deserialize(b: SerializedRepr): ModelObject

  def serialize(a: ModelObject): SerializedRepr

  val supportsGrater = manifest[SerializedRepr].runtimeClass.getName.endsWith("DBObject")

  override def toString = "CustomTransformer[ %s <-> %s ]".format(manifest[ModelObject].runtimeClass.getName, manifest[SerializedRepr].runtimeClass.getName)
}
