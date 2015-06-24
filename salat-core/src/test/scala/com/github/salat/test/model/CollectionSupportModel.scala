/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         CollectionSupportModel.scala
 * Last modified: 2015-06-23 20:52:14 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package com.github.salat.test.model

import com.github.salat.annotations._
import com.mongodb.casbah.Imports._

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
