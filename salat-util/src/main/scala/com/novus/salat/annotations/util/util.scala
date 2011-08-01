package com.novus.salat.annotations.util

import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement

object `package` {

  implicit def whatever2annotated(x: Any) = new PimpedAnnotatedElement(x)

  class PimpedAnnotatedElement(x: Any) {
    def annotation[A <: Annotation : Manifest]: Option[A] = x match {
      case x: AnnotatedElement if x != null => Option(x.getAnnotation[A](manifest[A].erasure.asInstanceOf[Class[A]]))
      case _ => None
    }

    def annotated_?[A <: Annotation : Manifest]: Boolean = annotation[A](manifest[A]).isDefined
  }

}