/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         CollectionSupportSpec.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.util.MapPrettyPrinter
import com.novus.salat.dao.SalatDAO
import com.novus.salat.test.model._

class CollectionSupportSpec extends SalatSpec {

  "Salat extractors and injectors" should {

    "supports Maps whose key is a String" in {

      "scala.collection.Map[String, _]" in {
        val coll = scala.collection.Map("a" -> Thingy("A"), "b" -> Thingy("B"))
        val omphalos = Omphalos(coll = coll)
        val dbo: MongoDBObject = grater[Omphalos].asDBObject(omphalos)
        dbo must havePair("coll" -> {
          val builder = MongoDBObject.newBuilder
          builder += "a" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += "b" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Omphalos, ObjectId](collection = MongoConnection()(SalatSpecDb)("able_coll_spec")) {}
        val _id = dao.insert(omphalos)
        _id must beSome(omphalos.id)
        dao.findOneByID(omphalos.id) must beSome(omphalos)
      }

      "scala.collection.immutable.Map[String, _]" in {
        val coll = scala.collection.immutable.Map("a" -> Thingy("A"), "b" -> Thingy("B"))
        val able = Able(coll = coll)
        val dbo: MongoDBObject = grater[Able].asDBObject(able)
        dbo must havePair("coll" -> {
          val builder = MongoDBObject.newBuilder
          builder += "a" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += "b" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Able, ObjectId](collection = MongoConnection()(SalatSpecDb)("able_coll_spec")) {}
        val _id = dao.insert(able)
        _id must beSome(able.id)
        dao.findOneByID(able.id) must beSome(able)
      }

      "scala.collection.mutable.Map[String, _]" in {
        val coll = scala.collection.mutable.Map("a" -> Thingy("A"), "b" -> Thingy("B"))
        val baker = Baker(coll = coll)
        val dbo: MongoDBObject = grater[Baker].asDBObject(baker)
        dbo must havePair("coll" -> {
          val builder = MongoDBObject.newBuilder
          builder += "a" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += "b" -> MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Baker, ObjectId](collection = MongoConnection()(SalatSpecDb)("baker_coll_spec")) {}
        val _id = dao.insert(baker)
        _id must beSome(baker.id)
        dao.findOneByID(baker.id) must beSome(baker)
      }
    }

    "support Set[_]" in {

      "support scala.collection.Set[_]" in {
        val coll = scala.collection.Set(Thingy("A"), Thingy("B"))
        val charlie = Charlie(coll = coll)
        val dbo: MongoDBObject = grater[Charlie].asDBObject(charlie)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Charlie, ObjectId](collection = MongoConnection()(SalatSpecDb)("charlie_coll_spec")) {}
        val _id = dao.insert(charlie)
        _id must beSome(charlie.id)
        dao.findOneByID(charlie.id) must beSome(charlie)
      }

      "support scala.collection.immutable.Set[_]" in {
        val coll = scala.collection.immutable.Set(Thingy("A"), Thingy("B"))
        val dog = Dog(coll = coll)
        val dbo: MongoDBObject = grater[Dog].asDBObject(dog)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Dog, ObjectId](collection = MongoConnection()(SalatSpecDb)("dog_coll_spec")) {}
        val _id = dao.insert(dog)
        _id must beSome(dog.id)
        dao.findOneByID(dog.id) must beSome(dog)
      }

      "support scala.collection.mutable.Set[_]" in {
        val coll = scala.collection.mutable.Set(Thingy("A"), Thingy("B"))
        val easy = Easy(coll = coll)
        val dbo: MongoDBObject = grater[Easy].asDBObject(easy)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder.result()
        })

        val dao = new SalatDAO[Easy, ObjectId](collection = MongoConnection()(SalatSpecDb)("easy_coll_spec")) {}
        val _id = dao.insert(easy)
        _id must beSome(easy.id)
        dao.findOneByID(easy.id) must beSome(easy)
      }
    }

    "support List[_]" in {
      val coll = List(Thingy("A"), Thingy("B"))
      val fox = Fox(coll = coll)
      val dbo: MongoDBObject = grater[Fox].asDBObject(fox)
      dbo must havePair("coll" -> {
        val builder = MongoDBList.newBuilder
        builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
        builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
        builder.result()
      })

      val dao = new SalatDAO[Fox, ObjectId](collection = MongoConnection()(SalatSpecDb)("fox_coll_spec")) {}
      val _id = dao.insert(fox)
      _id must beSome(fox.id)
      dao.findOneByID(fox.id) must beSome(fox)
    }

    "support Seq[_]" in {

      "support scala.collection.Seq[_]" in {
        val coll = scala.collection.Seq(Thingy("A"), Thingy("B"))
        val gee = Gee(coll = coll)
        val dbo: MongoDBObject = grater[Gee].asDBObject(gee)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Gee, ObjectId](collection = MongoConnection()(SalatSpecDb)("gee_coll_spec")) {}
        val _id = dao.insert(gee)
        _id must beSome(gee.id)
        dao.findOneByID(gee.id) must beSome(gee)
      }

