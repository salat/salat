package com.bumnetworks.salat.test.model

// Just a dummy data model. It's totally contrived, so don't hold it
// against me, neh?

import com.bumnetworks.salat._
import com.bumnetworks.salat.global._
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

object `package` {
  implicit object GraterA extends Grater(classOf[A])
  implicit object GraterB extends Grater(classOf[B])
  implicit object GraterC extends Grater(classOf[C])
  implicit object GraterD extends Grater(classOf[D])
  implicit object GraterE extends Grater(classOf[E])

  def graph = A("x", Some("y"),
                B(Some(80), 81,
                  C(Seq("l1", "l2"), List(1, 2), List(
                    D(IMap("foo1" -> A("foo", None, null), "baz1" -> A("baz", Some("quux"), null)),
                      MMap("a1" -> 1, "c1" -> 2),
                      Some(B(
                        None, 24, C(
                          List("l3", "l4"), List(1, 2, 3), Nil))))))))

  def numbers = E(a = "a value",                    aa = None, aaa = Some("aaa value"),
                  b = 2,                            bb = None, bbb = Some(22),
                  c = ScalaBigDecimal(3.30003),     cc = None, ccc = Some(ScalaBigDecimal(33.30003)))
}
