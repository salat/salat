package com.bumnetworks.salat.test

import com.bumnetworks.salat._
import com.bumnetworks.salat.global._
import com.bumnetworks.salat.test.model._
import com.mongodb.casbah.Imports._

import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import org.specs._
import org.specs.specification.PendingUntilFixed

object SalatSpec extends Specification with PendingUntilFixed with CasbahLogging {
  detailedDiffs()

  doBeforeSpec {
    com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
  }

  "a grater" should {
    "make DBObject-s out of case class instances" in {
      "properly treat primitive values and optional values" in {
        val e = numbers
        val dbo: MongoDBObject = grater[E].asDBObject(e)

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
        val dbo: MongoDBObject = grater[A].asDBObject(a)
        log.info("before: %s", a)
        log.info("after : %s", dbo.asDBObject)
        dbo must havePair("x" -> "x")
      }
    }

    "instantiate case class instances using data from DBObject-s" in {
      "cover primitive types" in {
        val e = numbers
        val e_* = grater[E].asObject(grater[E].asDBObject(e))
        e_* must_== e
      }

      "and silly object graphs" in {
        val a = graph
        val a_* = grater[A].asObject(grater[A].asDBObject(a))
        // these two checks are *very* naive, but it's hard to compare
        // unordered maps and expect them to come out equal.
        a_*.z.p must_== a.z.p
        a_*.z.q must_== a.z.q
      }

      "and also object graphs of even sillier shapes" in {
        val f = mucho_numbers()
        val dbo: MongoDBObject = grater[F].asDBObject(f)
        dbo.get("complicated") must beSome[AnyRef]
        val f_* = grater[F].asObject(dbo)
        f_* must_== f
      }
    }

    "work with Scala enums" in {
      "be able to serialize Scala enums" in {
        val me = Me("max")
        val g = grater[Me]
        val dbo: MongoDBObject = g.asDBObject(me)
        dbo("_typeHint") must_== classOf[Me].getName
        dbo("state") must_== Frakked.BeyondRepair.toString
      }

      "be able to deserialize Scala enums" in {
        val me = Me("max")
        val g = grater[Me]
        val dbo = g.asDBObject(me)
        val me_* = g.asObject(dbo)
        me must_== me_*
      }
    }

    "support case objects" in {
      "be able to serialize case objects" in {
	val mine = Wardrobe(suits = List(Zoot))
	log.info("mine: %s", mine)
	val dbo: MongoDBObject = grater[Wardrobe].asDBObject(mine)
	log.info("dbo : %s", dbo)
	val suits = dbo.expand[BasicDBList]("suits")
	suits must beSome[BasicDBList].which {
	  suits => val suit: MongoDBObject = suits(0).asInstanceOf[DBObject]
	  val th = suit.expand[String]("_typeHint")
	  th must beSome[String].which { th => th == Zoot.getClass.getName }
	}
      }

      "be able to deserialize case objects" in {
	val mine = Wardrobe(suits = List(WhatArmstrongWore, Zoot))
	val dbo = grater[Wardrobe].asDBObject(mine)
	val mine_* = grater[Wardrobe].asObject(dbo)
	mine must_== mine_*
      }
    }
  }

  "usage example for the README" should {
    val deflate_me = evil_empire

    "print out some sample JSON" in {
      val deflated = grater[Company].asDBObject(deflate_me)
      val inflated = grater[Company].asObject(deflated)
      inflated.copy(departments = Map.empty) must_== deflate_me.copy(departments = Map.empty)
      inflated.departments("MoK") must_== deflate_me.departments("MoK")
      inflated.departments("FOSS_Sabotage") must_== deflate_me.departments("FOSS_Sabotage")
    }
  }

  "an ObjectId shortener" should {
    "shorten ObjectId-s" in {
      val oid = new ObjectId("4d2ba030eb79807454ca5cbf")
      val shortened = oid.asShortString
      shortened must_== "2bcw4m7j7ycxzdvgzwf"
    }

    "explode ObjectId-s back from shortened strings" in {
      val oid = new ObjectId
      val shortened = oid.asShortString
      shortened.asObjectId must_== oid
    }
  }
}
