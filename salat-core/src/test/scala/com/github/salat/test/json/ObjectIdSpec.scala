/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         ObjectIdSpec.scala
 * Last modified: 2015-06-23 20:48:17 EDT
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

package com.github.salat.test.json

import com.github.salat.json.{StrictJSONObjectIdStrategy, StringObjectIdStrategy}
import com.github.salat.util.Logging
import org.bson.types.ObjectId
import org.json4s.JsonAST._
import org.specs2.mutable.Specification

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
