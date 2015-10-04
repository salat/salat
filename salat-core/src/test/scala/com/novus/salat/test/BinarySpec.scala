/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         CollectionSupportSpec.scala
 * Last modified: 2012-10-15 20:40:59 EDT
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

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.test.global._
import com.novus.salat.test.model._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Binary extends SalatSpec {

  "Salat extractors and injectors" should {
    "support Array[Byte]" in {
      val coll: Array[Byte] = "Hello World".getBytes
      val x = GloratArrayByte(coll = coll)
      grater[GloratArrayByte].asObject(grater[GloratArrayByte].asDBObject(x)) must_== x
    }
    "support Array[Byte] via DAO" in {
      val str = "Hello World"
      val coll: Array[Byte] = str.getBytes
      val x = GloratArrayByte(coll = coll)

      val dao = new SalatDAO[GloratArrayByte, ObjectId](collection = MongoConnection()(SalatSpecDb)("glorat_array_byte_coll_spec")) {}
      val _id = dao.insert(x)
      _id must beSome(x.id)
      // Can't do this obvious line because we'll get a new (non-ref-equal Array back)
      // dao.findOneById(x.id) must beSome(x)
      val ret = dao.findOneById(x.id).get
      new String(ret.coll) must_== str
    }
    "support Seq[Byte]" in {
      val coll: Seq[Byte] = "Hello World".getBytes
      val x = GloratSeqByte(coll = coll)
      grater[GloratSeqByte].asObject(grater[GloratSeqByte].asDBObject(x)) must_== x
    }
    "support Seq[Byte] via DAO" in {
      val str = "Hello World"
      val coll: Seq[Byte] = str.getBytes
      val x = GloratSeqByte(coll = coll)
      val dao = new SalatDAO[GloratSeqByte, ObjectId](collection = MongoConnection()(SalatSpecDb)("glorat_seq_byte_coll_spec")) {}
      val _id = dao.insert(x)
      _id must beSome(x.id)
      val db = dao.findOneById(x.id)
      db.get must_== x
      db.get.coll must_== x.coll
      // This line is the key test!
      // The buggy impl had the Mongo driver returning Int instead of Byte
      // and due to type erasure, everything is fine until you examine the elements
      db.get.coll.head must_== x.coll.head
      // And finally...
      new String(db.get.coll.toArray) must_== str
    }
  }
}