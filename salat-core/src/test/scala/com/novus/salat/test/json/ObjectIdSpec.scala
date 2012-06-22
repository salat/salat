/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         ObjectIdSpec.scala
 * Last modified: 2012-06-22 00:16:11 EDT
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

package com.novus.salat.test.json

import org.bson.types.ObjectId
import org.specs2.mutable.Specification
import com.novus.salat.util.Logging
import com.novus.salat.json.{ StrictJSONObjectIdStrategy, StringObjectIdStrategy }
import net.liftweb.json.JsonAST.{ JField, JObject, JString }

class ObjectIdSpec extends Specification with Logging {

  val oid = "4fdf3bc2c89cd58b22f27811"
  val _id = new ObjectId(oid)
  val jval = JString(oid)

  "JSON ObjectId strategy" should {
    "string" in {
      val s = StringObjectIdStrategy
      "from ObjectId to string" in {
        s.out(_id) must_== jval
      }
      "from string to ObjectId" in {
        s.in(jval) must_== _id
      }
    }
    "strict JSON object" in {
      val s = StrictJSONObjectIdStrategy
      val jobj = JObject(JField("$oid", jval) :: Nil)
      "from ObjectId to JSON object" in {
        s.out(_id) must_== jobj
      }
      "from JSON object to ObjectId" in {
        s.in(jobj) must_== _id
      }
    }
  }

}
