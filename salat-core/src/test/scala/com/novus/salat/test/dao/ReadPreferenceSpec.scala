package com.novus.salat.test.dao

import org.specs2.specification.Scope
import com.novus.salat.test._
import com.novus.salat._
import com.novus.salat.test.global._
import com.mongodb.casbah.Imports._

class ReadPreferenceSpec extends SalatSpec {

  // whilst most specs can execute concurrently, this particular spec needs to execute sequentially to avoid mutating shared state,
  // namely, the MongoDB collection referenced by the AlphaDAO
  override def is = args(sequential = true) ^ super.is

  implicit val wc = AlphaDAO.defaultWriteConcern

  val alpha1 = Alpha(id = 1, beta = List[Beta](Gamma("gamma3"), Delta("delta3", "sampi3")))
  val alpha2 = Alpha(id = 2, beta = List[Beta](Gamma("gamma2"), Delta("delta2", "sampi2"), Delta("digamma2", "san2")))

  "A Salat DAO" should {

    "support a default ReadPreference" in new alphaContextWithData {
      AlphaDAO.defaultReadPreference.getName must be matching ReadPreference.Primary.getName
    }

    "support findOne with ReadPreference" in new alphaContextWithData {
      // Just a check to see if we are set...
      AlphaDAO.collection.count() must_== 2L
      // Since findOne does not return a cursor, we cannot really check
      // if the ReadPreference is set. All we can check is if the method
      // allows for a ReadPreference.
      AlphaDAO.findOne(grater[Alpha].asDBObject(alpha1), ReadPreference.Primary)
    }

    "support find with ReadPreference" in new alphaContextWithData {
      val cursor = AlphaDAO.find(MongoDBObject("_id" -> 1), MongoDBObject.empty, ReadPreference.SecondaryPreferred)
      cursor.underlying.getReadPreference.getName must be matching ReadPreference.SecondaryPreferred.getName
      cursor.toList must contain(alpha1)
    }

    "support setting of ReadPreference on cursor" in new alphaContextWithData {
      val cursor = AlphaDAO.find(MongoDBObject("_id" -> 1))
      cursor.underlying.getReadPreference.getName must be matching ReadPreference.Primary.getName
      cursor.limit(10) // just set an arbitrary option on the cursor for testing
      cursor.readPreference(ReadPreference.Nearest)
      cursor.underlying.getReadPreference.getName must be matching ReadPreference.Nearest.getName
      cursor.toList must contain(alpha1)
    }

    "support count with ReadPreference" in new alphaContextWithData {
      AlphaDAO.count(q = MongoDBObject("_id" -> 2), rp = ReadPreference.SecondaryPreferred) must beEqualTo(1L)
    }

  }

  trait alphaContextWithData extends Scope {
    log.debug("before: dropping %s", AlphaDAO.collection.getFullName())
    AlphaDAO.collection.drop()
    AlphaDAO.collection.setReadPreference(ReadPreference.Primary)
    AlphaDAO.collection.count() must_== 0L

    val _ids = AlphaDAO.insert(alpha1, alpha2)
    _ids must contain(Option(alpha1.id), Option(alpha2.id))
    AlphaDAO.collection.count() must_== 2L
  }

}
