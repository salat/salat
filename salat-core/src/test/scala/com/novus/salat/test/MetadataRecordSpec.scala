/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         MetadataRecordSpec.scala
 * Last modified: 2012-10-15 20:40:58 EDT
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

package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.test.global._
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