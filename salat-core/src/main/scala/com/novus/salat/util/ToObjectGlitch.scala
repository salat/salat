/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         ToObjectGlitch.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
package com.novus.salat.util

import java.lang.reflect.Constructor

import com.mongodb.casbah.commons.TypeImports._
import com.novus.salat._

import scala.tools.scalap.scalax.rules.scalasig.SymbolInfoSymbol

case class ToObjectGlitch[X <: AnyRef with Product](grater: ConcreteGrater[X], sym: SymbolInfoSymbol, constructor: Constructor[X], args: Seq[AnyRef], cause: Throwable) extends Error(
  """

  %s

  %s toObject failed on:
  SYM: %s
  %s

  """.format(
    cause.getMessage,
    grater.toString,
    sym.path,
    ConstructorInputPrettyPrinter(grater, args)
  ),
  cause
)

case class GraterFromDboGlitch(path: String, dbo: MongoDBObject)(implicit ctx: Context) extends SalatGlitch(MissingGraterExplanation(path, dbo)(ctx))

case class GraterGlitch(path: String)(implicit ctx: Context) extends SalatGlitch(MissingGraterExplanation(path)(ctx))

object MissingTypeHint {
  def apply(x: Any): MissingTypeHint = x match {
    case dbo: DBObject          => MissingTypeHint(scala.collection.JavaConversions.mapAsScalaMap(dbo.toMap))
    case mdbo: MongoDBObject    => MissingTypeHint(scala.collection.JavaConversions.mapAsScalaMap(mdbo.toMap))
    case m: java.util.Map[_, _] => MissingTypeHint(scala.collection.JavaConversions.mapAsScalaMap(m))
    case unknownThing           => sys.error("Help, can't find type error in clazz=%s\n%s".format(unknownThing.getClass.getName, unknownThing.toString))
  }
}

case class MissingTypeHint(m: Map[_, _])(implicit ctx: Context) extends Error("""

 NO TYPE HINT FOUND!

 Expected type hint key: %s

 MAP-LIKE:
 %s

 """.format(ctx.typeHintStrategy.typeHint, m.mkString("\n")))

case class EnumInflaterGlitch(clazz: Class[_], strategy: EnumStrategy, value: Any) extends SalatGlitch(
  "Not sure how to handle value='%s' as enum of class %s using strategy %s".format(value, clazz.getName, strategy)
)

case class EnumInflaterCompanionObjectGlitch(clazz: Class[_], missingMethod: String) extends SalatGlitch(s"required method '$missingMethod' missing from companion object for $clazz")
