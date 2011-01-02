package com.bumnetworks.salat.test.model

// Just a dummy data model. It's totally contrived, so don't hold it
// against me, neh?

import com.bumnetworks.salat._
import com.bumnetworks.salat.global._
import com.bumnetworks.salat.test._

import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{Map => MMap}
import scala.math.{BigDecimal => ScalaBigDecimal}

import java.math.{BigDecimal => JavaBigDecimal}

case class A(x: String,           y: Option[String],    z: B)
case class B(p: Option[Int],      q: Int,               r: C)
case class C(l: Seq[String],      m: List[Int],         n: List[D])
case class D(h: IMap[String, A],  i: MMap[String, Int], j: Option[B])

case class E(a:          String,    b:        Int,        c:        ScalaBigDecimal,    d:        JavaBigDecimal,
             aa:  Option[String],  bb: Option[Int],      cc: Option[ScalaBigDecimal],  dd: Option[JavaBigDecimal],
             aaa: Option[String], bbb: Option[Int],     ccc: Option[ScalaBigDecimal], ddd: Option[JavaBigDecimal])

object `package` {
  implicit object GraterA extends Grater(classOf[A])
  implicit object GraterB extends Grater(classOf[B])
  implicit object GraterC extends Grater(classOf[C])
  implicit object GraterD extends Grater(classOf[D])
  implicit object GraterE extends Grater(classOf[E])

  def graph = A("x", Some("y"),
                B(Some(80), 81,
                  C(Seq("l1", "l2"), List(1, 2), Nil)))
}
