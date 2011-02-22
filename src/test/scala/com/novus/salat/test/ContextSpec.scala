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

import org.specs.specification.PendingUntilFixed
import com.novus.salat.test.model._
import com.novus.salat._
import com.novus.salat.global._
import scala.tools.nsc.util.ScalaClassLoader
import com.mongodb.casbah.Imports._

class ContextSpec extends SalatSpec {

  "A context" should {
    val j = James("Draino", true)
    val dbo = grater[James].asDBObject(j)

    "lookup_! graters" in {
      "by name of case class" in {
        ctx.lookup(James.getClass.getName) must beSome(grater[James])
      }
      "by instance of case class" in {
        ctx.lookup(j) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, j) must beSome(grater[James])
      }
      "by MongoDBObject" in {
        ctx.lookup(dbo) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, dbo) must beSome(grater[James])
      }
      "by type" in {
        ctx.lookup_![James] mustEqual grater[James]
      }
    }

    "extract type hints" in {
      "from dbo" in {
        ctx.extractTypeHint(dbo) must beSome("com.novus.salat.test.model.James")
      }
    }
  }

}