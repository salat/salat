package com.bumnetworks.salat

import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.Imports._

abstract class Grater[X <: AnyRef with Product](val clazz: Class[X])(implicit val ctx: Context) extends CasbahLogging {
  ctx.accept(this)

  lazy val sym = ScalaSigParser.parse(clazz).get.topLevelClasses.head
  lazy val caseAccessors = sym.children.filter(_.isCaseAccessor).filter(!_.isPrivate).map(_.asInstanceOf[MethodSymbol])
  lazy val indexedFields = caseAccessors.zipWithIndex.map { case (ms, idx) => Field(idx, ms.name, typeRefType(ms)) }
  lazy val fields = collection.SortedMap.empty[String, Field] ++ indexedFields.map { f => f.name -> f }

  def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def asDBObject(o: X): DBObject = {
    val builder = MongoDBObject.newBuilder

    o.productIterator.zip(indexedFields.iterator).foreach {
      case (element, field) => {
        val value = field.typeRefType match {
          case IsOption(_) => element match {
            case Some(v) => Some(v)
            case _ => None
          }
	  case _ => Some(element)
        }

	value match {
	  case Some(bareValue) => builder += field.name -> field.out_!(bareValue).get
	  case _ =>
	}
      }
    }

    builder += "_typeHint" -> clazz.getName
    builder.result
  }

  def asObject(dbo: DBObject): X = clazz.newInstance.asInstanceOf[X]
}
