package com.novus.salat.bson

import com.mongodb.casbah.Imports._
import org.bson._
import org.bson.types.BasicBSONList

object BSONObjectToMongoDbObject {

  def apply(bo: BSONObject): MongoDBObject = {
    val map = scala.collection.JavaConversions.mapAsScalaMap(bo.asInstanceOf[BasicBSONObject])
    val builder = MongoDBObject.newBuilder
    map.foreach {
      case (k, v) =>

        builder += k -> transform(v)
    }
    builder.result()
  }

  def transform(v: Any): AnyRef = v match {
    case bl: BasicBSONList => {
      val builder = MongoDBList.newBuilder
      val iter = bl.iterator()
      while (iter.hasNext) {
        builder += transform(iter.next())
      }
      builder.result()
    }
    case bo: BasicBSONObject => {
      val map = scala.collection.JavaConversions.mapAsScalaMap(bo)
      val builder = MongoDBObject.newBuilder
      map.foreach {
        case (k, v) =>
          builder += k -> transform(v)
      }
      builder.result()
    }
    case x => x.asInstanceOf[AnyRef]
  }

}
