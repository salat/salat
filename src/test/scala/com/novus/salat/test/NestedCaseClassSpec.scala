/**
* Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
* For questions and comments about this product, please see the project page at:
*
* http://github.com/novus/salat
*
*/
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class NestedCaseClassSpec extends SalatSpec {

  // https://github.com/novus/salat/issues#issue/1

  "a grater" should {

    "handle a case object nested in a trait" in {

      val e = TestApp.NestedCaseClassInATrait(foo = "Some Foo", bar = Some(-99), baz = Some(BigDecimal(scala.math.Pi.toString)))

      // fails because signature can't be found  (parseScalaSig: clazz=com.novus.salat.test.model.SomeTrait$NestedCaseClassInATrait)
      val dbo: MongoDBObject = grater[TestApp.NestedCaseClassInATrait].asDBObject(e)

      dbo must havePair("foo" -> e.foo)
      dbo must havePair("bar" -> e.bar.getOrElse(0))
      dbo must havePair("baz" -> e.baz.getOrElse(BigDecimal("0")))

      val e_* = grater[TestApp.NestedCaseClassInATrait].asObject(dbo)
      e_* mustEqual e

    } pendingUntilFixed

  }

}