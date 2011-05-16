/**
* Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* For questions and comments about this product, please see the project page at:
*
* http://github.com/novus/salat
*
*/
package com.novus.salat


import scala.collection.mutable.{Map => MMap, Set => MSet, Seq => MSeq, IndexedSeq => MIndexedSeq}
import scala.collection.mutable.{Buffer, ArrayBuffer, LinkedList, DoubleLinkedList}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.collection.immutable.{List => IList, Map => IMap, Set => ISet, Seq => ISeq, IndexedSeq => IIndexedSeq}

package object impls {
  def traversableImpl(name: String, real: collection.Traversable[_]): scala.collection.Traversable[_] = name match {

    case ImplClasses.BufferClass => Buffer.empty ++ real
    case ImplClasses.ArrayBufferClass => ArrayBuffer.empty ++ real

    case ImplClasses.SeqClass => Seq.empty ++ real
    case ImplClasses.ISeqClass => ISeq.empty ++ real
    case ImplClasses.MSeqClass => MSeq.empty ++ real

    case ImplClasses.IListClass => IList.empty ++ real

    case ImplClasses.SetClass => Set.empty ++ real
    case ImplClasses.ISetClass => ISet.empty ++ real
    case ImplClasses.MSetClass => MSet.empty ++ real

    case ImplClasses.ISetClass => ISet.empty ++ real
    case ImplClasses.MSetClass => MSet.empty ++ real

    case ImplClasses.VectorClass => Vector.empty ++ real

    case ImplClasses.IndexedSeq => IndexedSeq.empty ++ real
    case ImplClasses.IIndexedSeq => IIndexedSeq.empty ++ real
    case ImplClasses.MIndexedSeq => MIndexedSeq.empty ++ real

    case ImplClasses.LinkedList => LinkedList.empty ++ real
    case ImplClasses.DoubleLinkedList => DoubleLinkedList.empty ++ real

    case x => throw new IllegalArgumentException("failed to find proper Traversable[_] impl for %s".format(x))
  }

  def traversableImpl(t: Type, real: scala.collection.Traversable[_]): scala.collection.Traversable[_] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.package.Seq" => traversableImpl(ImplClasses.SeqClass, real)
        case "scala.package.List" => traversableImpl(ImplClasses.IListClass, real)
        case "scala.package.Vector" => traversableImpl(ImplClasses.VectorClass, real)
        case "scala.package.IndexedSeq" => traversableImpl(ImplClasses.IndexedSeq, real)
        case "scala.Predef.Set" => traversableImpl(ImplClasses.SetClass, real)
        case x => traversableImpl(x, real)
      }
    }

  def mapImpl(name: String, real: scala.collection.Map[_, _]): scala.collection.Map[_, _] = name match {
    case ImplClasses.MapClass => Map.empty ++ real
    case ImplClasses.IMapClass => IMap.empty ++ real
    case ImplClasses.MMapClass => MMap.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Map[_,_] impl for %s".format(x))
  }

  def mapImpl(t: Type, real: collection.Map[String, _]): scala.collection.Map[_, _] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.Predef.Map" => mapImpl(ImplClasses.IMapClass, real)
        case x => mapImpl(x, real)
      }
    }
}

package impls {

  object ImplClasses {

    val IListClass = classOf[IList[_]].getName

    val SeqClass = classOf[scala.collection.Seq[_]].getName
    val ISeqClass = classOf[ISeq[_]].getName
    val MSeqClass = classOf[MSeq[_]].getName

    val BufferClass = classOf[Buffer[_]].getName
    val ArrayBufferClass = classOf[ArrayBuffer[_]].getName

    val MapClass = classOf[scala.collection.Map[_, _]].getName
    val IMapClass = classOf[IMap[_, _]].getName
    val MMapClass = classOf[MMap[_, _]].getName
    
    val SetClass = classOf[scala.collection.Set[_]].getName
    val ISetClass = classOf[ISet[_]].getName
    val MSetClass = classOf[MSet[_]].getName

    val VectorClass = classOf[scala.collection.immutable.Vector[_]].getName

    val IndexedSeq = classOf[scala.collection.IndexedSeq[_]].getName
    val IIndexedSeq = classOf[IIndexedSeq[_]].getName
    val MIndexedSeq = classOf[MIndexedSeq[_]].getName

    val LinkedList = classOf[LinkedList[_]].getName
    val DoubleLinkedList = classOf[DoubleLinkedList[_]].getName
  }

}