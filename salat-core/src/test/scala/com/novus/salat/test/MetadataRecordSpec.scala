package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.MapPrettyPrinter

class MetadataRecordSpec extends SalatSpec {
  "Metadata record" should {
    "serialize with only provided default params" in {
      val m = MetadataRecord()
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
    "serialize with only the first param defined" in {
      val m = MetadataRecord(validOutputFormats = List("a", "b", "c"))
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
    "serialize with only the second param defined" in {
      val m = MetadataRecord(transferIdx = Option(99))
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
    "serialize with only the third param defined" in {
      val m = MetadataRecord(deleted = true)
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
    "serialize with only the first and second param defined" in {
      val m = MetadataRecord(validOutputFormats = List("a", "b", "c"), transferIdx = Option(99))
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
    "serialize with only the first and third param defined" in {
      val m = MetadataRecord(validOutputFormats = List("a", "b", "c"), deleted = true)
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
    "serialize with only the second and third param defined" in {
      val m = MetadataRecord(transferIdx = Option(99), deleted = true)
      val dbo: MongoDBObject = grater[MetadataRecord].asDBObject(m)
      val m_* = grater[MetadataRecord].asObject(dbo)
      m_* must_== m
    }
  }
}