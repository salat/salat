package com.bumnetworks.salat

import java.lang.reflect.{Method, AnnotatedElement}
import java.lang.annotation.Annotation

import scala.annotation.target.getter

package object annotations {
  type Key = raw.Key @getter
  type Salat = raw.Salat @getter

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
