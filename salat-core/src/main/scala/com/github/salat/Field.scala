/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         Field.scala
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
package com.github.salat

import java.lang.reflect.Method

import com.github.salat.annotations.raw._
import com.github.salat.annotations.util._
import com.github.salat.transformers._
import com.github.salat.util.Logging

import scala.tools.scalap.scalax.rules.scalasig._

object Field {
  def apply(idx: Int, name: String, t: TypeRefType, method: Method)(implicit ctx: Context): Field = {
    val _name = {
      val n = method.annotation[Key].map(_.value)
      if (n.isDefined) n.get else name
    }

    val customTransformer = ctx.customTransformers.get(t.symbol.path)

    val _in = if (customTransformer.isDefined) new CustomSerializer(customTransformer.get, t.symbol.path, t, ctx) else in.select(t, method.annotated_?[Salat])
    val _out = if (customTransformer.isDefined) new CustomDeserializer(customTransformer.get, t.symbol.path, t, ctx) else out.select(t, method.annotated_?[Salat])
    val ignore = method.annotation[Ignore].isDefined

    new Field(
      idx         = idx,
      name        = _name,
      typeRefType = t,
      in          = _in,
      out         = _out,
      ignore      = ignore
    ) {}
  }
}

sealed abstract class Field(
    val idx:         Int,
    val name:        String,
    val typeRefType: TypeRefType,
    val in:          Transformer,
    val out:         Transformer,
    val ignore:      Boolean
)(implicit val ctx: Context) extends Logging {

  def in_!(value: Any) = {
    val xformed = in.transform_!(value)
    //    log.debug(
    //      """
    //                    |IN:
    //                    |                name: %s
    //                    |         typeRefType:
    //                    |%s
    //                    |                  in:
    //                    |%s
    //                    |               value: %s
    //                    |      value.getClass: %s
    //                    |         transformed: %s
    //                    |
    //                  """.stripMargin, name, typeRefType, in match {
    //        case c: UseCustomTransformer[_, _] => c.toString
    //        case _                             => in.getClass.getInterfaces.mkString("\n")
    //      }, value, value.getClass.getName, xformed)
    xformed
  }

  def out_!(value: Any) = {
    val xformed = out.transform_!(value)
    //    log.debug(
    //      """
    //                    |OUT:
    //                    |                name: %s
    //                    |         typeRefType:
    //                    |%s
    //                    |                  out:
    //                    |%s
    //                    |               value: %s
    //                    |         transformed: %s
    //                    |
    //                  """.stripMargin, name, typeRefType, out match {
    //        case c: UseCustomTransformer[_, _] => c.toString
    //        case _                             => out.getClass.getInterfaces.mkString("\n")
    //      }, value, xformed)
    xformed
  }

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
