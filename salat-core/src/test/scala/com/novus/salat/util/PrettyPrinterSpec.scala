package com.novus.salat.util

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.util._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import com.novus.salat.test.SalatSpec
import scala.reflect.Manifest

class PrettyPrinterSpec extends SalatSpec {
  "Constructor/input pretty printer" should {
    val g = grater[Able].asInstanceOf[ConcreteGrater[Able]]
    "flag all missing inputs" in {
      val s = ConstructorInputPrettyPrinter(g, Seq.empty)
      log.info(s)
      s must not beNull
    }
    "flag missing inputs" in {
      val s = ConstructorInputPrettyPrinter(g, Seq(new ObjectId))
      log.info(s)
      s must not beNull
    }
    "correctly register inputs that are present" in {
      val s = ConstructorInputPrettyPrinter(g, Seq(new ObjectId, Map.empty))
      log.info(s)
      s must not beNull
    }
    "flag too many inputs" in {
      val s = ConstructorInputPrettyPrinter(g, Seq(new ObjectId, Map("a" -> Thingy("a")), Option("foo")))
      log.info(s)
      s must not beNull
    }
  }
}