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
package com.novus.salat.util

import com.novus.salat._
import com.mongodb.casbah.commons.Imports._

/** Hello, is this thing on?  If you are having trouble using Salat to serialize your thingy, dump it in here
 *  and get real debug output!
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
        case None       => mapDesc.format(m.getClass.getName, m.size) // TODO: fix this using parametrized types
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