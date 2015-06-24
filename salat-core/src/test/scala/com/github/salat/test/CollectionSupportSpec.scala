/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2015 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         CollectionSupportSpec.scala
 * Last modified: 2015-06-23 20:52:14 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package com.github.salat.test

import com.github.salat._
import com.github.salat.dao.SalatDAO
import com.github.salat.test.global._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

class CollectionSupportSpec extends SalatSpec {

  "Salat extractors and injectors" should {

    "supports Maps whose key is a String" in {

      "scala.collection.Map[String, _]" in {
        val coll = scala.collection.Map("a" -> Thingy("A"), "b" -> Thingy("B"))
        val omphalos = Omphalos(coll = coll)
        val dbo: MongoDBObject = grater[Omphalos].asDBObject(omphalos)
        dbo must havePair("coll" -> {
          val builder = MongoDBObject.newBuilder
          builder += "a" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += "b" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Omphalos, ObjectId](collection = MongoConnection()(SalatSpecDb)("able_coll_spec")) {}
        val _id = dao.insert(omphalos)
        _id must beSome(omphalos.id)
        dao.findOneById(omphalos.id) must beSome(omphalos)
      }

      "scala.collection.immutable.Map[String, _]" in {
        val coll = scala.collection.immutable.Map("a" -> Thingy("A"), "b" -> Thingy("B"))
        val able = Able(coll = coll)
        val dbo: MongoDBObject = grater[Able].asDBObject(able)
        dbo must havePair("coll" -> {
          val builder = MongoDBObject.newBuilder
          builder += "a" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += "b" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Able, ObjectId](collection = MongoConnection()(SalatSpecDb)("able_coll_spec")) {}
        val _id = dao.insert(able)
        _id must beSome(able.id)
        dao.findOneById(able.id) must beSome(able)
      }

      "scala.collection.mutable.Map[String, _]" in {
        val coll = scala.collection.mutable.Map("a" -> Thingy("A"), "b" -> Thingy("B"))
        val baker = Baker(coll = coll)
        val dbo: MongoDBObject = grater[Baker].asDBObject(baker)
        dbo must havePair("coll" -> {
          val builder = MongoDBObject.newBuilder
          builder += "a" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += "b" -> MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Baker, ObjectId](collection = MongoConnection()(SalatSpecDb)("baker_coll_spec")) {}
        val _id = dao.insert(baker)
        _id must beSome(baker.id)
        dao.findOneById(baker.id) must beSome(baker)
      }
    }

    "support Set[_]" in {

      "support scala.collection.Set[_]" in {
        val coll = scala.collection.Set(Thingy("A"), Thingy("B"))
        val charlie = Charlie(coll = coll)
        val dbo: MongoDBObject = grater[Charlie].asDBObject(charlie)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Charlie, ObjectId](collection = MongoConnection()(SalatSpecDb)("charlie_coll_spec")) {}
        val _id = dao.insert(charlie)
        _id must beSome(charlie.id)
        dao.findOneById(charlie.id) must beSome(charlie)
      }

      "support scala.collection.immutable.Set[_]" in {
        val coll = scala.collection.immutable.Set(Thingy("A"), Thingy("B"))
        val dog = Dog(coll = coll)
        val dbo: MongoDBObject = grater[Dog].asDBObject(dog)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Dog, ObjectId](collection = MongoConnection()(SalatSpecDb)("dog_coll_spec")) {}
        val _id = dao.insert(dog)
        _id must beSome(dog.id)
        dao.findOneById(dog.id) must beSome(dog)
      }

      "support scala.collection.mutable.Set[_]" in {
        val coll = scala.collection.mutable.Set(Thingy("A"), Thingy("B"))
        val easy = Easy(coll = coll)
        val dbo: MongoDBObject = grater[Easy].asDBObject(easy)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Easy, ObjectId](collection = MongoConnection()(SalatSpecDb)("easy_coll_spec")) {}
        val _id = dao.insert(easy)
        _id must beSome(easy.id)
        dao.findOneById(easy.id) must beSome(easy)
      }
    }

    "support List[_]" in {
      val coll = List(Thingy("A"), Thingy("B"))
      val fox = Fox(coll = coll)
      val dbo: MongoDBObject = grater[Fox].asDBObject(fox)
      dbo must havePair("coll" -> {
        val builder = MongoDBList.newBuilder
        builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
        builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
        builder.result()
      })

      val dao = new SalatDAO[Fox, ObjectId](collection = MongoConnection()(SalatSpecDb)("fox_coll_spec")) {}
      val _id = dao.insert(fox)
      _id must beSome(fox.id)
      dao.findOneById(fox.id) must beSome(fox)
    }

    "support Seq[_]" in {

      "support scala.collection.Seq[_]" in {
        val coll = scala.collection.Seq(Thingy("A"), Thingy("B"))
        val gee = Gee(coll = coll)
        val dbo: MongoDBObject = grater[Gee].asDBObject(gee)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Gee, ObjectId](collection = MongoConnection()(SalatSpecDb)("gee_coll_spec")) {}
        val _id = dao.insert(gee)
        _id must beSome(gee.id)
        dao.findOneById(gee.id) must beSome(gee)
      }

      "support scala.collection.immutable.Seq[_]" in {
        val coll = scala.collection.immutable.Seq(Thingy("A"), Thingy("B"))
        val how = How(coll = coll)
        val dbo: MongoDBObject = grater[How].asDBObject(how)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[How, ObjectId](collection = MongoConnection()(SalatSpecDb)("how_coll_spec")) {}
        val _id = dao.insert(how)
        _id must beSome(how.id)
        dao.findOneById(how.id) must beSome(how)
      }

      "support scala.collection.mutable.Seq[_]" in {
        val coll = scala.collection.mutable.Seq(Thingy("A"), Thingy("B"))
        val item = Item(coll = coll)
        val dbo: MongoDBObject = grater[Item].asDBObject(item)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Item, ObjectId](collection = MongoConnection()(SalatSpecDb)("item_coll_spec")) {}
        val _id = dao.insert(item)
        _id must beSome(item.id)
        dao.findOneById(item.id) must beSome(item)
      }
    }

    "support Buffer[_]" in {

      "support scala.collection.mutable.Buffer[_]" in {
        val coll = scala.collection.mutable.Buffer(Thingy("A"), Thingy("B"))
        val jig = Jig(coll = coll)
        val dbo: MongoDBObject = grater[Jig].asDBObject(jig)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Jig, ObjectId](collection = MongoConnection()(SalatSpecDb)("jig_coll_spec")) {}
        val _id = dao.insert(jig)
        _id must beSome(jig.id)
        dao.findOneById(jig.id) must beSome(jig)
      }

      "support scala.collection.mutable.ArrayBuffer[_]" in {
        val coll = scala.collection.mutable.ArrayBuffer(Thingy("A"), Thingy("B"))
        val king = King(coll = coll)
        val dbo: MongoDBObject = grater[King].asDBObject(king)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[King, ObjectId](collection = MongoConnection()(SalatSpecDb)("king_coll_spec")) {}
        val _id = dao.insert(king)
        _id must beSome(king.id)
        dao.findOneById(king.id) must beSome(king)
      }
    }

    "support Vector[_]" in {
      val coll = scala.collection.immutable.Vector(Thingy("A"), Thingy("B"))
      val mike = Mike(coll = coll)
      val dbo: MongoDBObject = grater[Mike].asDBObject(mike)
      dbo must havePair("coll" -> {
        val builder = MongoDBList.newBuilder
        builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
        builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
        builder.result()
      })

      val dao = new SalatDAO[Mike, ObjectId](collection = MongoConnection()(SalatSpecDb)("mike_coll_spec")) {}
      val _id = dao.insert(mike)
      _id must beSome(mike.id)
      dao.findOneById(mike.id) must beSome(mike)
    }

    "support IndexedSeq[_]" in {

      "support scala.collection.IndexedSeq[_]" in {
        val coll = scala.collection.IndexedSeq(Thingy("A"), Thingy("B"))
        val nab = Nab(coll = coll)
        val dbo: MongoDBObject = grater[Nab].asDBObject(nab)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Nab, ObjectId](collection = MongoConnection()(SalatSpecDb)("nab_coll_spec")) {}
        val _id = dao.insert(nab)
        _id must beSome(nab.id)
        dao.findOneById(nab.id) must beSome(nab)
      }

      "support scala.collection.immutable.IndexedSeq[_]" in {
        val coll = scala.collection.immutable.IndexedSeq(Thingy("A"), Thingy("B"))
        val oboe = Oboe(coll = coll)
        val dbo: MongoDBObject = grater[Oboe].asDBObject(oboe)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Oboe, ObjectId](collection = MongoConnection()(SalatSpecDb)("oboe_coll_spec")) {}
        val _id = dao.insert(oboe)
        _id must beSome(oboe.id)
        dao.findOneById(oboe.id) must beSome(oboe)
      }

      "support scala.collection.mutable.IndexedSeq[_]" in {
        val coll = scala.collection.mutable.IndexedSeq(Thingy("A"), Thingy("B"))
        val prep = Prep(coll = coll)
        val dbo: MongoDBObject = grater[Prep].asDBObject(prep)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Prep, ObjectId](collection = MongoConnection()(SalatSpecDb)("prep_coll_spec")) {}
        val _id = dao.insert(prep)
        _id must beSome(prep.id)
        dao.findOneById(prep.id) must beSome(prep)
      }
    }

    "support linked lists" in {
      "LinkedList[_]" in {
        val coll = scala.collection.mutable.LinkedList(Thingy("A"), Thingy("B"))
        val queen = Queen(coll = coll)
        val dbo: MongoDBObject = grater[Queen].asDBObject(queen)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Queen, ObjectId](collection = MongoConnection()(SalatSpecDb)("queen_coll_spec")) {}
        val _id = dao.insert(queen)
        _id must beSome(queen.id)
        dao.findOneById(queen.id) must beSome(queen)
      }
      "DoubleLinkedList[_]" in {
        val coll = scala.collection.mutable.DoubleLinkedList(Thingy("A"), Thingy("B"))
        val roger = Roger(coll = coll)
        val dbo: MongoDBObject = grater[Roger].asDBObject(roger)
        dbo must havePair("coll" -> {
          val builder = MongoDBList.newBuilder
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "A")
          builder += MongoDBObject("_typeHint" -> "com.github.salat.test.model.Thingy", "t" -> "B")
          builder.result()
        })

        val dao = new SalatDAO[Roger, ObjectId](collection = MongoConnection()(SalatSpecDb)("roger_coll_spec")) {}
        val _id = dao.insert(roger)
        _id must beSome(roger.id)
        dao.findOneById(roger.id) must beSome(roger)
      }
    }

    // TODO: moar collection types

    "support BitSet" in {
      "scala.collection.BitSet" in {
        val coll = scala.collection.BitSet(0, 5, 10, 15)
        val x = XRay(coll = coll)
        grater[XRay].asObject(grater[XRay].asDBObject(x)) must_== x
      }
      "scala.collection.immutable.BitSet" in {
        val coll = scala.collection.immutable.BitSet(0, 5, 10, 15)
        val y = Yoke(coll = coll)
        grater[Yoke].asObject(grater[Yoke].asDBObject(y)) must_== y
      }
      "scala.collection.mutable.BitSet" in {
        val coll = scala.collection.mutable.BitSet(0, 5, 10, 15)
        val y = Zebra(coll = coll)
        grater[Zebra].asObject(grater[Zebra].asDBObject(y)) must_== y
      }
    }
  }
}
