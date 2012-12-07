/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         TestModel.scala
 * Last modified: 2012-12-06 23:04:13 EST
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

// Just a dummy data model. It's totally contrived, so don't hold it
// against me, neh?

import com.novus.salat._
import com.novus.salat.annotations._
import scala.collection.immutable.{ Map => IMap }
import scala.collection.mutable.{ Map => MMap }
import scala.math.{ BigDecimal => ScalaBigDecimal }
import com.mongodb.casbah.Imports._

import org.scala_tools.time.Imports._

case class Alice(x: String, y: Option[String] = Some("default y"), z: Basil)
case class Basil(p: Option[Int], q: Int = 1067, r: Clara)
case class Clara(l: Seq[String] = Nil, m: List[Int], n: List[Desmond])
case class Desmond(h: IMap[String, Alice], i: MMap[String, Int] = MMap.empty, j: Option[Basil])

case class Edward(a: String, b: Int, c: ScalaBigDecimal,
                  aa: Option[String] = None, bb: Option[Int] = None, cc: Option[ScalaBigDecimal] = None,
                  aaa: Option[String], bbb: Option[Int], ccc: Option[ScalaBigDecimal])

case class Fanny(@Key("complicated") es: List[Edward])

case class George(number: ScalaBigDecimal, someNumber: Option[ScalaBigDecimal], noNumber: Option[ScalaBigDecimal])
case class George2(number: scala.BigDecimal, someNumber: Option[scala.BigDecimal], noNumber: Option[scala.BigDecimal])

case class Hector(thug: ThugLevel.Value, doneIn: DoneIn.Value)
case class HectorOverrideId(thug: ThugLevel.Value, doneInById: DoneInById.Value)
case class HectorOverrideValue(thug: ThugLevel.Value, doneInByValue: DoneInByValue.Value)

