package com.bumnetworks.salat

import java.lang.reflect.Method
import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.Imports._

import com.bumnetworks.salat.annotations.raw._
import com.bumnetworks.salat.annotations.util._

abstract class Grater[X <: CaseClass](val clazz: Class[X])(implicit val ctx: Context) extends CasbahLogging {
  ctx.accept(this)

  protected def parseScalaSig = {
    ScalaSigParser.parse(clazz) match {
      case None if clazz.getName.endsWith("$") =>
        ScalaSigParser.parse(Class.forName(clazz.getName.replaceFirst("\\$$", "")))
      case x => x
    }
  }

  protected lazy val sym = parseScalaSig match {
    case Some(x) =>
      x.topLevelClasses.headOption.getOrElse(
        x.topLevelObjects.headOption.getOrElse(
          throw new Exception("parsed pickled Scala signature, but no expected type found: %s".format(clazz))))
    case _ => throw new Exception("failed to parse pickled Scala signature from %s".format(clazz))
  }

  protected lazy val indexedFields = {
    sym.children
    .filter(_.isCaseAccessor)
    .filter(!_.isPrivate)
    .map(_.asInstanceOf[MethodSymbol])
    .zipWithIndex
    .map {
      case (ms, idx) =>
        Field(idx, ms.name, typeRefType(ms), clazz.getMethod(ms.name))
    }
  }
  lazy val fields = collection.SortedMap.empty[String, Field] ++ indexedFields.map { f => f.name -> f }

  lazy val companionClass = clazz.companionClass
  lazy val companionObject = clazz.companionObject

  protected lazy val constructor: Option[Method] =
    if (sym.isModule) None else companionClass.getMethods.filter(_.getName == "apply").headOption

  protected lazy val defaults: Seq[Option[Method]] = indexedFields.map {
    field => try {
      Some(companionClass.getMethod("apply$default$%d".format(field.idx + 1)))
    } catch {
      case _ => None
    }
  }

  protected def typeRefType(ms: MethodSymbol): TypeRefType = ms.infoType match {
    case PolyType(tr @ TypeRefType(_, _, _), _) => tr
  }

  protected def generateDefault(idx: Int): Option[_] =
    defaults(idx).map(m => Some(m.invoke(companionObject))).getOrElse(None)

  def asDBObject(o: X): DBObject = {
    val builder = MongoDBObject.newBuilder
    ctx.typeHint match {
      case Some(hint) => builder += hint -> clazz.getName
      case _ =>
    }

    o.productIterator.zip(indexedFields.iterator).foreach {
      case (_, field) if field.ignore => {}
      case (null, _) => {}
      case (element, field) => {
        field.out_!(element) match {
          case Some(None) => {}
          case Some(serialized) =>
            builder += field.name -> (serialized match {
              case Some(unwrapped) => unwrapped
              case _ => serialized
            })
          case _ => {}
        }
      }
    }

    builder.result
  }

  protected def safeDefault(field: Field) =
    generateDefault(field.idx) match {
      case yes @ Some(default) => yes
      case _ => field.typeRefType match {
        case IsOption(_) => Some(None)
        case _ => throw new Exception("%s requires value for '%s'".format(clazz, field.name))
      }
    }

  def asObject(dbo: MongoDBObject): X =
    if (sym.isModule) companionObject.asInstanceOf[X]
    else {
      val args = indexedFields.map {
        case field if field.ignore => safeDefault(field)
        case field => dbo.get(field.name) match {
          case Some(value) => field.in_!(value)
          case _ => safeDefault(field)
        }
      }.map(_.get.asInstanceOf[AnyRef])
      constructor.get.invoke(companionObject, args :_*).asInstanceOf[X]
    }

  override def toString = "Grater(%s @ %s)".format(clazz, ctx)
}
