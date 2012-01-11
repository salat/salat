/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat

import com.novus.salat.util._

object `package` extends Logging {

  type CaseClass = AnyRef with Product

  val ModuleFieldName = "MODULE$"

  def timeAndLog[T](f: => T)(l: Long => Unit): T = {
    val t = System.currentTimeMillis
    val r = f
    l.apply(System.currentTimeMillis - t)
    r
  }

  implicit def class2companion(clazz: Class[_])(implicit ctx: Context) = new {
    def companionClass: Class[_] = {
      val path = if (clazz.getName.endsWith("$")) clazz.getName else "%s$".format(clazz.getName)
      getClassNamed(path).getOrElse {
        throw new Error("Could not resolve clazz='%s' in any of the %d classpaths in ctx='%s'".
          format(path, ctx.classLoaders.size, ctx.name))
      }
    }

    def companionObject = companionClass.getField(ModuleFieldName).get(null)
  }

  val TypeHint = "_typeHint"

  object TypeHintFrequency extends Enumeration {
    val Never, WhenNecessary, Always = Value
  }

  def grater[X <: CaseClass](implicit ctx: Context, m: Manifest[X]): Grater[X] = ctx.lookup[X](m)

  protected[salat] def getClassNamed_!(c: String)(implicit ctx: Context): Class[_] = getClassNamed(c)(ctx).getOrElse {
    throw new Error("getClassNamed: path='%s' does not resolve in any of %d classloaders registered with context='%s'".
      format(c, ctx.classLoaders.size, ctx.name))
  }

  protected[salat] def getClassNamed(c: String)(implicit ctx: Context): Option[Class[_]] = {
    resolveClass(c, ctx.classLoaders)
  }

  protected[salat] def isCaseClass(clazz: Class[_]) = clazz.getInterfaces.contains(classOf[Product])
}
