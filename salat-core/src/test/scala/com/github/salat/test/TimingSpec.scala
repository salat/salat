/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         TimingSpec.scala
 * Last modified: 2015-06-23 20:52:14 EDT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package com.github.salat.test

import com.github.salat._
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.github.salat.util.Logging
import com.mongodb.casbah.Imports._
import org.apache.commons.lang.RandomStringUtils.{randomAscii => rs}
import org.apache.commons.lang.math.RandomUtils.{nextInt => rn}
import org.specs2.execute.{PendingUntilFixed, Success}
import org.specs2.mutable._

import scala.collection.mutable.ArrayBuffer
import scala.math.{BigDecimal => ScalaBigDecimal}

class TimingSpec extends Specification with PendingUntilFixed with Logging {

  object NodeCounter {
    var n: Int = _
  }

  // TODO: operator, can you connect with with detailed diffs?  - i'm sorry, that party is no longer available.
  //  detailedDiffs()

  trait context extends Success {
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

    "try to beat casbah-mapper, too" in new context {
      val outTimes = ArrayBuffer.empty[Long]
      val inTimes = ArrayBuffer.empty[Long]

      val tree = timeAndLog {
        ListNode(name = "top level", children = List(node(10).get))
      } {
        m => log.info("generated %d nodes in %d msec", NodeCounter.n, m)
      }

      log.info("%s bytes deflated", grater[ListNode].asDBObject(tree).toString.length)

      (0 until 50).foreach {
        _ =>
          {
            timeAndLog {
              grater[ListNode].asDBObject(tree)
            } {
              m =>
                log.info("%s deflation time: %d msec", tree.getClass.getName, m)
                outTimes += m
            }
          }
      }

      val deflated = grater[ListNode].asDBObject(tree)

      (0 until 50).foreach {
        _ =>
          {
            timeAndLog {
              grater[ListNode].asObject(deflated)
            } {
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
      }
      else {
        Some(MapNode(crap, Map.empty[String, Node] ++ (1 to many).toList.map {
          _ =>
            node(level - 1) match {
              case Some(n) => Some(n.name -> n)
              case _       => None
            }
        }.filter(_.isDefined).map(_.get)))
      }
    }
  }

  private val many = 3

  private def _many: Int =
    rn(10) match {
      case n if n < 3 => many
      case n          => n
    }

  private def crap: String = rs(many)

}
