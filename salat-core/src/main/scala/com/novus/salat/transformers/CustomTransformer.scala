package com.novus.salat.transformers

import com.novus.salat.util.Logging

abstract class CustomTransformer[ModelObject <: AnyRef: Manifest, SerializedRepr <: AnyRef: Manifest]() extends Logging {

  final def in(value: Any): Any = {
    //    log.debug("%s\nin: value: %s", toString, value)
    value match {
      case Some(o: SerializedRepr) => deserialize(o)
      case o: SerializedRepr       => deserialize(o)
      case _                       => None
    }

  }
  final def out(value: Any): Option[SerializedRepr] = value match {
    case i: ModelObject => Option(serialize(i))
  }

  def path = manifest[ModelObject].erasure.getName

  def deserialize(b: SerializedRepr): ModelObject

  def serialize(a: ModelObject): SerializedRepr

  override def toString = "CustomTransformer[ %s <-> %s ]".format(manifest[ModelObject].erasure.getName, manifest[SerializedRepr].erasure.getName)
}
