package com.novus.salat.transform_scratch

import scala.collection.mutable.{Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig.TypeRefType
import com.novus.salat.{IsOption, Context}

trait TransformerRegistry {

  val transformers: MMap[String, Transformation]
  val optionTransformers: MMap[String, Transformation]
  val traversableTransformers: MMap[String, TransformationWithParentType]
  val mapTransformers: MMap[String, TransformationWithParentType]

//  def select(t: TypeRefType, hint: Boolean = false)(implicit ctx: Context): Transformation = t match {
//    case IsOption(t @ TypeRefType(_, _, _)) => t match {
//
//    }
//  }

}