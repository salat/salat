package com.bumnetworks.salat.test

import com.bumnetworks.salat._
import com.bumnetworks.salat.test.model._

import scala.tools.scalap.scalax.rules.scalasig._

import org.specs._
import org.specs.specification.PendingUntilFixed

class SalatSpec extends Specification with PendingUntilFixed with CasbahLogging {
  detailedDiffs()

  doBeforeSpec {
    GraterA
    GraterB
    GraterC
    GraterD
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

  "field unapplies" should {
    "correctly detect Option[_]" in {
      "primitive Option[_]" in {
        val arg = implicitly[Grater[A]].fields("y").typeRefType match {
          case IsOption(ot @ TypeRefType(_,_,_)) => Some(ot)
          case _ => None
        }
        arg must beSome[TypeRefType].which {
          t => t.symbol.path.split("\\.").last must_== "String"
        }
      }

      "Option[_] with type arg in context" in {
        val arg = implicitly[Grater[D]].fields("j").typeRefType match {
          case IsOption(ot @ TypeRefType(_,_,_)) => Some(ot)
          case _ => None
        }
        arg must beSome[TypeRefType].which {
          t => t.symbol.path must_== classOf[B].getName
	  implicitly[Grater[D]].ctx.graters must haveKey(t.symbol.path)
        }
      }
    }

    "correctly detect Map[_, _]" in {
      "with primitive value type" in {
        val arg = implicitly[Grater[D]].fields("i").typeRefType match {
          case IsMap(k, v @ TypeRefType(_,_,_)) => Some(v)
          case _ => None
        }
        arg must beSome[TypeRefType].which {
          t => t.symbol.path.split("\\.").last must_== "Int"
        }
      }

      "with something in context" in {
        val arg = implicitly[Grater[D]].fields("h").typeRefType match {
          case IsMap(k, v @ TypeRefType(_,_,_)) => Some(v)
          case _ => None
        }
        arg must beSome[TypeRefType].which {
          t => t.symbol.path must_== classOf[A].getName
	  implicitly[Grater[D]].ctx.graters must haveKey(t.symbol.path)
        }
      }
    }

    "correctly detect Seq[_]" in {
      "with primitive value type" in {
        val arg = implicitly[Grater[C]].fields("l").typeRefType match {
          case IsSeq(e @ TypeRefType(_,_,_)) => Some(e)
          case _ => None
        }
        arg must beSome[TypeRefType].which {
          t => t.symbol.path.split("\\.").last must_== "String"
        }
      }

      "with something in context" in {
        val arg = implicitly[Grater[C]].fields("n").typeRefType match {
          case IsSeq(e @ TypeRefType(_,_,_)) => Some(e)
          case _ => None
        }
        arg must beSome[TypeRefType].which {
          t => t.symbol.path must_== classOf[D].getName
	  implicitly[Grater[C]].ctx.graters must haveKey(t.symbol.path)
        }
      }
    }
  }
}
