package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{BigDecimal => ScalaBigDecimal}

import org.specs._
import org.specs.specification.PendingUntilFixed

import org.apache.commons.lang.RandomStringUtils.{randomAscii => rs}
import org.apache.commons.lang.math.RandomUtils.{nextInt => rn}

class TimingSpec extends Specification with PendingUntilFixed with CasbahLogging {
  object NodeCounter {
    var n: Int = _
  }

  detailedDiffs()

  doBeforeSpec {
    NodeCounter.n = 0
    com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
  }

  "performance" should {
    // "be at least acceptable" in {
    //   val numbers = timeAndLog {
    //     mucho_numbers(1000)
    //   } { m => log.info("made big ass object in %d msec", m) }

    //   val times: Buffer[Long] = ArrayBuffer.empty
    //   (0 until 50).foreach {
    //     _ => timeAndLog {
    //       grater[F].asDBObject(numbers)
    //     } {
    //       m =>
    //         log.info("%s deflation time: %d msec", numbers.getClass.getName, m)
    //       times += m
    //     }
    //   }

    //   val avg = times.sum / times.size.toDouble
    //   log.info("min / avg / max deflation times: %d msec / %f msec / %d msec", times.min, avg, times.max)
    // }

    "try to beat casbah-mapper, too" in {
      val outTimes = ArrayBuffer.empty[Long]
      val inTimes = ArrayBuffer.empty[Long]

      val tree = timeAndLog {
	ListNode(name = "top level", children = List(node(10).get))
      } { m => log.info("generated %d nodes in %d msec", NodeCounter.n, m) }

      log.info("%s bytes deflated", grater[ListNode].asDBObject(tree).toString.length)

      (0 until 50).foreach {
        _ => {
	  timeAndLog { grater[ListNode].asDBObject(tree) } {
	    m =>
	      log.info("%s deflation time: %d msec", tree.getClass.getName, m)
	    outTimes += m
	  }
        }
      }

      val deflated = grater[ListNode].asDBObject(tree)

      (0 until 50).foreach {
        _ => {
	  timeAndLog { grater[ListNode].asObject(deflated) } {
	    m =>
	      log.info("%s inflation time: %d msec", tree.getClass.getName, m)
	    outTimes += m
	  }
        }
      }

      1 must_== 1 // we're here, so not all is lost
    }
  }

  private def node(level: Int): Option[Node] = {
    if (level == 0) None
    else {
      NodeCounter.n += 1
      if (rn(10) % 2 == 0) {
        Some(ListNode(crap, (1 to many).toList.map(_ => node(level - 1)).filter(_.isDefined).map(_.get)))
      } else {
        Some(MapNode(crap, Map.empty[String, Node] ++ (1 to many).toList.map {
          _ =>
            node(level - 1) match {
              case Some(n) => Some(n.name -> n)
              case _ => None
            }
        }.filter(_.isDefined).map(_.get)))
      }
    }
  }

  private val many = 3

  private def _many: Int =
    rn(10) match {
      case n if n < 3 => many
      case n => n
    }

  private def crap: String = rs(many)

}
