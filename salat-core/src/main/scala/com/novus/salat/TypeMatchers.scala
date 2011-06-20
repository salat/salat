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

import scala.math.{BigDecimal => ScalaBigDecimal}
import scala.tools.scalap.scalax.rules.scalasig._

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.transformers._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging

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

abstract class NeedsSalatAnnotation(what: String, t: Class[_]) extends Error("""

 NB: %s %s must be annotated with @com.novus.salat.annotations.Salat
 in order to be picked up by this library. See the docs for more details.

 """.format(what, t.getName))

case class NoAnnotationTrait(t: Class[_]) extends NeedsSalatAnnotation("trait", t)
case class NoAnnotationAbstractSuperclass(t: Class[_]) extends NeedsSalatAnnotation("abstract superclass", t)

object IsTraitLike extends Logging {

  def unapply(t: TypeRefType)(implicit ctx: Context): Option[Type] = t match {
    case t @ TypeRefType(_, symbol, _) => {
      try {
        getClassNamed(symbol.path) match {
          case Some(clazz: Class[_]) => {
            val parsed = ScalaSigUtil.parseScalaSig0(clazz).get.topLevelClasses.head
            if (parsed.isTrait) {
              if (clazz.annotated_?[Salat]) Some(t) else {
                throw NoAnnotationTrait(clazz)
              }
            }
            else if (parsed.isAbstract) {
              if (clazz.annotated_?[Salat]) Some(t) else {
                throw NoAnnotationAbstractSuperclass(clazz)
              }
            }
            else None
          }
          case _ => None
        }
      }
      catch {
        case _ => None
      }
    }
    case _ => None
  }
}

object IsEnum extends Logging {
  def unapply(t: TypeRefType): Option[SingleType] = {
    t match {
      case TypeRefType(prefix @ SingleType(_, esym), sym, _) if sym.path == "scala.Enumeration.Value" =>
	Some(prefix)
      case _ => None
    }
  }
}
