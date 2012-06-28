/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-util
 * Class:         util.scala
 * Last modified: 2012-04-28 20:34:21 EDT
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
package com.novus.salat.util

object `package` {

  val NonePlaceholder = "[None]"
  val MissingPlaceholder = "[!!! MISSING - VALUE INPUT TO CONSTRUCTOR WAS ALSO MISSING !!!]"
  val OptionalMissingPlaceholder = "[Missing, but unnecessary because input value was supplied]"
  val NullPlaceholder = "[Null]"
  val EmptyPlaceholder = "[Empty]"
  val QuestionPlaceholder = "[???]"

  val SalatThreads = new ThreadGroup("Salat")
  val DefaultSalatStackSize = 1024L * 1024

  def truncate(a: AnyRef, l: Int = 100): String = if (a != null) {
    val s = a.toString
    if (s != null && s.length > l) "%s...".format(s.substring(0, l)) else s
  }
  else NullPlaceholder

  def asyncSalat[T](f: => T): T = asyncSalat[T](DefaultSalatStackSize)(f)

  def asyncSalat[T](stackSize: Long)(f: => T): T = {
    var result: Either[Throwable, T] = Left(new Error("no reply back, boo"))
      def satisfy(r: Either[Throwable, Any]) {
        result = r.asInstanceOf[Either[Throwable, T]]
      }

    val th = new Thread(SalatThreads,
      new AsyncSalatRunnable(f)(satisfy _),
      "Salat-%d".format(System.nanoTime),
      stackSize)

    th.start
    var done = false
    while (!done) {
      try {
        th.join
        done = true
      }
      catch {
        case ie: InterruptedException => {}
      }
    }
    result.right.getOrElse(throw result.left.get)
  }

  // TODO: reflection.  i'm so ashamed.  but not so ashamed i wouldn't do it!
  def reflectFields(x: Any with Product): Map[Any, Any] = {
    val fieldNames: Map[Any, String] = {
      val builder = Map.newBuilder[Any, String]
      for (field: java.lang.reflect.Field <- x.getClass.getDeclaredFields) {
        field.setAccessible(true)
        builder += field.get(x) -> field.getName
      }
      builder.result
    }

    val builder = Map.newBuilder[Any, Any]
    for (v <- x.productIterator) {
      builder += fieldNames(v) -> v
    }
    builder.result
  }

  protected[salat] def resolveClass_!(c: String, classLoaders: Iterable[ClassLoader]): Class[_] = {
    val clazz = resolveClass(c, classLoaders)
    if (clazz.isDefined) clazz.get else sys.error("resolveClass: path='%s' does not resolve in any of %d available classloaders".format(c, classLoaders.size))
  }

  protected[salat] def toUsableClassName(clazz: String) = if (clazz.endsWith("$")) clazz.substring(0, clazz.size - 1) else clazz

  protected[salat] def resolveClass[X <: AnyRef](c: String, classLoaders: Iterable[ClassLoader]): Option[Class[X]] = classLoaders match {
    case Nil      => sys.error("resolveClass: expected 1+ classloaders but received empty list")
    case List(cl) => Some(Class.forName(c, true, cl).asInstanceOf[Class[X]])
    case many => {
      //    log.info("resolveClass(): looking for %s in %d classloaders", c, classLoaders.size)
      try {
        var clazz: Class[_] = null
        //      var count = 0
        val iter = many.iterator
        while (clazz == null && iter.hasNext) {
          try {
            clazz = Class.forName(c, true, iter.next())
          }
          catch {
            case e: ClassNotFoundException => // keep going, maybe it's in the next one
          }

          //        log.info("resolveClass: %s %s in classloader '%s' %d of %d", c, (if (clazz != null) "FOUND" else "NOT FOUND"), ctx.name.getOrElse("N/A"), count, ctx.classloaders.size)
          //        count += 1
        }

        if (clazz != null) Some(clazz.asInstanceOf[Class[X]]) else None
      }
      catch {
        case _ => None
      }
    }
  }
}
