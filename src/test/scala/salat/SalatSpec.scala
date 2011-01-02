package com.bumnetworks.salat.test

import com.bumnetworks.salat._
import com.bumnetworks.salat.test.model._
import com.mongodb.casbah.Imports._

import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import org.specs._
import org.specs.specification.PendingUntilFixed

object SalatSpec extends Specification with PendingUntilFixed with CasbahLogging {
  detailedDiffs()

  doBeforeSpec {
    com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()

    GraterA
    GraterB
    GraterC
    GraterD
    GraterE
  }

  "a grater" should {
    "come to life" in {
      implicitly[Grater[A]] must_== GraterA
    }

    "perhaps even multiple times" in {
      implicitly[Grater[A]] must notBe(GraterB)
      implicitly[Grater[B]] must notBe(GraterA)
    }

    "make DBObject-s out of case class instances" in {
      "properly treat primitive values and optional values" in {
        val e = numbers
        val dbo: MongoDBObject = GraterE.asDBObject(e)

        log.info("before: %s", e)
        log.info("after : %s", dbo.asDBObject)

        dbo must havePair("a" -> e.a)
        dbo must notHaveKey("aa")
        dbo must havePair("aaa" -> e.aaa.get)

        dbo must havePair("b" -> e.b)
        dbo must notHaveKey("bb")
        dbo must havePair("bbb" -> e.bbb.get)

        dbo must havePair("c" -> e.c)
        dbo must notHaveKey("cc")
        dbo must havePair("ccc" -> e.ccc.get)
      }

      "work with object graphs" in {
        val a = graph
        val dbo: MongoDBObject = GraterA.asDBObject(a)
        log.info("before: %s", a)
        log.info("after : %s", dbo.asDBObject)
        dbo must havePair("x" -> "x")
      }
    }

    "instantiate case class instances using data from DBObject-s" in {
      "cover primitive types" in {
        val e = numbers
        val e_* = GraterE.asObject(GraterE.asDBObject(e))
        e_* must_== e
      }

      "and object graphs" in {
        // val a = graph
        // val a_* = GraterA.asObject(GraterA.asDBObject(a))
        // a_* must_== a
	fail
      } pendingUntilFixed
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
