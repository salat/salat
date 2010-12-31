package com.bumnetworks.salat.test.model

// Just a dummy data model. It's totally contrived, so don't hold it
// against me, neh?

case class A(x: String, y: Option[String], z: B)
case class B(p: Option[Int], q: Int, r: C)
case class C(l: Seq[String], m: List[Int], n: List[D])
case class D(h: Map[String, A], i: Seq[A], j: B)
