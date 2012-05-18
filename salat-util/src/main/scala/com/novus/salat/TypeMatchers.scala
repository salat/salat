/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-util
 * Class:         TypeMatchers.scala
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
package com.novus.salat

import scala.math.{ BigDecimal => ScalaBigDecimal }
import com.novus.salat.util.Logging
import scala.tools.scalap.scalax.rules.scalasig.{ SingleType, TypeRefType, Type }

private object TypeMatchers {
  def matchesOneType(t: Type, name: String): Option[Type] = t match {
    case TypeRefType(_, symbol, List(arg)) if symbol.path == name => Some(arg)
    case _ => None
  }
}

object IsOption {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, "scala.Option")
}

object IsMap {
  /**
   *
   * @param t
   * @return
   */
  def unapply(t: Type): Option[(Type, Type)] =
    t match {
      case TypeRefType(_, symbol, k :: v :: Nil) if symbol.path.endsWith(".Map") => Some(k -> v)
      case _ => None
    }
}

object IsTraversable {
  def unapply(t: Type): Option[Type] =
    t match {
      case TypeRefType(_, symbol, List(e)) =>
        if (symbol.path.endsWith(".Seq")) Some(e)
        else if (symbol.path.endsWith(".List")) Some(e)
        else if (symbol.path.endsWith(".Set")) Some(e)
        else if (symbol.path.endsWith(".Buffer")) Some(e)
        else if (symbol.path.endsWith(".ArrayBuffer")) Some(e)
        else if (symbol.path.endsWith(".Vector")) Some(e)
        else if (symbol.path.endsWith(".IndexedSeq")) Some(e)
        else if (symbol.path.endsWith(".LinkedList")) Some(e)
        else if (symbol.path.endsWith(".DoubleLinkedList")) Some(e)
        else None
      case _ => None
    }
}

object IsScalaBigDecimal {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, classOf[ScalaBigDecimal].getName)
}