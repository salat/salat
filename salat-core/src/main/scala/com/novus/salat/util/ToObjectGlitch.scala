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
package com.novus.salat.util

import com.novus.salat._
import scala.tools.scalap.scalax.rules.scalasig.SymbolInfoSymbol
import java.lang.reflect.Constructor
import com.mongodb.casbah.commons.TypeImports._
import com.novus.salat.util.MissingGraterExplanation._

case class ToObjectGlitch[X <: AnyRef with Product](grater: Grater[X], sym: SymbolInfoSymbol, constructor: Constructor[X], args: Seq[AnyRef], cause: Throwable) extends Error(
  """

  %s

  %s toObject failed on:
  SYM: %s
  CONSTRUCTOR: %s
  ARGS:
  %s

  """.format(cause.getMessage, grater.toString, sym.path, constructor, ArgsPrettyPrinter(args)), cause)

case class GraterFromDboGlitch(path: String, dbo: MongoDBObject)(implicit ctx: Context) extends Error(MissingGraterExplanation(path, dbo)(ctx))
case class GraterGlitch(path: String)(implicit ctx: Context) extends Error(MissingGraterExplanation(path)(ctx))
case class MissingTypeHint(dbo: MongoDBObject)(implicit ctx: Context) extends Error("""

 NO TYPE HINT FOUND!

 Expected type hint key: %s

 DBO:
 %s

 """.format(ctx.typeHintStrategy.typeHint, dbo.toString()))

case class EnumInflaterGlitch(clazz: Class[_], strategy: EnumStrategy, value: Any) extends Error(
  "Not sure how to handle value='%s' as enum of class %s using strategy %s".format(value, clazz.getName, strategy))
