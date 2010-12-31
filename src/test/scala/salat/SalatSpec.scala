package com.bumnetworks.salat.test

import com.bumnetworks.salat._
import com.bumnetworks.salat.test.model._

import org.specs._
import org.specs.specification.PendingUntilFixed

class SalatSpec extends Specification with PendingUntilFixed with CasbahLogging {
  detailedDiffs()

  doBeforeSpec {
    GraterA
  }

  "a grater" should {
    "come to life" in {
      implicitly[Grater[A]] must_== GraterA
    }

    "perhaps even multiple times" in {
      implicitly[Grater[A]] must notBe(GraterB)
      implicitly[Grater[B]] must notBe(GraterA)
    }
  }
}
