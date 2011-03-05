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
package object util {

  val NonePlaceholder = "[None]"
  val NullPlaceholder = "[Null]"
  val EmptyPlaceholder = "[Empty]"

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

}