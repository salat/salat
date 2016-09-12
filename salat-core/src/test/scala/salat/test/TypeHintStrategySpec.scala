/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         TypeHintStrategySpec.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
package salat.test

import salat.{BinaryTypeHintStrategy, _}
import salat.test.model._
import salat.util.Logging
import salat.util.encoding.TypeHintEncoding
import org.specs2.mutable.Specification

class TypeHintStrategySpec extends Specification with Logging {

  "Binary type hint strategy" should {
    val bths = BinaryTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "t", encoding = TypeHintEncoding.UsAsciiEncoding)
    "handle a class name" in {
      val clazzName = classOf[SomeSubclassExtendingSaidTrait].getName
      // http://stackoverflow.com/questions/4182029/how-to-convert-byte-array-to-biginteger-in-java
      val expected = BigInt("514742097497111975556240938723917692093828340540365521870232755356320865506384270702")

      "encode class name as BitInt" in {
        bths.encode(clazzName) must_== expected.toByteArray
      }
      "decode a BigInt into a class name" in {
        bths.decode(expected.toByteArray) must_== clazzName
      }
      "for legacy purposes, pass a string type hint back through" in {
        bths.decode(clazzName) must_== clazzName
      }
      "aggressively memoizes" in {
        for (i <- 1 to 100) {
          bths.encode(clazzName)
          bths.decode(expected)
        }
        success
      }
    }
    "encode case object classname as a string" in {
      Zoot.getClass.getName must endWith("$")
      bths.encode(Zoot.getClass.getName) must_== Zoot.getClass.getName
    }

  }

}