      "support scala.collection.immutable.Seq[_]" in {
        val coll = scala.collection.immutable.Seq(Thingy("A"), Thingy("B"))
        val how = How(coll = coll)
        val dbo: MongoDBObject = grater[How].asDBObject(how)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[How, ObjectId](collection = MongoConnection()(SalatSpecDb)("how_coll_spec")) {}
        val _id = dao.insert(how)
        _id must beSome(how.id)
        dao.findOneByID(how.id) must beSome(how)
      }

      "support scala.collection.mutable.Seq[_]" in {
        val coll = scala.collection.mutable.Seq(Thingy("A"), Thingy("B"))
        val item = Item(coll = coll)
        val dbo: MongoDBObject = grater[Item].asDBObject(item)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Item, ObjectId](collection = MongoConnection()(SalatSpecDb)("item_coll_spec")) {}
        val _id = dao.insert(item)
        _id must beSome(item.id)
        dao.findOneByID(item.id) must beSome(item)
      }
    }

    "support Buffer[_]" in {

      "support scala.collection.mutable.Buffer[_]" in {
        val coll = scala.collection.mutable.Buffer(Thingy("A"), Thingy("B"))
        val jig = Jig(coll = coll)
        val dbo: MongoDBObject = grater[Jig].asDBObject(jig)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Jig, ObjectId](collection = MongoConnection()(SalatSpecDb)("jig_coll_spec")) {}
        val _id = dao.insert(jig)
        _id must beSome(jig.id)
        dao.findOneByID(jig.id) must beSome(jig)
      }

      "support scala.collection.mutable.ArrayBuffer[_]" in {
        val coll = scala.collection.mutable.ArrayBuffer(Thingy("A"), Thingy("B"))
        val king = King(coll = coll)
        val dbo: MongoDBObject = grater[King].asDBObject(king)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[King, ObjectId](collection = MongoConnection()(SalatSpecDb)("king_coll_spec")) {}
        val _id = dao.insert(king)
        _id must beSome(king.id)
        dao.findOneByID(king.id) must beSome(king)
      }
    }

    "support Vector[_]" in {
      val coll = scala.collection.immutable.Vector(Thingy("A"), Thingy("B"))
      val mike = Mike(coll = coll)
      val dbo: MongoDBObject = grater[Mike].asDBObject(mike)
      dbo must havePair("coll" -> {
        val builder = MongoDBList.newBuilder
        builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
        builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
        builder.result()
      })

      val dao = new SalatDAO[Mike, ObjectId](collection = MongoConnection()(SalatSpecDb)("mike_coll_spec")) {}
      val _id = dao.insert(mike)
      _id must beSome(mike.id)
      dao.findOneByID(mike.id) must beSome(mike)
    }

    "support IndexedSeq[_]" in {

      "support scala.collection.IndexedSeq[_]" in {
        val coll = scala.collection.IndexedSeq(Thingy("A"), Thingy("B"))
        val nab = Nab(coll = coll)
        val dbo: MongoDBObject = grater[Nab].asDBObject(nab)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Nab, ObjectId](collection = MongoConnection()(SalatSpecDb)("nab_coll_spec")) {}
        val _id = dao.insert(nab)
        _id must beSome(nab.id)
        dao.findOneByID(nab.id) must beSome(nab)
      }

      "support scala.collection.immutable.IndexedSeq[_]" in {
        val coll = scala.collection.immutable.IndexedSeq(Thingy("A"), Thingy("B"))
        val oboe = Oboe(coll = coll)
        val dbo: MongoDBObject = grater[Oboe].asDBObject(oboe)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Oboe, ObjectId](collection = MongoConnection()(SalatSpecDb)("oboe_coll_spec")) {}
        val _id = dao.insert(oboe)
        _id must beSome(oboe.id)
        dao.findOneByID(oboe.id) must beSome(oboe)
      }

      "support scala.collection.mutable.IndexedSeq[_]" in {
        val coll = scala.collection.mutable.IndexedSeq(Thingy("A"), Thingy("B"))
        val prep = Prep(coll = coll)
        val dbo: MongoDBObject = grater[Prep].asDBObject(prep)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Prep, ObjectId](collection = MongoConnection()(SalatSpecDb)("prep_coll_spec")) {}
        val _id = dao.insert(prep)
        _id must beSome(prep.id)
        dao.findOneByID(prep.id) must beSome(prep)
      }
    }

    "support linked lists" in {
      "LinkedList[_]" in {
        val coll = scala.collection.mutable.LinkedList(Thingy("A"), Thingy("B"))
        val queen = Queen(coll = coll)
        val dbo: MongoDBObject = grater[Queen].asDBObject(queen)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Queen, ObjectId](collection = MongoConnection()(SalatSpecDb)("queen_coll_spec")) {}
        val _id = dao.insert(queen)
        _id must beSome(queen.id)
        dao.findOneByID(queen.id) must beSome(queen)
      }
      "DoubleLinkedList[_]" in {
        val coll = scala.collection.mutable.DoubleLinkedList(Thingy("A"), Thingy("B"))
        val roger = Roger(coll = coll)
        val dbo: MongoDBObject = grater[Roger].asDBObject(roger)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.novus.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Roger, ObjectId](collection = MongoConnection()(SalatSpecDb)("roger_coll_spec")) {}
        val _id = dao.insert(roger)
        _id must beSome(roger.id)
        dao.findOneByID(roger.id) must beSome(roger)
      }
    }

    // TODO: moar collection types
  }
}