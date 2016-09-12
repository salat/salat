/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         JsonDefaultValuesSpec.scala
 * Last modified: 2016-07-10 23:45:43 EDT
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

package salat.test.json

import salat._
import salat.json._
import salat.util._
import org.bson.types.ObjectId
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification

/**
 * @see https://github.com/novus/salat/issues/130
 */
class JsonDefaultValuesSpec extends Specification with Logging with JsonMatchers {
  private val SuppressDefaults = new Context {
    val name = "suppressDefault"
    override val suppressDefaultArgs = true
  }
  private val SuppressDefaultsStringsAsObjectIds = new Context {
    val name = "suppressDefaultNoTypeHints"
    override val suppressDefaultArgs = true
    override val typeHintStrategy = NeverTypeHint
    override val jsonConfig = JSONConfig(objectIdStrategy = StringObjectIdStrategy)
  }

  "JSON support with default arg suppression disabled (the default)" should {
    "evaluate default constructor argument when corresponding JSON property is missing" in {

      val deserializedObj1 = grater[Probe].fromJSON("{}")
      val deserializedObj2 = grater[Probe].fromJSON("{}")

      deserializedObj1.id should not be null
      deserializedObj1.id should not be deserializedObj2.id
    }
  }

  "JSON support with default arg suppression enabled" should {
    implicit val ctx = SuppressDefaults
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

    "initialize empty _id with default 'None' if type Option" in {
      grater[ProbeOptionalId].fromJSON("{}")._id should beNone
    }

    "initialize populated _id field if type Option" in {
      val id = new ObjectId
      grater[ProbeOptionalId].fromJSON(s"""{"_id": {"$$oid": "$id"}}""")._id should beSome(id)
    }

    "initialize populated _id field if type Option with type hints disabled" in {
      val id = new ObjectId
      implicit val ctx = SuppressDefaultsStringsAsObjectIds
      grater[ProbeNoDefaultObjectId].fromJSON(s"""{"_id": "$id"}""")._id shouldEqual id
    }

    "initialize populated ObjectId _id field" in {
      val id = new ObjectId
      grater[ProbeNoDefaultObjectId].fromJSON(s"""{"_id": {"$$oid": "$id"}}""")._id shouldEqual id
    }

    "initialize populated ObjectId _id field...with type hints disabled" in {
      val id = new ObjectId
      implicit val ctx = SuppressDefaultsStringsAsObjectIds
      grater[ProbeNoDefaultObjectId].fromJSON(s"""{"_id": "$id"}""")._id shouldEqual id
    }
    "throw when a required ObjectId _id field is not supplied" in {
      val id = new ObjectId
      grater[ProbeNoDefaultObjectId].fromJSON("{}")._id should throwA[RuntimeException]
    }
  }
}

case class Probe(id: ObjectId = new ObjectId())
case class ChildProbe(id: ObjectId, parent: ObjectId)
case class ChildProbeParentOptional(id: ObjectId, parent: Option[ObjectId])
case class ChildProbeManyParents(id: ObjectId, parents: List[ObjectId])

case class ProbeOptionalId(_id: Option[ObjectId] = None)
case class ProbeNoDefaultObjectId(_id: ObjectId)
