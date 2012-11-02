/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         TypeMatchers.scala
 * Last modified: 2012-09-17 22:38:46 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
package com.novus.salat

import scala.math.{ BigDecimal => SBigDecimal, BigInt }
import tools.scalap.scalax.rules.scalasig.{ TypeRefType, Type, Symbol }

protected[salat] object Types {
  val Date = "java.util.Date"
  val DateTime = Set("org.joda.time.DateTime", "org.scala_tools.time.TypeImports.DateTime")
  val Oid = Set("org.bson.types.ObjectId", "com.mongodb.casbah.commons.TypeImports.ObjectId")
  val BsonTimestamp = "org.bson.types.BSONTimestamp"
  val SBigDecimal = classOf[SBigDecimal].getName
  val BigInt = classOf[BigInt].getName
  val Option = "scala.Option"
  val Map = ".Map"
  val Traversables = Set(".Seq", ".List", ".Vector", ".Set", ".Buffer", ".ArrayBuffer", ".IndexedSeq", ".LinkedList", ".DoubleLinkedList")
  val BitSets = Set("scala.collection.BitSet", "scala.collection.immutable.BitSet", "scala.collection.mutable.BitSet")

  def isOption(sym: Symbol) = sym.path == Option

  def isMap(symbol: Symbol) = symbol.path.endsWith(Map)

  def isTraversable(symbol: Symbol) = Traversables.exists(symbol.path.endsWith(_))

  def isBitSet(symbol: Symbol) = BitSets.contains(symbol.path)

  def isBigDecimal(symbol: Symbol) = symbol.path == SBigDecimal

  def isBigInt(symbol: Symbol) = symbol.path == BigInt
}

protected[salat] case class TypeFinder(t: TypeRefType) {

  lazy val path = t.symbol.path

  lazy val isMap = Types.isMap(t.symbol)
  lazy val isTraversable = Types.isTraversable(t.symbol)
  lazy val isBitSet = Types.isBitSet(t.symbol)

  lazy val isDate = TypeMatchers.matches(t, Types.Date)
  lazy val isDateTime = TypeMatchers.matches(t, Types.DateTime)

  lazy val isChar = TypeMatchers.matches(t, classOf[Char].getName)
  lazy val isFloat = TypeMatchers.matches(t, classOf[Float].getName)
  lazy val isShort = TypeMatchers.matches(t, classOf[Short].getName)
  lazy val isBigDecimal = Types.isBigDecimal(t.symbol)
  lazy val isBigInt = Types.isBigInt(t.symbol)
  lazy val isLong = TypeMatchers.matches(t, classOf[Long].getName)

  lazy val isOption = TypeMatchers.matches(t, Types.Option)
  lazy val isOid = TypeMatchers.matches(t, Types.Oid)
  lazy val isURL = TypeMatchers.matches(t, classOf[java.net.URL].getName)
  lazy val isBSONTimestamp = TypeMatchers.matches(t, Types.BsonTimestamp)

  lazy val directlyDeserialize = isDate || isDateTime || isBSONTimestamp || isOid
}

protected[salat] object TypeMatchers {

  def matchesOneType(t: Type, name: String): Option[Type] = t match {
    case TypeRefType(_, symbol, List(arg)) if symbol.path == name => Some(arg)
    case _ => None
  }

  def matches(t: TypeRefType, name: String) = t.symbol.path == name

  def matches(t: TypeRefType, names: Traversable[String]) = names.exists(t.symbol.path == _)

  def matchesMap(t: Type) = t match {
    case TypeRefType(_, symbol, k :: v :: Nil) if Types.isMap(symbol) => Some(k -> v)
    case _ => None
  }

  def matchesTraversable(t: Type) = t match {
    case TypeRefType(_, symbol, List(arg)) if Types.isTraversable(symbol) => Some(arg)
    case _ => None
  }

  def matchesBitSet(t: Type) = t match {
    case TypeRefType(_, symbol, _) if Types.isBitSet(symbol) => Some(symbol)
    case _ => None
  }
}

protected[salat] object IsOption {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, Types.Option)
}

protected[salat] object IsMap {
  def unapply(t: Type): Option[(Type, Type)] = TypeMatchers.matchesMap(t)
}

protected[salat] object IsTraversable {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesTraversable(t)
}

protected[salat] object IsScalaBigDecimal {
  def unapply(t: Type): Option[Type] = TypeMatchers.matchesOneType(t, Types.SBigDecimal)
}
