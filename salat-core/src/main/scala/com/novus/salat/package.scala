/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         package.scala
 * Last modified: 2012-12-06 22:51:54 EST
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */
package com.novus.salat

import com.novus.salat.util._
import java.math.{ RoundingMode, MathContext }

object `package` extends Logging {

  type CaseClass = AnyRef with Product

  val DefaultMathContext = new MathContext(17, RoundingMode.HALF_UP)

  def timeAndLog[T](f: => T)(l: Long => Unit): T = {
    val t = System.currentTimeMillis
    val r = f
    l.apply(System.currentTimeMillis - t)
    r
  }

  def timeAndLogNanos[T](f: => T)(l: Long => Unit): T = {
    val t = System.nanoTime()
    val r = f
    l.apply(System.nanoTime() - t)
    r
  }

  implicit def class2companion(clazz: Class[_])(implicit ctx: Context) = new {
    def companionClass: Class[_] = ClassAnalyzer.companionClass(clazz, ctx.classLoaders)

    def companionObject = ClassAnalyzer.companionObject(clazz, ctx.classLoaders)
  }

  val TypeHint = "_typeHint"

  object TypeHintFrequency extends Enumeration {
    val Never, WhenNecessary, Always = Value
  }

  def grater[Y <: AnyRef](implicit ctx: Context, m: Manifest[Y]): Grater[Y] = ctx.lookup(m.runtimeClass.getName).asInstanceOf[Grater[Y]]

  protected[salat] def getClassNamed_!(c: String)(implicit ctx: Context): Class[_] = {
    val clazz = getClassNamed(c)(ctx)
    if (clazz.isDefined) clazz.get else sys.error("getClassNamed: path='%s' does not resolve in any of %d classloaders registered with context='%s'".
      format(c, ctx.classLoaders.size, ctx.name))
  }

  protected[salat] def getClassNamed(c: String)(implicit ctx: Context): Option[Class[_]] = {
    resolveClass(c, ctx.classLoaders)
  }

  protected[salat] def isCaseClass(clazz: Class[_]) = {
    //log.debug("isCaseClass: clazz='%s'\nInterfaces:\n%s", clazz.getName, clazz.getInterfaces.map(_.getName).mkString("\n"))
    clazz.getInterfaces.contains(classOf[Product])
  }

  protected[salat] def isCaseObject(clazz: Class[_]): Boolean = clazz.getInterfaces.contains(classOf[Product]) &&
    clazz.getInterfaces.contains(classOf[ScalaObject]) && clazz.getName.endsWith("$")
}
