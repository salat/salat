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

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.Logging
import java.lang.reflect.Modifier

package object util {

  val NonePlaceholder = "[None]"
  val NullPlaceholder = "[Null]"
  val EmptyPlaceholder = "[Empty]"

  object MissingGraterExplanation extends Logging {

    def clazzFromTypeHint(typeHint: Option[String])(implicit ctx: Context): Option[Class[_]] = typeHint match {
      case Some(typeHint) => try {
        Some(Class.forName(typeHint))
      }
      catch {
        case c: ClassNotFoundException => None
        case e => {
          log.error("Type hint from dbo with key='%s', value='%s' is some bad candy!".format(ctx.typeHintStrategy.typeHint, typeHint),
            e)
          None
        }
      }
    }

    def clazzFromPath(path: String): Option[Class[_]] = if (path != null) {
      try {
        Some(Class.forName(path))
      }
      catch {
        case c: ClassNotFoundException => None
        case e => {
          log.error("Error resolving class from path='%s'".format(path),
            e)
          None
        }
      }
    }
    else None

    def getReasonForPathClazz(pathClazz: Option[Class[_]], path: String): String = {
      pathClazz match {
        case Some(clazz) if clazz.isInstanceOf[CaseClass] => "Well, this is a case class, so... not sure what went wrong."
        case Some(clazz) if clazz.isInterface => "Class %s is an interface".format(clazz.getName)
        case Some(clazz) if Modifier.isAbstract(clazz.getModifiers) => "Class %s is abstract".format(clazz.getName)
        case Some(clazz) if !clazz.isInstanceOf[CaseClass] => "Class %s is not an instance of CaseClass".format(clazz.getName)
        case None if path != null && pathClazz.isEmpty => "Very strange!  Path='%s' from pickled ScalaSig causes ClassNotFoundException".
          format(path)
        case _ => "Unable to determine why grater was not found"
      }
    }

    def apply(path: String)(implicit ctx: Context) = {
      val pathClazz = clazzFromPath(path)
      val reason = getReasonForPathClazz(pathClazz, path)

      val explanation = """

      GRATER GLITCH - unable to find or instantiate a grater using supplied path name

      REASON: %s

      Context: '%s'
      Path from pickled Scala sig: '%s'


       """.format(reason, ctx.name.getOrElse("No name supplied"), path)

      explanation
    }

    def apply(path: String, dbo: MongoDBObject)(implicit ctx: Context) = {

      val typeHint = ctx.extractTypeHint(dbo)
      val hintClazz = clazzFromTypeHint(typeHint)(ctx)
      val pathClazz = clazzFromPath(path)
      val reason = hintClazz match {
        case Some(clazz) if clazz.isInstanceOf[CaseClass] => "Well, this is a case class, so... not sure what went wrong."
        case Some(clazz) if clazz.isInterface => "Class %s is an interface".format(clazz.getName)
        case Some(clazz) if Modifier.isAbstract(clazz.getModifiers) => "Class %s is abstract".format(clazz.getName)
        case Some(clazz) if !clazz.isInstanceOf[CaseClass] => "Class %s is not an instance of CaseClass".format(clazz.getName)
        case None if typeHint.isDefined && hintClazz.isEmpty => "Type hint %s='%s' causes ClassNotFoundException".
          format(ctx.typeHintStrategy.typeHint, typeHint)
        case None if path == null && typeHint.isEmpty => "Unknown class: type hint is empty and path from pickled ScalaSig is null"
        case _ => getReasonForPathClazz(pathClazz, path)
      }

      val explanation = """

      GRATER GLITCH - unable to find or instantiate a grater using supplied DBO type hint and/or path name

      REASON: %s

      Context: '%s'
      Path from pickled Scala sig: '%s'
      DBO type hint: '%s'

      FELL DOWN DESERIALZIING:
      %s

       """.format(reason, ctx.name.getOrElse("No name supplied"), path, typeHint.getOrElse(""), dbo)

      explanation
    }
  }

  object ArgsPrettyPrinter {
    def apply(args: Seq[AnyRef]) = if (args == null) {
      NullPlaceholder
    }
    else if (args.isEmpty) {
      EmptyPlaceholder
    }
    else {
      val builder = Seq.newBuilder[String]
      val p = "[%d]\t%s\n\t\t%s"
      for (i <- 0 until args.length) {
        val o = args(i)
        builder += p.format(i, o.getClass, truncate(o))
      }
      builder.result.mkString("\n")
    }
  }

  def truncate(a: AnyRef) = if (a == null) {
    val s = a.toString
    if (s != null && s.length > 100) s.substring(0, 100) + "..." else s
  }
  else a

  def convertBasicDBObject(m: BasicDBObject): Map[AnyRef, AnyRef] = if (m == null) {
    Map.empty[AnyRef, AnyRef]
  }
  else {
    val builder = Map.newBuilder[AnyRef, AnyRef]
    for ((k, v) <- m) {
      builder += k -> v
    }
    builder.result
  }

  // TODO: reflection.  i'm so ashamed.  but not so ashamed i wouldn't do it!
  def reflectFields(x: CaseClass): Map[Any, Any] = {
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

  object ClassPrettyPrinter {
    def apply(x: AnyRef) = x match {
      case Some(x) => "Some[%s]".format(x.asInstanceOf[AnyRef].getClass) // bugger type erasure
      case None => NonePlaceholder
      case null => NullPlaceholder
      case Nil => EmptyPlaceholder
      case x => (x.asInstanceOf[AnyRef]).getClass.getName
    }
  }

  /**
   * Hello, is this thing on?  If you are having trouble using Salat to serialize your thingy, dump it in here
   * and get real debug output!
   */
  object MapPrettyPrinter {
    def apply(x: CaseClass): String = if (x == null) {
      NullPlaceholder
    }
    else {
      apply(what = Some(x.getClass.getName), m = reflectFields(x))
    }

    def apply(m: MongoDBObject): String = if (m == null) {
      NullPlaceholder
    }
    else if (m.isEmpty) {
      EmptyPlaceholder
    }
    else {
      apply(what = Some("MongoDBObject"), m = m.toMap[AnyRef, AnyRef])
    }

    def apply(m: BasicDBObject): String = if (m == null) {
      NullPlaceholder
    }
    else if (m.isEmpty) {
      EmptyPlaceholder
    }
    else {
      apply(what = Some("BasicDBObject"), m = convertBasicDBObject(m))
    }

    def apply[A <: Any, B <: Any](what: Option[String] = None, m: Map[A, B], limit: Int = 10) = if (m == null) {
      NullPlaceholder
    }
    else if (m.isEmpty) {
      EmptyPlaceholder
    }
    else {
      val builder = Seq.newBuilder[String]
      val mapDesc = "Displaying %s with %d entries:"
      builder += {
        what match {
          case Some(what) => mapDesc.format(what, m.size)
          case None => mapDesc.format(m.getClass.getName, m.size)      // TODO: fix this using parametrized types
        }
      }
      val kv = "[%d] k=%s\tv=%s\n\t%s -> %s"
      var counter = 0
      val iter = m.iterator
      while (iter.hasNext && counter < limit) {
        val (k, v) = {
          val (k_any, v_any) = iter.next
          (k_any.asInstanceOf[AnyRef], v_any.asInstanceOf[AnyRef])
        }
        builder += kv.format(counter, ClassPrettyPrinter(k), ClassPrettyPrinter(v), truncate(k), truncate(v))
        counter += 1
      }
      builder.result.mkString("\n")
    }
  }

  val SalatThreads = new ThreadGroup("Salat")
  val DefaultSalatStackSize = 1024L * 1024

  // one megabyte
  class AsyncSalatRunnable(f: => Any)(r: Either[Throwable, Any] => Unit) extends Runnable {
    def run {
      try {
        r(Right(f))
      }
      catch {
        case t => r(Left(t))
      }
    }
  }

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
}
