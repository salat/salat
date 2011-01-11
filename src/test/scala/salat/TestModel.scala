package com.bumnetworks.salat.test.model

// Just a dummy data model. It's totally contrived, so don't hold it
// against me, neh?

import com.bumnetworks.salat._
import com.bumnetworks.salat.global._
import com.bumnetworks.salat.annotations._
import com.bumnetworks.salat.test._

import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{Map => MMap}
import scala.math.{BigDecimal => ScalaBigDecimal}

case class A(x: String, y: Option[String] = Some("default y"), z: B)
case class B(p: Option[Int], q: Int = 1067, r: C)
case class C(l: Seq[String] = Nil, m: List[Int], n: List[D])
case class D(h: IMap[String, A], i: MMap[String, Int] = MMap.empty, j: Option[B])

case class E(a:          String,           b:        Int,           c:        ScalaBigDecimal,
             aa:  Option[String] = None,  bb: Option[Int] = None,  cc: Option[ScalaBigDecimal] = None,
             aaa: Option[String],        bbb: Option[Int],        ccc: Option[ScalaBigDecimal])

case class F(@Key("complicated") es: List[E])

case class Employee(name: String, age: Option[Int], annual_salary: Option[ScalaBigDecimal])
case class Department(name: String, head_honcho: Option[Employee], cya_factor: ScalaBigDecimal, minions: List[Employee])
case class Company(name: String, year_of_inception: Int, departments: Map[String, Department])

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

object `package` {
  def graph = A("x", Some("y"),
                B(Some(80), 81,
                  C(Seq("l1", "l2"), List(1, 2), List(
                    D(IMap("foo1" -> A("foo", None, B(p = None, r = C(m = Nil, n = Nil))),
                           "baz1" -> A("baz", Some("quux"), B(p = None, r = C(m = Nil, n = Nil)))),
                      MMap("a1" -> 1, "c1" -> 2),
                      Some(B(
                        None, 24, C(
                          List("l3", "l4"), List(1, 2, 3), Nil))))))))

  def numbers = E(a = "a value",                    aa = None, aaa = Some("aaa value"),
                  b = 2,                            bb = None, bbb = Some(22),
                  c = ScalaBigDecimal(3.30003),     cc = None, ccc = Some(ScalaBigDecimal(33.30003)))

  def mucho_numbers(factor: Long = 10) = F((0L until factor).toList.map {
    i =>
      E(
        a = "a %d".format(i), aa = Some("aa %d".format(i)), aaa = None,
        b = (i*i*123/1000).toInt, bb = None, bbb = Some((i*i*321/100).toInt),
        c = ScalaBigDecimal((i*i).toDouble/123d, mathCtx), cc = None, ccc = None
      )})

  def evil_empire = Company(name = "Evil Empire, Inc.",
                            year_of_inception = 2000,
                            departments = Map(
                              "MoK" -> Department(name = "Murder of Kittens",
                                                  head_honcho = Some(Employee("Dick Cheney", None, Some(ScalaBigDecimal(12345.6789)))),
                                                  cya_factor = ScalaBigDecimal(0.01980082),
                                                  minions = List(
                                                    Employee("George W. Bush", Some(66), None),
                                                    Employee(name = "Michael Eisner", age = None, annual_salary = None)
                                                  )
                                                ),
                              "FOSS_Sabotage" -> Department(name = "Sabotage of F/OSS projects",
                                                            head_honcho = Some(Employee("Bill Gates", None, Some(ScalaBigDecimal(28823.772383)))),
                                                            cya_factor = ScalaBigDecimal(14.982023002),
                                                            minions = List(
                                                              Employee(name = "Darl McBridge", age = Some(123), annual_salary = None),
                                                              Employee(name = "Patent Trolls Everywhere", age = None, annual_salary = Some(ScalaBigDecimal(1000000.00022303)))
                                                            )
                                                          )
                            )
                          )
}
