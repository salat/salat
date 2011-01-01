package com.bumnetworks.salat

import scala.collection.mutable.{Map => MMap, HashMap}
import com.mongodb.casbah.commons.Logging

trait Context extends Logging {
  private[salat] val graters: MMap[String, Grater[_ <: AnyRef]] = HashMap.empty
  val name: Option[String]

  def accept(grater: Grater[_ <: AnyRef]): Unit =
    if (!graters.contains(grater.clazz.getName)) {
      graters += grater.clazz.getName -> grater
      log.info("Context(%s) accepted Grater[%s]", name.getOrElse("<no name>"), grater.clazz)
    }
}

object `package` {
  type CasbahLogging = Logging
}

package object global {
  implicit val ctx = new Context { val name = Some("global") }
}
