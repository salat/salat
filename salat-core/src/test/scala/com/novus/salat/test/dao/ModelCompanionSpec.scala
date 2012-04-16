package com.novus.salat.test.dao

import com.novus.salat.global._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import com.novus.salat.dao.ModelCompanion
import com.novus.salat.test._
import org.specs2.specification.Scope
import com.novus.salat.json.JSONConfig
import net.liftweb.json._
import scala.util.parsing.json.JSONArray

class ModelCompanionSpec extends SalatSpec {
  // which most specs can execute concurrently, this particular spec needs to execute sequentially to avoid mutating shared state,
  // namely, the MongoDB collection referenced by the MyModel.dao
  override def is = args(sequential = true) ^ super.is

  "Model companion spec for case class MyModel" should {

    "provide convenience methods delegating to Grater[MyModel]" in {

      "toDBObject" in new myModelScope {
        val dbo: MongoDBObject = MyModel.toDBObject(m)
        dbo must havePair("_id", _id)
        dbo must havePair("x", x)
        dbo must havePair("y", y)
        dbo must havePair("z", DBList(z: _*))
        dbo must havePair("d", d)
      }

      "toObject" in new myModelScope {
        val dbo = MyModel.toDBObject(m)
        val m_* = MyModel.toObject(dbo)
        m_* must_== m
      }

      "toPrettyJson" in new myModelScope {
        MyModel.toPrettyJson(m) must_== """{
  "_typeHint":"com.novus.salat.test.dao.MyModel",
  "_id":{
    "$oid":"%s"
  },
  "x":"Test",
  "y":99,
  "z":[1.0,2.0,3.0],
  "d":"%s"
}""".format(_id.toString, JSONConfig.DefaultDateTimeFormatter.print(d.millis))
      }

      "toCompactJson" in new myModelScope {
        val expected = "{\"_typeHint\":\"com.novus.salat.test.dao.MyModel\",\"_id\":{\"$oid\":\""+
          _id.toString+
          "\"},\"x\":\"Test\",\"y\":99,\"z\":[1.0,2.0,3.0],\"d\":\""+
          JSONConfig.DefaultDateTimeFormatter.print(d.millis)+
          "\"}"
        MyModel.toCompactJson(m) must_== expected
      }

      "toJson (JObject)" in new myModelScope {
        val j = MyModel.toJson(m)
        j \ "_id" must_== JObject(List(JField("$oid", JString(_id.toString))))
        j \ "x" must_== JString(x)
        j \ "y" must_== JInt(y)
        j \ "z" must_== JArray(z.map(JDouble(_)))
        j \ "d" must_== ctx.jsonConfig.dateStrategy.out(d)
      }
    }

    "provide simple static access to CRUD ops" in {
      "insert" in new myModelScope {
        MyModel.insert(m) must beSome(_id)
        MyModel.findOneByID(_id) must beSome(m)
      }

      "save" in new myModelScope {
        MyModel.insert(m) must beSome(_id)
        val z_* = 0d :: z
        MyModel.save(m.copy(z = z_*))
        val m_* = MyModel.findOneByID(_id)
        m_*.map(_.z) must beSome(z_*)
      }

      "update" in new myModelScope {
        MyModel.insert(m) must beSome(_id)
        val x_* = "cold stone lamping" // this spec is brought to you by the Jurassic 5
        val q = MongoDBObject("_id" -> _id)
        val o = MongoDBObject("$set" -> MongoDBObject("x" -> x_*))
        MyModel.update(q = q, o = o, upsert = false, multi = false, wc = MyModel.dao.collection.writeConcern)
        MyModel.findOneByID(_id) must beSome(m.copy(x = x_*))
      }

      "remove" in new myModelScope {
        MyModel.insert(m) must beSome(_id)
        MyModel.findOneByID(_id) must beSome(m)
        MyModel.remove(m)
        MyModel.findOneByID(_id) must beNone
      }
    }

  }

  trait myModelScope extends Scope {
    log.trace("before: dropping %s", MyModel.collection.getFullName())
    MyModel.collection.drop()
    MyModel.collection.count must_== 0L

    val _id = new ObjectId
    val d = new DateTime
    val x = "Test"
    val y = 99
    val z = 1d :: 2d :: 3d :: Nil
    val m = MyModel(id = _id, x = x, y = y, z = z, d = d)
  }

}
