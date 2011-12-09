/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.test

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.test.model._
import com.mongodb.casbah.Imports._

class ListOfCommonTraitSpec extends SalatSpec {

  // TODO: replace this test with something that sucks less

  "a grater" should {
    "handle a case class containing a list typed to a common trait" in {

      val scc = SomeContainerClass(e = "Tergiversation",
        theListWhichNeedsToBeTested = List[SomeCommonTrait](
          SomeSubclassExtendingSaidTrait(b = 1),
          AnotherSubclassExtendingSaidTrait(d = 2.0),
          AnotherSubclassExtendingSaidTrait(d = 3.0),
          SomeSubclassExtendingSaidTrait(b = 4),
          SomeSubclassExtendingSaidTrait(b = 5)))

      val dbo: MongoDBObject = ctx.toDBObject(scc)
      //      Map(_typeHint -> com.novus.salat.test.model.SomeContainerClass, e -> Tergiversation,
      //        theListWhichNeedsToBeTested -> [
      //         { "_typeHint" : "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait" , "b" : 1} ,
      //         { "_typeHint" : "com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait" , "d" : 2.0} ,
      //         { "_typeHint" : "com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait" , "d" : 3.0} ,
      //         { "_typeHint" : "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait" , "b" : 4} ,
      //         { "_typeHint" : "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait" , "b" : 5}
      //        ])

      dbo must havePair("e" -> scc.e)
      dbo must haveKey("theListWhichNeedsToBeTested") // TODO: look at casbah specs and figure out how to test a DBList

      val scc_* = grater[SomeContainerClass].asObject(dbo)
      scc_* must_== scc

    }

    "handle a case class typed to a common trait" in {

      //      pendingUntilFixed {
      //        val sct1: SomeCommonTrait = SomeSubclassExtendingSaidTrait(b = 1)
      ////      val sct1Dbo: MongoDBObject = grater[SomeCommonTrait].asDBObject(sct1)
      ////      sct1Dbo must havePair("b" -> sct1.b)
      ////      val sct1_* = grater[SomeCommonTrait].asObject(sct1Dbo)
      ////      sct1_* must_== sct1
      //        fail("TODO: probably never feasible, but look through scalasig and try to sort it out")
      //      }
      //    }

      "handle a case class containing a list of case classes typed to a common trait" in {

        val sct1: SomeCommonTrait = SomeSubclassExtendingSaidTrait(b = 1)
        val sct2: SomeCommonTrait = AnotherSubclassExtendingSaidTrait(d = 2.0)
        val sct3: SomeCommonTrait = AnotherSubclassExtendingSaidTrait(d = 3.0)
        val sct4: SomeCommonTrait = SomeSubclassExtendingSaidTrait(b = 4)
        val sct5: SomeCommonTrait = SomeSubclassExtendingSaidTrait(b = 5)

        val scc = SomeContainerClass(e = "Neurotic", theListWhichNeedsToBeTested = List(sct1, sct2, sct3, sct4, sct5))

        val dbo: MongoDBObject = ctx.toDBObject(scc)
        //      Map(_typeHint -> com.novus.salat.test.model.SomeContainerClass,
        //       e -> Neurotic,
        //      theListWhichNeedsToBeTested -> [
        //        { "_typeHint" : "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait" , "b" : 1} ,
        //        { "_typeHint" : "com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait" , "d" : 2.0} ,
        //        { "_typeHint" : "com.novus.salat.test.model.AnotherSubclassExtendingSaidTrait" , "d" : 3.0} ,
        //        { "_typeHint" : "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait" , "b" : 4} ,
        //        { "_typeHint" : "com.novus.salat.test.model.SomeSubclassExtendingSaidTrait" , "b" : 5}
        //       ])

        dbo must havePair("e" -> scc.e)
        dbo must haveKey("theListWhichNeedsToBeTested") // TODO: look at casbah specs and figure out how to test a DBList

        val scc_* = grater[SomeContainerClass].asObject(dbo)
        scc_* must_== scc

      }

    }
  }
}