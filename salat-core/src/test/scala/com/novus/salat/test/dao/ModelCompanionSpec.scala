package com.novus.salat.test.dao

import com.novus.salat.global._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import com.novus.salat.dao.ModelCompanion
import com.novus.salat.test._
import org.specs2.specification.Scope

class ModelCompanionSpec extends SalatSpec {
  // which most specs can execute concurrently, this particular spec needs to execute sequentially to avoid mutating shared state,
  // namely, the MongoDB collection referenced by the MyModel.dao
  override def is = args(sequential = true) ^ super.is

  "Model companion spec for case class MyModel" should {
    "allow inserting an instance of MyModel" in {
      val _id = new ObjectId
      val d = new DateTime
      val m = MyModel(id = _id, x = "Test", y = 99, z = 1d :: 2d :: 3d :: Nil, d = d)
      MyModel.insert(m) must beSome(_id)
      MyModel.findOneByID(_id) must beSome(m)
    }
  }

  //  trait myModelScope extends Scope {
  //    log.debug("before: dropping %s", MyModel.collection.getFullName())
  //    MyModel.collection.drop()
  //    MyModel.collection.count must_== 0L
  //
  //    val _id = new ObjectId
  //    val d = new DateTime
  //    val m = MyModel(id = _id, x = "Test", y = 99, z = 1d :: 2d :: 3d :: Nil, d = d)
  //
  //  }

}
