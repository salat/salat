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


//Queen
//Roger
//Sugar
//Tare
//Uncle
//Victor
//William
//X-ray
//Yoke
//Zebra
