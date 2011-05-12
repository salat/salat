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


import scala.collection.immutable.{List => IList, Map => IMap}
import scala.collection.mutable.{Buffer, ArrayBuffer, Map => MMap}
import scala.tools.scalap.scalax.rules.scalasig._
import com.novus.salat.impls.ImplClasses

package object impls {
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

  def mapImpl(name: String, real: collection.Map[_, _]): scala.collection.Map[_, _] = name match {
    case ImplClasses.IMapClass => IMap.empty ++ real
    case ImplClasses.MMapClass => MMap.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Map[_,_] impl for %s".format(x))
  }

  def mapImpl(t: Type, real: collection.Map[_, _]): scala.collection.Map[_, _] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.Predef.Map" => mapImpl(ImplClasses.IMapClass, real)
        case x => mapImpl(x, real)
      }
    }
  
  def setImpl(name: String, real: collection.Set[_]): scala.collection.Set[_] = name match {
    case ImplClasses.SetClass => Set.empty ++ real
    case x => throw new IllegalArgumentException("failed to find proper Set[_] impl for %s".format(x))
  }

  def setImpl(t: Type, real: collection.Set[_]): scala.collection.Set[_] =
    t match {
      case TypeRefType(_, symbol, _) => symbol.path match {
        case "scala.Predef.Set" => setImpl(ImplClasses.SetClass, real)
        case x => setImpl(x, real)
      }
    }
}

package impls {

  object ImplClasses {
    val IListClass = classOf[IList[_]].getName
    val BufferClass = classOf[Buffer[_]].getName
    val SeqClass = classOf[scala.collection.Seq[_]].getName
    
    val IMapClass = classOf[IMap[_, _]].getName
    val MMapClass = classOf[MMap[_, _]].getName
    
    val SetClass = classOf[scala.collection.Set[_]].getName
  }

}