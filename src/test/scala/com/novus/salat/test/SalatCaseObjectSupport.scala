/**
* Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
* For questions and comments about this product, please see the project page at:
*
* http://github.com/novus/salat
*
*/
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class SalatCaseObjectSupport extends SalatSpec {
  "a grater" should {
    "support case objects" in {
      "be able to serialize case objects" in {
        val mine = Wardrobe(suits = List(Zoot))
        log.info("mine: %s", mine)
        val dbo: MongoDBObject = grater[Wardrobe].asDBObject(mine)
        log.info("dbo : %s", dbo)
        val suits = dbo.expand[BasicDBList]("suits")
        suits must beSome[BasicDBList].which {
          suits => val suit: MongoDBObject = suits(0).asInstanceOf[DBObject]
          val th = suit.expand[String]("_typeHint")
          th must beSome[String].which {
            th => th == Zoot.getClass.getName
          }
        }
      }

      "be able to deserialize case objects" in {
        val mine = Wardrobe(suits = List(WhatArmstrongWore, Zoot))
        val dbo = grater[Wardrobe].asDBObject(mine)
        val mine_* = grater[Wardrobe].asObject(dbo)
        mine must_== mine_*
      }
    }
  }
}