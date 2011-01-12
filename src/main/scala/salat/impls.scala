package com.novus.salat.impls

import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._

object ImplClasses {
  val IListClass = classOf[IList[_]].getName
  val BufferClass = classOf[Buffer[_]].getName
  val SeqClass = classOf[scala.collection.Seq[_]].getName

  val IMapClass = classOf[IMap[_,_]].getName
  val MMapClass = classOf[MMap[_,_]].getName
}

object `package` {
  def seqImpl(name: String, real: collection.Seq[_]): scala.collection.Seq[_] = name match {
    case ImplClasses.IListClass => IList.empty ++ real
    case ImplClasses.BufferClass => Buffer.empty ++ real
    case ImplClasses.SeqClass => IList.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Seq[_] impl for %s".format(x))
  }

  def seqImpl(t: Type, real: collection.Seq[_]): scala.collection.Seq[_] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.package.Seq" => seqImpl(ImplClasses.SeqClass, real)
        case "scala.package.List" => seqImpl(ImplClasses.IListClass, real)
        case x => seqImpl(x, real)
      }
    }

  def mapImpl(name: String, real: collection.Map[_,_]): scala.collection.Map[_,_] = name match {
    case ImplClasses.IMapClass => IMap.empty ++ real
    case ImplClasses.MMapClass => MMap.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Map[_,_] impl for %s".format(x))
  }

  def mapImpl(t: Type, real: collection.Map[_,_]): scala.collection.Map[_,_] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.Predef.Map" => mapImpl(ImplClasses.IMapClass, real)
        case x => mapImpl(x, real)
      }
    }
}
