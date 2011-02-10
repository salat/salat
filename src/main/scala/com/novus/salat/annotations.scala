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

import java.lang.reflect.AnnotatedElement
import java.lang.annotation.Annotation

import scala.annotation.target.getter

package object annotations {

  type Key = com.novus.salat.annotations.raw.Key @getter

  type Salat = com.novus.salat.annotations.raw.Salat @getter

  type EnumAs = com.novus.salat.annotations.raw.EnumAs @getter

  object util {
    implicit def whatever2annotated(x: Any) = new {
      def annotation[A <: Annotation : Manifest]: Option[A] =
        x match {
          case x: AnnotatedElement if x != null => x.getAnnotation[A](manifest[A].erasure.asInstanceOf[Class[A]]) match {
            case a: A if a != null => Some(a)
            case _ => None
          }
          case _ => None
        }

      def annotated_?[A <: Annotation : Manifest]: Boolean = annotation[A](manifest[A]).isDefined
    }
  }
}