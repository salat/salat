package com.novus.salat

import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement
import scala.annotation.target.getter

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

}
