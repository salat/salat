/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.test

import com.novus.salat.test.model._
import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.MapPrettyPrinter

class ContextSpec extends SalatSpec {

  val j = James("Draino", true)

  "A context that uses type hints only when necessary" should {
    import com.novus.salat.test.when_necessary._

    "lookup_! graters" in {

      "by name of case class" in {
        ctx.lookup(James.getClass.getName) must beSome(grater[James])
      }

      "by instance of case class" in {
        ctx.lookup(j) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, j) must beSome(grater[James])
      }

      "by type" in {
        ctx.lookup_![James] must_== grater[James]
      }
    }
  }

  "A context that never uses type hints" should {
    import com.novus.salat.test.never._

    "lookup_! graters" in {

      "by name of case class" in {
        ctx.lookup(James.getClass.getName) must beSome(grater[James])
      }

      "by instance of case class" in {
        ctx.lookup(j) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, j) must beSome(grater[James])
      }

      "by type" in {
        ctx.lookup_![James] must_== grater[James]
      }
    }
  }

  "A context that always uses type hints" should {

    "lookup_! graters" in {

      import com.novus.salat.test.always._
      val dbo = grater[James].asDBObject(j)

      "by MongoDBObject" in {
        val dbo = grater[James].asDBObject(j)
        ctx.lookup(dbo) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, dbo) must beSome(grater[James])
      }

      "by name of case class" in {
        ctx.lookup(James.getClass.getName) must beSome(grater[James])
      }

      "by instance of case class" in {
        ctx.lookup(j) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, j) must beSome(grater[James])
      }

      "by type" in {
        ctx.lookup_![James] must_== grater[James]
      }
    }

    "extract type hints" in {
      import com.novus.salat.test.always._
      "from dbo" in {
        val dbo = grater[James].asDBObject(j)
        ctx.extractTypeHint(dbo) must beSome("com.novus.salat.test.model.James")
      }
    }

    "allow the use of implicits to do clever things" in {
      import com.novus.salat.test.always_with_implicits._

      "implicitly convert case class <-> dbo" in {
        val coll = MongoConnection()(SalatSpecDb)("context_test_1")
        val wr = coll += j // implicit conversion from case class James to DBObject
        //        log.info("WR: %s", wr)
        val j_* : James = coll.findOne().get // implicit conversion from DBObject to case class James
        j_* must_== j
      }
    }
  }

  "A context that uses a custom key for type hints" should {
    "lookup_! graters" in {

      import com.novus.salat.test.custom_type_hint._
      val dbo = grater[James].asDBObject(j)

      "by MongoDBObject" in {
        val dbo = grater[James].asDBObject(j)
        ctx.lookup(dbo) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, dbo) must beSome(grater[James])
      }

      "by name of case class" in {
        ctx.lookup(James.getClass.getName) must beSome(grater[James])
      }

      "by instance of case class" in {
        ctx.lookup(j) must beSome(grater[James])
        ctx.lookup(James.getClass.getName, j) must beSome(grater[James])
      }

      "by type" in {
        ctx.lookup_![James] must_== grater[James]
      }
    }

    "extract type hints" in {
      import com.novus.salat.test.custom_type_hint._
      "from dbo" in {
        val dbo: MongoDBObject = grater[James].asDBObject(j)
        dbo must havePair("_t", "com.novus.salat.test.model.James") // custom context uses "_t" as typeHint
        ctx.extractTypeHint(dbo) must beSome("com.novus.salat.test.model.James")
      }
    }
  }
}