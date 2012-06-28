/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         PrettyPrinterSpec.scala
 * Last modified: 2012-06-27 23:42:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */

package com.novus.salat.util

import com.novus.salat._
import com.novus.salat.test.global._
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