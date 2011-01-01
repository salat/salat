package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._

abstract class Grater[X <: AnyRef](val clazz: Class[X])(implicit val ctx: Context) extends CasbahLogging {
  ctx.accept(this)

  lazy val sym = ScalaSigParser.parse(clazz).get.topLevelClasses.head
  lazy val caseAccessors = sym.children.filter(_.isCaseAccessor).filter(!_.isPrivate).map(_.asInstanceOf[MethodSymbol])
  lazy val typeRefTypes = caseAccessors.map(typeRefType _)
  lazy val names = collection.SortedMap.empty[String, Field] ++ caseAccessors.map(ms => ms.name -> Field(ms, typeRefType(ms)))

  def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def named_?(name: String) = caseAccessors.filter(_.name == name).headOption
  def named(name: String) = named_?(name).getOrElse(throw new IllegalArgumentException("no such field '%s' in '%s'".format(name, clazz)))
}
