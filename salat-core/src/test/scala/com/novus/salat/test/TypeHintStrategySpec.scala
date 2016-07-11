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
package com.novus.salat.test

import com.novus.salat.{BinaryTypeHintStrategy, _}
import com.novus.salat.test.model._
import com.novus.salat.util.Logging
import com.novus.salat.util.encoding.TypeHintEncoding
import org.specs2.mutable.Specification

class TypeHintStrategySpec extends Specification with Logging {

  "Binary type hint strategy" should {
    val bths = BinaryTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "t", encoding = TypeHintEncoding.UsAsciiEncoding)
    "handle a class name" in {
      val clazzName = classOf[SomeSubclassExtendingSaidTrait].getName
      val expected = BigInt("692984074727956783039651647384290191570936777613175552267852671289279014922270519922204214854615810047")
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
