/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         CaseObjectSupport.scala
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
package com.github.salat.test

import com.github.salat._
import com.github.salat.test.always._
import com.github.salat.test.model._
import com.mongodb.casbah.Imports._

class CaseObjectSupport extends SalatSpec {
  "a grater" should {

    "support case objects" in {
      "be able to serialize case objects" in {
        val mine = Wardrobe(suits = List(Zoot))
        log.info("mine: %s", mine)
        val dbo: MongoDBObject = grater[Wardrobe].asDBObject(mine)
        log.info("dbo : %s", dbo)
        val suits = dbo.expand[MongoDBList]("suits")
        suits must beSome[MongoDBList]
        suits.getOrElse(throw new Exception("argh, someone stole my wardrobe")) // TODO: where did which go?
        suits match {
          case Some(suits) => {
            val suit: MongoDBObject = suits(0).asInstanceOf[DBObject]
            val th = suit.expand[String]("_typeHint")
            th must beSome(Zoot.getClass.getName)
          }
          case None => throw new Exception("where are my suits?") // TODO: where did fail go?
        }
      }

      "be able to deserialize case objects" in {
        val mine = Wardrobe(suits = List(WhatArmstrongWore, Zoot))
        val dbo = grater[Wardrobe].asDBObject(mine)
        val mine_* = grater[Wardrobe].asObject(dbo)
        mine must_== mine_*
      }

      // TODO: list of case objects

      "handle case objects whose top-level trait is not annotated with @Salat" in {
        val u = SlippedDownADrain
        val dbo: MongoDBObject = grater[Una].asDBObject(u)
        dbo must havePair("_typeHint" -> "com.github.salat.test.model.SlippedDownADrain$")
        val u_* = grater[Una].asObject(dbo)
        u must_== u
      }
    }
  }
}
