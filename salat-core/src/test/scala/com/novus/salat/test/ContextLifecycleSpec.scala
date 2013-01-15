/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ContextLifecycleSpec.scala
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

import com.novus.salat.test.model._
import com.novus.salat._
import com.mongodb.casbah.Imports._

class ContextLifecycleSpec extends SalatSpec {

  "Context lifecycle" should {
    "allow manually clearing graters" in new testContext {
      val g1 = grater[Alice]
      val g2 = grater[Basil]
      val g3 = grater[Clara]
      ctx.graters must haveSize(3)
      ctx.clearAllGraters()
      ctx.graters must beEmpty
    }
    "allow manually clearing a single grater" in new testContext {
      val g1 = grater[Alice]
      val g2 = grater[Basil]
      val g3 = grater[Clara]
      ctx.graters must haveSize(3)
      ctx.clearGrater(classOf[Alice].getName) //must beSome(g1.asInstanceOf[Grater[_ <: AnyRef]])
      ctx.graters must haveSize(2)
      ctx.graters must not have key(classOf[Alice].getName)
      ctx.graters must have key (classOf[Basil].getName)
      ctx.graters must have key (classOf[Clara].getName)

    }
  }

}