object ThugLevel extends Enumeration {
  //  "Fairplay Tony", "Honour student", "Just a good boy who loves his mum", "Trouble, you"
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
case class James2(@Key("cyanide") lye: String, byMistake: Boolean)

trait JamesLike {
  @Key("arsenic") val lye: String
}
case class James3(lye: String, byMistake: Boolean) extends JamesLike

abstract class JamesIsh {
  @Key("mercury") val lye: String
}
case class James4(lye: String, byMistake: Boolean) extends JamesIsh

case class Kate(axe: java.lang.Character, struckWith: Char)

case class Leo(swallowed: Option[BigInt], tacks: BigInt)

case class Maud(swept: String, out: String) {
  @Persist val toSea = "%s %s".format(out.reverse, swept.reverse)
}

case class Maud2(swept: String, out: String) {
  @Persist val ida = Ida(lake = Some(BigDecimal(swept.size + out.size)))
}

case class Maud3(swept: String, out: String) {
  @Persist var ida = Ida(lake = Some(BigDecimal(swept.size + out.size)))
}

case class Maud4(swept: String, out: String) {
  @Persist @Ignore val ida = Ida(lake = Some(BigDecimal(swept.size + out.size)))
  @Persist val toSea = "%s %s".format(out.reverse, swept.reverse)
}

@Salat
trait MaudLike {
  val swept: String
  val out: String
}
case class Maud5(swept: String, out: String) extends MaudLike {
  @Persist val toSea = "%s %s".format(out.reverse, swept.reverse)
}
case class Maud6(swept: String, out: String) extends MaudLike {
  @Persist val ida = Ida(lake = Some(BigDecimal(swept.size + out.size)))
}
case class Maud7(swept: String, out: String) extends MaudLike {
  val notWaving: Boolean = true
  @Persist val butDrowning: Boolean = true
}
case class ManyMauds(mauds: List[MaudLike])

@Salat
trait EvenMoreMaudLike {
  val swept: String
  val out: String
  @Persist val howFar = swept.size + out.size
}
case class Maud8(swept: String, out: String) extends EvenMoreMaudLike {
  @Persist val toSea = "%s %s".format(out.reverse, swept.reverse)
}
case class Maud9(swept: String, out: String) extends EvenMoreMaudLike {
  @Persist val ida = Ida(lake = Some(BigDecimal(swept.size + out.size)))
}
case class Maud10(swept: String, out: String) extends EvenMoreMaudLike {
  val notWaving: Boolean = true
  @Persist val butDrowning: Boolean = true
}
case class Maudelic(mauds: List[EvenMoreMaudLike])

abstract class MaudAgain() {
  val swept: String
  val out: String
  @Persist val howFar = swept.size + out.size
}
case class Maud11(swept: String, out: String) extends MaudAgain {
  @Persist val toSea = "%s %s".format(out.reverse, swept.reverse)
}

@Salat
trait AnnotatedMaud

trait UnannotatedMaud

@Salat
abstract class AbstractMaud()

abstract class UnannotatedAbstractMaud()

case class Neville(ennui: Boolean = true, asOf: DateTime = new DateTime)

case class Olive(awl: java.util.UUID)

case class Quentin(mire: Float)

case class LongSpecExample(timestamp: Long, value: Int)

case class Rhoda(consumed: Option[String] = None)
case class Rhoda2(howHot: Option[BigDecimal] = None)
case class Rhoda3(consumed: Option[String] = None)

object SuppressDefaults {
  val HowDefault = "who"
  val PerishedDefault = true
  val FitsDefault = Nil
  val AboutDefault = Map.empty[String, String]
}

case class Fit(length: Int = 3)
object Susan {
  val empty = Susan()
}
case class Susan(how: String = SuppressDefaults.HowDefault,
                 perished: Boolean = SuppressDefaults.PerishedDefault,
                 fits: List[Fit] = SuppressDefaults.FitsDefault,
                 about: Map[String, String] = SuppressDefaults.AboutDefault)

case class Susan2(how: String = "who", perished: Boolean = true, fits: List[Fit])

sealed trait Una
case object SlippedDownADrain extends Una

case class Employee(name: String, age: Option[Int], annual_salary: Option[ScalaBigDecimal])
case class Department(name: String, head_honcho: Option[Employee], cya_factor: ScalaBigDecimal, minions: List[Employee])
case class Company(name: String, year_of_inception: Int, departments: Map[String, Department])
case class HasCompany(c: Company)

case class Walrus[W](manyThings: Seq[W])
case class ElephantSeal[ES](distinctThings: Set[ES])

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

case class AttributeObject(_id: Long, key: String, bestDef: String, urls: IMap[String, UrlID])
case class UrlID(dh: Long, ph: Long)

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

@Salat
sealed abstract class Vertebrate
case class Bird(name: String, canFly: Boolean = true) extends Vertebrate
case class Squirrel(name: String, canFly: Boolean = false) extends Vertebrate
case class VertebrateList(vList: List[Vertebrate]);

case class Ad(slogan: String)
case class Page(
  @Key("_id") uri: java.net.URI,
  crawled: List[DateTime] = Nil,
  ads: Option[Set[Ad]] = None,
  title: Option[String] = None,
  description: Option[String] = None,
  keywords: Option[String] = None)

class NotACaseClass(x: String)

@Salat
trait SomeTrait
case class SomeTraitImpl1(x: String) extends SomeTrait
case class SomeTraitImpl2(y: Int) extends SomeTrait

case class ContainsFieldTypedToTrait(someTrait: SomeTrait)

@Salat
trait Contract {
  val name: String
}
@Salat
trait Security extends Contract {
  val ticker: String
}
case class Stock(name: String, ticker: String) extends Security
case class Turbo(name: String, ticker: String) extends Security
case class Index(name: String) extends Contract

case class Investments(contracts: List[Contract])

case class Titus(@Ignore ignoreMe: String = "bits", dontIgnoreMe: Int)
case class Titus2(@Ignore ignoreMe: String = null, dontIgnoreMe: Int)
case class SomeClassWithUnsupportedField(@Key("_id") val id: ObjectId = new ObjectId,
                                         text: Option[String] = None,
                                         @Ignore unsupportedType: java.io.File = null)

@Salat
sealed trait SomeStatus
case object Borked extends SomeStatus

case class SomeClassWithUnsupportedField2(@Key("_id") val id: ObjectId = new ObjectId,
                                          email: String,
                                          status: SomeStatus,
                                          @Ignore cascade: Map[Int, Set[Int]] = Map.empty,
                                          thingy: Option[Int] = None,
                                          created: DateTime = DateTime.now,
                                          updated: DateTime = DateTime.now)

// Issue #24
case class MetadataRecord(
  validOutputFormats: List[String] = List.empty[String], // valid formats this records can be mapped to
  transferIdx: Option[Int] = None, // 0-based index for the transfer order
  deleted: Boolean = false // if the record has been deleted
  )

