package com.bumnetworks.salat

import java.lang.reflect.Method
import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.Imports._

abstract class Grater[X <: CaseClass](val clazz: Class[X])(implicit val ctx: Context) extends CasbahLogging {
  ctx.accept(this)

  lazy val sym = ScalaSigParser.parse(clazz).get.topLevelClasses.head
  lazy val caseAccessors = sym.children.filter(_.isCaseAccessor).filter(!_.isPrivate).map(_.asInstanceOf[MethodSymbol])

  lazy val indexedFields = caseAccessors.zipWithIndex.map { case (ms, idx) => Field(idx, ms.name, typeRefType(ms)) }
  lazy val fields = collection.SortedMap.empty[String, Field] ++ indexedFields.map { f => f.name -> f }

  lazy val companionClass = Class.forName("%s$".format(clazz.getName))
  lazy val companionObject = companionClass.getField("MODULE$").get(null)

  lazy val constructor: Method = companionClass.getMethods.filter(_.getName == "apply").head
  lazy val defaults: Seq[Option[Method]] = indexedFields.map {
    field => try {
      Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
    } catch {
      case _ => None
    }
  }

  def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  def generateDefault(idx: Int): Option[_] =
    defaults(idx).map(m => Some(m.invoke(companionObject))).getOrElse(None)

  def asDBObject(o: X): DBObject =
    o.productIterator.zip(indexedFields.iterator).foldLeft(MongoDBObject(ctx.typeHint -> clazz.getName)) {
      case (dbo, (null, _)) => dbo
      case (dbo, (element, field)) => {
        field.out_!(element) match {
          case Some(serialized) => dbo ++ MongoDBObject(field.name -> serialized)
          case _ => dbo
        }
      }
    }

  def asObject(dbo: MongoDBObject): X = {
    log.info("[%s] asObject( %s )", clazz.getSimpleName, dbo)
    val args = indexedFields.map {
      field => dbo.get(field.name) match {
        case Some(value) => field.in_!(value)
        case _ =>
          generateDefault(field.idx) match {
            case yes @ Some(default) => yes
            case _ => throw new Exception("%s requires value for '%s'".format(clazz, field.name))
          }
      }
    }.map(_.get.asInstanceOf[AnyRef])
    constructor.invoke(companionObject, args :_*).asInstanceOf[X]
  }
}
