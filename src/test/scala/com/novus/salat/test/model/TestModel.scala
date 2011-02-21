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

// Just a dummy data model. It's totally contrived, so don't hold it
// against me, neh?

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.test._

import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{Map => MMap}
import scala.math.{BigDecimal => ScalaBigDecimal}

case class Alice(x: String, y: Option[String] = Some("default y"), z: Basil)
case class Basil(p: Option[Int], q: Int = 1067, r: Clara)
case class Clara(l: Seq[String] = Nil, m: List[Int], n: List[Desmond])
case class Desmond(h: IMap[String, Alice], i: MMap[String, Int] = MMap.empty, j: Option[Basil])

case class Edward(a:          String,           b:        Int,           c:        ScalaBigDecimal,
             aa:  Option[String] = None,  bb: Option[Int] = None,  cc: Option[ScalaBigDecimal] = None,
             aaa: Option[String],        bbb: Option[Int],        ccc: Option[ScalaBigDecimal])

case class Fanny(@Key("complicated") es: List[Edward])

case class George(number: ScalaBigDecimal, someNumber: Option[ScalaBigDecimal], noNumber: Option[ScalaBigDecimal])
case class George2(number: scala.BigDecimal, someNumber: Option[scala.BigDecimal], noNumber: Option[scala.BigDecimal])

case class Hector(thug: ThugLevel.Value, doneIn: DoneIn.Value)
case class HectorOverrideId(thug: ThugLevel.Value, doneInById: DoneInById.Value)
case class HectorOverrideValue(thug: ThugLevel.Value, doneInByValue: DoneInByValue.Value)

object ThugLevel extends Enumeration("Fairplay Tony", "Honour student", "Just a good boy who loves his mum", "Trouble, you") {
  val One, Two, Three, Four = Value
}

object DoneIn extends Enumeration {
  val Napping, PiningForTheFjords, IsThereADoctorInTheHouse, OhDear, Definitely = Value
}

@EnumAs(strategy = EnumStrategy.BY_ID)
object DoneInById extends Enumeration {
  val Napping, PiningForTheFjords, IsThereADoctorInTheHouse, OhDear, Definitely = Value
}

@EnumAs(strategy = EnumStrategy.BY_VALUE)
object DoneInByValue extends Enumeration {
  val Napping, PiningForTheFjords, IsThereADoctorInTheHouse, OhDear, Definitely = Value
}

case class Ida(lake: Option[BigDecimal])

object James {
  def apply(lye: String): James = James(lye, true)
  def apply(byMistake: Boolean): James = James("Red Devil", byMistake)
  def apply(h: Hector): James = James(lye = h.thug.toString, byMistake = h.thug.id % 2 == 0)
}
case class James(lye: String, byMistake: Boolean)

case class Kate(axe: java.lang.Character, struckWith: Char)

case class Employee(name: String, age: Option[Int], annual_salary: Option[ScalaBigDecimal])
case class Department(name: String, head_honcho: Option[Employee], cya_factor: ScalaBigDecimal, minions: List[Employee])
case class Company(name: String, year_of_inception: Int, departments: Map[String, Department])

case class Walrus[W](manyThings: Seq[W])

case class LazyThing(excuses: Seq[Int]) {
  lazy val firstExcuse = excuses.headOption
  lazy val lastExcuse = excuses.lastOption
  lazy val factorial = {
    // the most shiftless way to calculate a factorial, i suppose.... replete with possibility of overflow.
    def loop(n: Int, acc: Int): Int = if (n <= 0) acc else loop(n - 1, acc * n)
    loop(excuses.length, 1)
  }
  lazy val nthDegree = List.range(1, factorial * factorial, factorial)
}

@Salat
trait Node {
  val name: String
  val cheat: String
}

case class ListNode(name: String, children: List[Node] = Nil, cheat: String = "list") extends Node
case class MapNode(name: String, children: Map[String, Node] = Map.empty[String, Node], cheat: String = "map") extends Node

object Frakked extends Enumeration {
  val JustALittle = Value("just a little")
  val QuiteABit = Value("quite a bit")
  val Majorly = Value("majorly")
  val BeyondRepair = Value("beyond repair")
}

case class Me(name: String, state: Frakked.Value = Frakked.BeyondRepair)

@Salat trait Suit
case object Zoot extends Suit
case object WhatArmstrongWore extends Suit
case class Wardrobe(suits: List[Suit])


@Salat
trait SomeCommonTrait
case class SomeSubclassExtendingSaidTrait(b: Int) extends SomeCommonTrait
case class AnotherSubclassExtendingSaidTrait(d: Double) extends SomeCommonTrait

case class SomeContainerClass(e: String, theListWhichNeedsToBeTested: List[SomeCommonTrait])
