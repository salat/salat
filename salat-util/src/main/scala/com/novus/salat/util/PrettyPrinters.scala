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
    builder.result().mkString("\n")
  }
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

