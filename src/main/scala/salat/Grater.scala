package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.Imports._

abstract class Grater[X <: AnyRef](val clazz: Class[X])(implicit val ctx: Context) extends CasbahLogging {
  ctx.accept(this)

  lazy val sym = ScalaSigParser.parse(clazz).get.topLevelClasses.head
  lazy val caseAccessors = sym.children.filter(_.isCaseAccessor).filter(!_.isPrivate).map(_.asInstanceOf[MethodSymbol])
  lazy val typeRefTypes = caseAccessors.map(typeRefType _)
  lazy val fields = collection.SortedMap.empty[String, Field] ++ caseAccessors.map(ms => ms.name -> Field(ms.name, typeRefType(ms)))

  def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def asDBObject(o: X): DBObject =
    MongoDBObject("_typeHint" -> clazz.getName)

  def asObject(dbo: DBObject): X = clazz.newInstance.asInstanceOf[X]
}
