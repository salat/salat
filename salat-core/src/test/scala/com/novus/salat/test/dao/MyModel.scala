package com.novus.salat.test.dao

import com.novus.salat.global._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import com.novus.salat.test._
import com.novus.salat.dao.{ SalatDAO, ModelCompanion }

object MyModel extends ModelCompanion[MyModel, ObjectId] {
  val collection = MongoConnection()(SalatSpecDb)(MyModelColl)
  val dao = new SalatDAO[MyModel, ObjectId](collection = collection) {}
}

case class MyModel(@Key("_id") id: ObjectId,
                   x: String,
                   y: Int,
                   z: List[Double],
                   d: DateTime)