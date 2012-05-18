/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-util
 * Class:         util.scala
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

package com.novus.salat.annotations.util

import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement

object `package` {

  implicit def whatever2annotated(x: Any) = new PimpedAnnotatedElement(x)

  /** PML class that allows an element typed to Any to be checked for an arbitrary annotation.
   *  @param x an arbitrary input element
   */
  class PimpedAnnotatedElement(x: Any) {

    /** @tparam A type of annotation
     *  @return Some annotation if element X is annotated with an annotation of type A; otherwise None
     */
    def annotation[A <: Annotation: Manifest]: Option[A] = x match {
      case x: AnnotatedElement if x != null => Option(x.getAnnotation[A](manifest[A].erasure.asInstanceOf[Class[A]]))
      case _                                => None
    }

    /** @tparam A type of annotation
     *  @return true if element X is annotated with annotation of type A; otherwise, false
     */
    def annotated_?[A <: Annotation: Manifest]: Boolean = annotation[A](manifest[A]).isDefined
  }

}