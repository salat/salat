/**
* @version $Id$
*/
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Implicits._
import java.math.{RoundingMode, MathContext}
import org.specs.specification.PendingUntilFixed
import scala.math.{BigDecimal => ScalaBigDecimal}

class BigDecimalPrecisionTest extends SalatSpec with PendingUntilFixed {



  "Salat grater" should {
    "preserve ScalaBigDecimal precision to 16 places" in {

      val mc = new MathContext(16, RoundingMode.HALF_UP)
      val PrecisePi = ScalaBigDecimal("3.1415926535897932384626433832795028841971693993", mc)

      val MorePrecisePi = ScalaBigDecimal("3.1415926535897932384626433832795028841971693993", new MathContext(22, RoundingMode.HALF_UP))

      val g = George(number = PrecisePi, someNumber = Some(PrecisePi), noNumber = None)

      val dbo: MongoDBObject = grater[George].asDBObject(g)
      dbo must havePair("number" -> PrecisePi)
      dbo must havePair("someNumber" -> PrecisePi)
      dbo must notHaveKey("noNumber")

      val coll =  MongoConnection()(SalatSpecDb)("scala_math_big_decimal_precision_test")
      val wr = coll.insert(dbo)
      println("WR: %s".format(wr))

      val g_* = grater[George].asObject(coll.findOne().get)
      g_* mustEqual g
      g_*.number.precision mustEqual 16
      g_*.someNumber.get.precision mustEqual 16

      g_*.number mustNotEq MorePrecisePi
      g_*.number mustEqual MorePrecisePi(mc)

    }

//    "preserve scala.BigDecimal precision to 16 places" in {
//
//      val mc = new MathContext(16, RoundingMode.HALF_UP)
//      val PrecisePi = scala.BigDecimal("3.1415926535897932384626433832795028841971693993", mc)
//
//      val MorePrecisePi = scala.BigDecimal("3.1415926535897932384626433832795028841971693993", new MathContext(22, RoundingMode.HALF_UP))
//
//      val h = Hector(number = PrecisePi, someNumber = Some(PrecisePi), noNumber = None)
//
//      val dbo: MongoDBObject = grater[Hector].asDBObject(h)
//      dbo must havePair("number" -> PrecisePi)
//      dbo must havePair("someNumber" -> PrecisePi)
//      dbo must notHaveKey("noNumber")
//
//      val coll =  MongoConnection()(SalatSpecDb)("scala_big_decimal_precision_test")
//      val wr = coll.insert(dbo)
////      println("WR: %s".format(wr))
//
//      val h_* = grater[Hector].asObject(coll.findOne().get)
//      h_* mustEqual h
//      h_*.number.precision mustEqual 16
//      h_*.someNumber.get.precision mustEqual 16
//
//      h_*.number mustNotEq MorePrecisePi
//      h_*.number mustEqual MorePrecisePi(mc)
//
//    }
  }


}
