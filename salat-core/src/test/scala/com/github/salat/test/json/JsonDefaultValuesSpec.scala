/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         JsonDefaultValuesSpec.scala
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

import com.github.salat._
import com.github.salat.util._
import org.bson.types.ObjectId
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification

/**
 * @see https://github.com/novus/salat/issues/130
 */
class JsonDefaultValuesSpec extends Specification with Logging with JsonMatchers {

  "JSON support with default arg suppression disabled (the default)" should {
    "evaluate default constructor argument when corresponding JSON property is missing" in {

      val deserializedObj1 = grater[Probe].fromJSON("{}")
      val deserializedObj2 = grater[Probe].fromJSON("{}")

      deserializedObj1.id should not be null
      deserializedObj1.id should not be deserializedObj2.id
    }
  }

  "JSON support with default arg suppression enabled" should {
    implicit val ctx = new Context {
      val name = "suppressDefault"
      override val suppressDefaultArgs = true
    }
    "serialize instances with multiple ObjectId fields that do not have default values" in {
      val father = Probe()
      val child = ChildProbe(id = new ObjectId, parent = father.id)
      val result = grater[ChildProbe].toCompactJSON(child)
      result must not(throwA[NoSuchMethodException]) and beAnInstanceOf[String]
    }
    "serialize instances with Optional ObjectId fields that do not have default values" in {
      val child = ChildProbeParentOptional(id = new ObjectId, parent = None)
      val result = grater[ChildProbeParentOptional].toCompactJSON(child)
      result must not(throwA[NoSuchMethodException]) and beAnInstanceOf[String]
    }
    "serialize instances with lists of ObjectIds that do not have default values" in {
      val child = ChildProbeManyParents(id = new ObjectId, parents = Nil)
      val result = grater[ChildProbeManyParents].toCompactJSON(child)
      result must not(throwA[NoSuchMethodException]) and beAnInstanceOf[String]
    }
  }
}

case class Probe(id: ObjectId = new ObjectId())
case class ChildProbe(id: ObjectId, parent: ObjectId)
case class ChildProbeParentOptional(id: ObjectId, parent: Option[ObjectId])
case class ChildProbeManyParents(id: ObjectId, parents: List[ObjectId])
