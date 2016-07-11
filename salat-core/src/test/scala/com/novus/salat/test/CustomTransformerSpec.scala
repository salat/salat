/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         CustomTransformerSpec.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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

package com.novus.salat.test

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.custom.Bicycle
import com.novus.salat.test.custom._

class CustomTransformerSpec extends SalatSpec {

  "Salat" should {
    "allow custom transformers for a Scala class whose serialized representation is a DBObject" in {
      WibbleTransformer.supportsGrater must beTrue
      val w = new Wibble("a", 4)
      val dbo = grater[Wibble].asDBObject(w)
      dbo should haveEntry("a" -> "a")
      dbo should haveEntry("b" -> 4)
      val w_* = grater[Wibble].asObject(dbo)
      w_* must_== w
    }
    "allow custom transformers for a Java class whose serialized representation is a DBObject" in {
      BicycleTransformer.supportsGrater must beTrue
      val b = new Bicycle(1, 2, 3)
      val dbo = grater[Bicycle].asDBObject(b)
      dbo should haveEntry("cadence" -> 1)
      dbo should haveEntry("speed" -> 2)
      dbo should haveEntry("gear" -> 3)
      val b_* = grater[Bicycle].asObject(dbo)
      b_* must_== b
    }
    "allow custom transformers for an embedded case class" in {
      val _id = new ObjectId
      val bar = Bar("b")
      val baz = Baz(1, 3.14)
      val foo = Foo(_id, bar, baz)
      val dbo = grater[Foo].asDBObject(foo)
      dbo should haveEntry("_t" -> foo.getClass.getName)
      dbo should haveEntry("bar" -> "b")
      dbo should haveEntry("baz._t" -> baz.getClass.getName)
      dbo should haveEntry("baz.a" -> 1)
      dbo should haveEntry("baz.b" -> 3.14)
      val foo_* = grater[Foo].asObject(dbo)
      foo_* must_== foo
    }
    "allow custom transformers for an option on a case class" in {
      val _id = new ObjectId
      val bar = Bar("b")
      val baz = Baz(1, 3.14)
      val foo = FooOptionBar(_id, Option(bar), baz)
      val dbo = grater[FooOptionBar].asDBObject(foo)
      dbo should haveEntry("_t" -> foo.getClass.getName)
      dbo should haveEntry("bar" -> "b")
      dbo should haveEntry("baz._t" -> baz.getClass.getName)
      dbo should haveEntry("baz.a" -> 1)
      dbo should haveEntry("baz.b" -> 3.14)
      val foo_* = grater[FooOptionBar].asObject(dbo)
      foo_* must_== foo
    }
    "allow custom transformers for a traversable containing a case class" in {
      val _id = new ObjectId
      val bar1 = Bar("b1")
      val bar2 = Bar("b2")
      val baz = Baz(1, 3.14)
      val foo = FooListBar(_id, List(bar1, bar2), baz)
      val dbo = grater[FooListBar].asDBObject(foo)
      dbo should haveEntry("_t" -> foo.getClass.getName)
      dbo must haveField("bar")
      dbo.getAsOrElse[MongoDBList]("bar", DBList.empty).toList must_== List(Some("b1"), Some("b2"))
      dbo should haveEntry("baz._t" -> baz.getClass.getName)
      dbo should haveEntry("baz.a" -> 1)
      dbo should haveEntry("baz.b" -> 3.14)
      val foo_* = grater[FooListBar].asObject(dbo)
      foo_* must_== foo
    }
    "allow custom transformers for a map containing a case class" in {
      val _id = new ObjectId
      val bar1 = Bar("b1")
      val bar2 = Bar("b2")
      val baz = Baz(1, 3.14)
      val foo = FooMapBar(_id, Map("key1" -> bar1, "key2" -> bar2), baz)
      val dbo = grater[FooMapBar].asDBObject(foo)
      dbo should haveEntry("_t" -> foo.getClass.getName)
      dbo must haveEntry("bar.key1" -> "b1")
      dbo must haveEntry("bar.key2" -> "b2")
      dbo should haveEntry("baz._t" -> baz.getClass.getName)
      dbo should haveEntry("baz.a" -> 1)
      dbo should haveEntry("baz.b" -> 3.14)
      val foo_* = grater[FooMapBar].asObject(dbo)
      foo_* must_== foo
    }
  }

}
