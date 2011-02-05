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
import com.mongodb.casbah.commons.Logging

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

object IsSeq {
  def unapply(t: Type): Option[Type] =
    t match {
      case TypeRefType(_, symbol, List(e)) =>
        if (symbol.path.endsWith(".Seq")) Some(e)
        else if (symbol.path.endsWith(".List")) Some(e)
        else None
      case _ => None
    }
}

object IsScalaBigDecimal {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, classOf[ScalaBigDecimal].getName)
}

object IsTrait extends Logging {
  private def noAnnotationOnTrait(t: Class[_]) =
    throw new Exception("NB: trait %s must be annotated with @com.novus.salat.annotations.Salat " +
                        "in order to be picked up by this library. See the docs for more details.".format(t.getName))

  def unapply(t: TypeRefType): Option[Type] = t match {
    case t @ TypeRefType(_, symbol, _) => {
      try {
        getClassNamed(symbol.path)(com.novus.salat.global.ctx.classLoaders) match {
          case Some(clazz: Class[_]) => {
            val parsed = ScalaSigParser.parse(clazz).get.topLevelClasses.head
            if (parsed.isTrait) {
              if (clazz.annotated_?[Salat]) Some(t)
              else noAnnotationOnTrait(clazz)
            } else None
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
