/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         PersistAnnotationSpec.scala
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

class PersistAnnotationSpec extends SalatSpec {

  "com.novus.salat.annotations.Persist" should {

    "allow a field inside a case class to be persisted" in {

      "a simple string val" in {
        val m = Maud(swept = "swept", out = "out")

        val dbo: MongoDBObject = grater[Maud].asDBObject(m)
        //      log.info(MapPrettyPrinter(dbo))
        dbo must havePair("swept" -> "swept")
        dbo must havePair("out" -> "out")
        dbo must havePair("toSea" -> "tuo tpews")

        val m_* = grater[Maud].asObject(dbo)
        m_* must_== m
        m_*.toSea must_== m.toSea
      }

      "a value that requires a transformer" in {
        val m = Maud2(swept = "swept", out = "out")
        val dbo: MongoDBObject = grater[Maud2].asDBObject(m)
        //        log.info(MapPrettyPrinter(dbo))
        dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Maud2")
        dbo must havePair("swept" -> "swept")
        dbo must havePair("out" -> "out")
        dbo must havePair("ida" -> {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.Ida"
          builder += "lake" -> 8.0
          builder.result
        })

        val m_* = grater[Maud2].asObject(dbo)
        m_* must_== m
        m_*.ida must_== m.ida
      }

      "a var" in {
        val m = Maud3(swept = "swept", out = "out")
        val dbo: MongoDBObject = grater[Maud3].asDBObject(m)
        //        log.info(MapPrettyPrinter(dbo))
        dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Maud3")
        dbo must havePair("swept" -> "swept")
        dbo must havePair("out" -> "out")
        // ida is a var but gets persisted anyway
        dbo must havePair("ida" -> {
          val builder = MongoDBObject.newBuilder
          builder += "_typeHint" -> "com.novus.salat.test.model.Ida"
          builder += "lake" -> 8.0
          builder.result
        })

        val m_* = grater[Maud3].asObject(dbo)
        m_* must_== m
        m_*.ida must_== m.ida
      }

    }

    "respect @Ignore" in {
      val m = Maud4(swept = "swept", out = "out")

      val dbo: MongoDBObject = grater[Maud4].asDBObject(m)
      //      log.info(MapPrettyPrinter(dbo))
      dbo must havePair("swept" -> "swept")
      dbo must havePair("out" -> "out")
      dbo must havePair("toSea" -> "tuo tpews")
      dbo must not have key("ida") // ida had both @Ignore and @Persist - @Ignore wins

      val m_* = grater[Maud4].asObject(dbo)
      m_* must_== m
      m_*.toSea must_== m.toSea
    }

    "respect @Persist declared in a trait" in {
      val m = Maud8(swept = "swept", out = "out")
      val dbo: MongoDBObject = grater[Maud8].asDBObject(m)
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Maud8")
      dbo must havePair("swept" -> "swept")
      dbo must havePair("out" -> "out")
      dbo must havePair("toSea" -> "tuo tpews") // persisted from Maud8 itself
      dbo must havePair("howFar" -> 8) // persisted from EvenMoreMaudLike trait
      val m_* = grater[Maud8].asObject(dbo)
      m_* must_== m
    }

    "respect @Persist declared in immediate superclass" in {
      val m = Maud11(swept = "swept", out = "out")
      val dbo: MongoDBObject = grater[Maud11].asDBObject(m)
      dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Maud11")
      dbo must havePair("swept" -> "swept")
      dbo must havePair("out" -> "out")
      dbo must havePair("toSea" -> "tuo tpews") // persisted from Maud11 itself
      dbo must havePair("howFar" -> 8) // persisted from abstract superclass MaudAgain
      val m_* = grater[Maud11].asObject(dbo)
      m_* must_== m
    }

    "work with @Salat on a trait" in {

      "where @Persist is declared in the subclasses implementing the trait" in {
        val m = ManyMauds(mauds = List[MaudLike](
          Maud5(swept = "swept", out = "out"),
          Maud6(swept = "swept", out = "out"),
          Maud7(swept = "swept", out = "out")))
        val dbo: MongoDBObject = grater[ManyMauds].asDBObject(m)
        //      log.info(MapPrettyPrinter(dbo))
        dbo must havePair("_typeHint" -> "com.novus.salat.test.model.ManyMauds")
        dbo must havePair("mauds" -> {
          val listBuilder = MongoDBList.newBuilder
          listBuilder += {
            val builder = MongoDBObject.newBuilder
            builder += "_typeHint" -> "com.novus.salat.test.model.Maud5"
            builder += "swept" -> "swept"
            builder += "out" -> "out"
            builder += "toSea" -> "tuo tpews"
            builder.result
          }
          listBuilder += {
            val builder = MongoDBObject.newBuilder
            builder += "_typeHint" -> "com.novus.salat.test.model.Maud6"
            builder += "swept" -> "swept"
            builder += "out" -> "out"
            builder += "ida" -> {
              val builder = MongoDBObject.newBuilder
              builder += "_typeHint" -> "com.novus.salat.test.model.Ida"
              builder += "lake" -> 8.0
              builder.result
            }
            builder.result
          }
          listBuilder += {
            val builder = MongoDBObject.newBuilder
            builder += "_typeHint" -> "com.novus.salat.test.model.Maud7"
            builder += "swept" -> "swept"
            builder += "out" -> "out"
            // notWaving does not have @Persist so does not appear here
            builder += "butDrowning" -> true
            builder.result
          }
          listBuilder.result
        })

        val m_* = grater[ManyMauds].asObject(dbo)
        m_* must_== m
      }

      "where a collection is typed to a trait declaring @Persist" in {
        val m = Maudelic(mauds = List[EvenMoreMaudLike](
          Maud8(swept = "swept", out = "out"),
          Maud9(swept = "swept", out = "out"),
          Maud10(swept = "swept", out = "out")))
        val dbo: MongoDBObject = grater[Maudelic].asDBObject(m)
        //        log.info(MapPrettyPrinter(dbo))
        dbo must havePair("_typeHint" -> "com.novus.salat.test.model.Maudelic")
        dbo must havePair("mauds" -> {
          val listBuilder = MongoDBList.newBuilder
          listBuilder += {
            val builder = MongoDBObject.newBuilder
            builder += "_typeHint" -> "com.novus.salat.test.model.Maud8"
            builder += "swept" -> "swept"
            builder += "out" -> "out"
            builder += "toSea" -> "tuo tpews"
            builder += "howFar" -> 8 // persisted from EvenMoreMaudLike trait
            builder.result
          }
          listBuilder += {
            val builder = MongoDBObject.newBuilder
            builder += "_typeHint" -> "com.novus.salat.test.model.Maud9"
            builder += "swept" -> "swept"
            builder += "out" -> "out"
            builder += "ida" -> {
              val builder = MongoDBObject.newBuilder
              builder += "_typeHint" -> "com.novus.salat.test.model.Ida"
              builder += "lake" -> 8.0
              builder.result
            }
            builder += "howFar" -> 8 // persisted from EvenMoreMaudLike trait
            builder.result
          }
          listBuilder += {
            val builder = MongoDBObject.newBuilder
            builder += "_typeHint" -> "com.novus.salat.test.model.Maud10"
            builder += "swept" -> "swept"
            builder += "out" -> "out"
            // notWaving does not have @Persist so does not appear here
            builder += "butDrowning" -> true
            builder += "howFar" -> 8 // persisted from EvenMoreMaudLike trait
            builder.result
          }
          listBuilder.result
        })

        val m_* = grater[Maudelic].asObject(dbo)
        m_* must_== m
      }
    }
  }

}