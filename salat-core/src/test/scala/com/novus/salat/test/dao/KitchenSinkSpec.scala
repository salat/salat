/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         MyModel.scala
 * Last modified: 2013-01-07 22:46:23 EST
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

package com.novus.salat.test.dao

import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._
import com.novus.salat.dao.{ SalatDAO, ModelCompanion }
import org.joda.time._
import com.novus.salat.test._
import com.novus.salat.test.global._

// Uber Model to test all the complex nested collection support added.

object KitchenModel extends ModelCompanion[KitchenSink, ObjectId] {
  val collection = MongoConnection()(SalatSpecDb)("kitchen")
  val dao = new SalatDAO[KitchenSink, ObjectId](collection = collection) {}
}

case class KitchenSink(
  @Key("_id") id: ObjectId,
  a: Animal,
  ll: ListList,
  lll: ListListList,
  lo: ListOpt,
  lm: ListMap,
  oo: OpOp,
  ol: OpList,
  oll: OpListList,
  om: OpMap,
  ml: MapList,
  mll: MapListList,
  mo: MapOpt,
  mm: MapMap)

//--- Base classes
case class Animal(val name: String, val legs: Int)

// Test Lists
case class ListList(val name: String, val stuff: List[List[Animal]])
case class ListListList(val name: String, val stuff: List[List[List[Animal]]])
case class ListOpt(val name: String, val stuff: List[Option[Animal]])
case class ListMap(val name: String, val stuff: List[Map[String, Animal]])

// Test nested Options+Variants w/other collections
case class OpOp(val name: String, val opts: Option[Option[Animal]])
case class OpList(val name: String, val opList: Option[List[Animal]])
case class OpListList(val name: String, val opListList: Option[List[List[Animal]]])
case class OpMap(val name: String, val opMap: Option[Map[String, Animal]])

// Test nested Maps+Variants w/other collections
case class MapList(val name: String, val mapList: Map[String, List[Animal]])
case class MapListList(val name: String, val mapList: Map[String, List[List[Animal]]])
case class MapOpt(val name: String, val mapOpt: Map[String, Option[Animal]])
case class MapMap(val name: String, val mapmap: Map[String, Map[String, Animal]])

object KitchenHelper {
  val a = Animal("mouse", 4)
  val ma = Map("Foo" -> a)
  val ll = ListList("Fred", List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4))))
  val lll = ListListList("Fred",
    List(
      List(
        List(
          Animal("mouse", 4),
          Animal("bug", 6)),
        List(
          Animal("whale", 0),
          Animal("elephant", 4))),
      List(
        List(
          Animal("millipede", 1000),
          Animal("slug", 0)),
        List(
          Animal("bird", 2),
          Animal("tiger", 4)))))
  val lo = ListOpt("Jenny", List(Some(Animal("mouse", 4)), None, Some(Animal("whale", 0))))
  val lm = ListMap("Jenny", List(Map("a" -> Animal("mouse", 4)), Map("b" -> Animal("whale", 0))))
  val oo = OpOp("Oops", Some(Some(Animal("mouse", 4))))
  val ol = OpList("Wow", Some(List(Animal("mouse", 4), Animal("bug", 6))))
  val oll = OpListList("Yay", Some(List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4)))))
  val om = OpMap("Wow", Some(Map("hello" -> (Animal("mouse", 4)))))
  val ml = MapList("Bob", Map("Mike" -> List(Animal("mouse", 4), Animal("bug", 6)), "Sally" -> List(Animal("whale", 0), Animal("elephant", 4))))
  val mll = MapListList("Bob", Map("Everyone" -> List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4)))))
  val x: Option[Animal] = None
  val mo = MapOpt("Bob", Map("things" -> Some(Animal("mouse", 4)), "otherthings" -> x))
  val mm = MapMap("Bob", Map("things" -> Map("a" -> Animal("mouse", 4), "b" -> Animal("horse", 4)), "stuff" -> Map("c" -> Animal("sloth", 2))))

  def apply() =
    KitchenSink(
      new ObjectId,
      a,
      ll,
      lll,
      lo,
      lm,
      oo,
      ol,
      oll,
      om,
      ml,
      mll,
      mo,
      mm)
  def withNonesRemoved() =
    KitchenSink(
      new ObjectId,
      a,
      ll,
      lll,
      lo.copy(stuff = List(Some(Animal("mouse", 4)), Some(Animal("whale", 0)))),
      lm,
      oo,
      ol,
      oll,
      om,
      ml,
      mll,
      mo.copy(mapOpt = mo.mapOpt.filter({ case (k, v) => v.isDefined })),
      mm)
}

class KitchenSinkSpec extends SalatSpec {

  "Kitchen sink spec" should {

    "prove all the nested collections can read/write to Mongo" in {
      val ks = KitchenHelper() // make the enormous object
      val id = KitchenModel.insert(ks)
      val fromDb = KitchenModel.findOneById(id.get.asInstanceOf[ObjectId])
      fromDb.get must_== KitchenHelper.withNonesRemoved.copy(id = id.get)
    }
  }
}
