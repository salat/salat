package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._

abstract class Grater[X <: AnyRef](clazz: Class[X]) extends CasbahLogging {
  lazy val sym = ScalaSigParser.parse(clazz).get.topLevelClasses.head
  lazy val names = sym.children.filter(_.isCaseAccessor).filter(!_.isPrivate).map(_.name)
}
