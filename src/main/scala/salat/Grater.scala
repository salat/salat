package com.bumnetworks.salat

import tools.scalap.scalax.rules.scalasig._

abstract class Grater[X <: AnyRef](clazz: Class[X]) extends CasbahLogging {
  protected val sig = ScalaSigParser.parse(clazz).get.topLevelClasses.head
}
