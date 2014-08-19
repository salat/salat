/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         JsonSpec.scala
 * Last modified: 2013-02-25 21:07:26 EST
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */
package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.util._
import org.bson.types.ObjectId
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification

class JsonDefaultValuesSpec extends Specification with Logging with JsonMatchers {

  "JSON support" should {
    "evaluate default constructor argument when corresponding JSON property is missing" in {

      val deserializedObj1 = grater[Probe].fromJSON("{}")
      val deserializedObj2 = grater[Probe].fromJSON("{}")

      deserializedObj1.id should not be null
      deserializedObj1.id should not be deserializedObj2.id
    }
  }
}

case class Probe(id: ObjectId = new ObjectId())
