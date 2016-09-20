/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         CollectionSupportModel.scala
 * Last modified: 2012-10-15 20:40:58 EDT
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */
package com.novus.salat.test.model

import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._

case class Thingy(t: String)

case class Omphalos(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.Map[String, Thingy])
case class Able(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.immutable.Map[String, Thingy])
case class Baker(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.Map[String, Thingy])

case class Charlie(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.Set[Thingy])
case class Dog(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.immutable.Set[Thingy])
case class Easy(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.Set[Thingy])

case class Fox(@Key("_id") id: ObjectId = new ObjectId, coll: List[Thingy])

case class Gee(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.Seq[Thingy])
case class How(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.immutable.Seq[Thingy])
case class Item(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.Seq[Thingy])

case class Jig(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.Buffer[Thingy])
case class King(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.ArrayBuffer[Thingy])

// TODO: Salat does *not* offer support for arrays yet!
case class Love(@Key("_id") id: ObjectId = new ObjectId, coll: Array[Thingy])

case class Mike(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.immutable.Vector[Thingy])

case class Nab(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.IndexedSeq[Thingy])
case class Oboe(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.immutable.IndexedSeq[Thingy])
case class Prep(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.IndexedSeq[Thingy])

case class Queen(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.LinkedList[Thingy])
case class Roger(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.DoubleLinkedList[Thingy])

//Sugar
//Tare
//Uncle
//Victor
//William

case class XRay(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.BitSet)
case class Yoke(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.immutable.BitSet)
case class Zebra(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.mutable.BitSet)

case class GloratArrayByte(@Key("_id") id: ObjectId = new ObjectId, coll: Array[Byte])
case class GloratSeqByte(@Key("_id") id: ObjectId = new ObjectId, coll: scala.collection.Seq[Byte])