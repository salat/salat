/** @version $Id$
 */
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.MapPrettyPrinter

class OptionSupportSpec extends SalatSpec {
  "a grater" should {
    "support Option[String]" in {
      "with value" in {
        val r = Rhoda(consumed = Some("flames"))
        val dbo: MongoDBObject = ctx.toDBObject(r)
        log.info(MapPrettyPrinter(dbo))
        dbo must havePair("_typeHint", "com.novus.salat.test.model.Rhoda")
        dbo must havePair("consumed", "flames")

        val r_* = grater[Rhoda].asObject(dbo)
        r_*.consumed must beSome("flames")
        r_* must_== r
      }
      //      "with no value" in {
      //        val r = Rhoda(consumed = None)
      //        val dbo: MongoDBObject = ctx.toDBObject(r)
      //        dbo must havePair("_typeHint", "com.novus.salat.test.model.Rhoda")
      //        // TODO: what happened to must not haveKey
      //
      //        val r_* = grater[Rhoda].asObject(dbo)
      //        r_*.consumed must beNone
      //        r_* must_== r
      //      }
    }
    //    "support Option[BigDecimal]" in {
    //      "with value" in {
    //        val temp = BigDecimal("451")
    //        val r = Rhoda2(howHot = Some(temp))
    //        val dbo: MongoDBObject = ctx.toDBObject(r)
    //        dbo must havePair("_typeHint", "com.novus.salat.test.model.Rhoda2")
    //        dbo must havePair("howHot", 451.0)
    //
    //        val r_* = grater[Rhoda2].asObject(dbo)
    //        r_*.howHot must beSome(temp)
    //        r_* must_== r
    //      }
    //      "with no value" in {
    //        val r = Rhoda2(howHot = None)
    //        val dbo: MongoDBObject = ctx.toDBObject(r)
    //        dbo must havePair("_typeHint", "com.novus.salat.test.model.Rhoda2")
    //        // TODO: what happened to must not haveKey
    //
    //        val r_* = grater[Rhoda2].asObject(dbo)
    //        r_*.howHot must beNone
    //        r_* must_== r
    //      }
    //    }
  }
}