package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.json._
import com.novus.salat.util._
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
