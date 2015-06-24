/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         annotations.scala
 * Last modified: 2012-12-06 22:09:59 EST
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */

package com.novus.salat

import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement
import scala.annotation.meta.getter

package object annotations {
  type Key = com.novus.salat.annotations.raw.Key @getter
  type Salat = com.novus.salat.annotations.raw.Salat @getter
  type EnumAs = com.novus.salat.annotations.raw.EnumAs @getter
  type Persist = com.novus.salat.annotations.raw.Persist @getter
  type Ignore = com.novus.salat.annotations.raw.Ignore @getter
}

package annotations {

  package object util {
    implicit def whatever2annotated(x: Any) = new PimpedAnnotatedElement(x)

    /**
     * PML class that allows an element typed to Any to be checked for an arbitrary annotation.
     *  @param x an arbitrary input element
     */
    class PimpedAnnotatedElement(x: Any) {

      /**
       * @tparam A type of annotation
       *  @return Some annotation if element X is annotated with an annotation of type A; otherwise None
       */
      def annotation[A <: Annotation: Manifest]: Option[A] = x match {
        case x: AnnotatedElement if x != null => Option(x.getAnnotation[A](manifest[A].runtimeClass.asInstanceOf[Class[A]]))
        case _                                => None
      }

      /**
       * @tparam A type of annotation
       *  @return true if element X is annotated with annotation of type A; otherwise, false
       */
      def annotated_?[A <: Annotation: Manifest]: Boolean = annotation[A](manifest[A]).isDefined
    }

  }

}
