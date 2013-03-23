package com.novus.salat.transformers

abstract class CustomTransformer[ModelObject <: AnyRef: Manifest, SerializedRepr <: AnyRef: Manifest]() {

  final def in(value: Any): Any = value match {
    case o: SerializedRepr => deserialize(o)
    case _                 => None
  }

  final def out(value: Any): Option[SerializedRepr] = value match {
    case i: ModelObject => Option(serialize(i))
  }

  def path = manifest[ModelObject].runtimeClass.getName

  def deserialize(b: SerializedRepr): ModelObject

  def serialize(a: ModelObject): SerializedRepr

  override def toString = "CustomTransformer[ %s <-> %s ]".format(manifest[ModelObject].runtimeClass.getName, manifest[SerializedRepr].runtimeClass.getName)
}
