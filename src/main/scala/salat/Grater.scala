package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.Imports._

abstract class Grater[X <: CaseClass](val clazz: Class[X])(implicit val ctx: Context) extends CasbahLogging {
  ctx.accept(this)

  lazy val sym = ScalaSigParser.parse(clazz).get.topLevelClasses.head
  lazy val caseAccessors = sym.children.filter(_.isCaseAccessor).filter(!_.isPrivate).map(_.asInstanceOf[MethodSymbol])
  lazy val indexedFields = caseAccessors.zipWithIndex.map { case (ms, idx) => Field(idx, ms.name, typeRefType(ms)) }
  lazy val fields = collection.SortedMap.empty[String, Field] ++ indexedFields.map { f => f.name -> f }

  def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def asDBObject(o: X): DBObject =
    o.productIterator.zip(indexedFields.iterator).foldLeft(MongoDBObject("_typeHint" -> clazz.getName)) {
      case (dbo, (null, _)) => dbo
      case (dbo, (element, field)) => {
        field.out_!(element) match {
          case Some(serialized) => dbo ++ MongoDBObject(field.name -> serialized)
          case _ => dbo
        }
      }
    }

  def asObject(dbo: DBObject): X = clazz.newInstance.asInstanceOf[X]
}
