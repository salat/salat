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

import scala.math.{ BigDecimal => ScalaBigDecimal }
import scala.tools.scalap.scalax.rules.scalasig._

import com.novus.salat.annotations.raw._
import com.novus.salat.annotations.util._
import com.novus.salat.util.{ ScalaSigUtil, Logging }

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
            val parsed = ScalaSigUtil.parseScalaSig0(clazz, ctx.classLoaders).get.topLevelClasses.head
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