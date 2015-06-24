/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         PrettyPrinterSpec.scala
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

package com.github.salat.util

import com.github.salat._
import com.github.salat.test.SalatSpec
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._

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
