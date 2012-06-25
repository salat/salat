/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         ProxySpec.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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

package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.test.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.test.model._
import org.specs2.execute.{ Success, PendingUntilFixed }

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

      dbo1 must havePair("_typeHint", classOf[SomeTraitImpl1].getName)
      dbo2 must havePair("_typeHint", classOf[SomeTraitImpl2].getName)

      success
    }
  }
}
