/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         ProxySpec.scala
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
import com.mongodb.casbah.Imports._
import org.specs2.execute.PendingUntilFixed

class ProxySpec extends SalatSpec with PendingUntilFixed {
  "a proxy grater" should {
    """delegate heavy lifting to correct "concrete" graters""" in {
      val i1 = SomeTraitImpl1("foo")
      val i2 = SomeTraitImpl2(5)

      val proxy = new ProxyGrater(classOf[SomeTrait])

      val dbo1: MongoDBObject = proxy.asDBObject(i1)
      val dbo2: MongoDBObject = proxy.asDBObject(i2)

      log.trace("""
                dbo1 = %s
                dbo2 = %s
                """, dbo1, dbo2)

      dbo1 must havePair("_typeHint" -> classOf[SomeTraitImpl1].getName)
      dbo2 must havePair("_typeHint" -> classOf[SomeTraitImpl2].getName)

      success
    }
  }
}
