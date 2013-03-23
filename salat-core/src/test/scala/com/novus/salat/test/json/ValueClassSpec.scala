/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         JsonSpec.scala
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
package com.novus.salat.test.json

import org.specs2.mutable.Specification
import com.novus.salat._
import com.novus.salat.dao._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._

// set implicit context for DAO
package object context {
  val CustomTypeHint = "type"
  implicit val ctx = new Context {
    val name = "JsonContext-As-Needed"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = CustomTypeHint)
  }
}
import context._

// [*] Tested
class LongVC(val lv: Long) extends AnyVal
// [ ] Tested
class FloatVC(val f: Float) extends AnyVal
// [*] Tested
class DoubleVC(val d: Double) extends AnyVal
// [*] Tested
class BooleanVC(val b: Boolean) extends AnyVal
// [*] Tested
class IntVC(val i: Int) extends AnyVal
// [*] Tested
class StringVC(val s: String) extends AnyVal

case class FooLong(
  @Key("_id") name: String,
  hey: Option[Long],
  big: Long,
  lv: LongVC,
  olv: Option[LongVC],
  llv: List[LongVC],
  mlv: Map[String, LongVC])
case class FooString(
  @Key("_id") name: String,
  hey: Option[String],
  big: String,
  lv: StringVC,
  olv: Option[StringVC],
  llv: List[StringVC],
  mlv: Map[String, StringVC])
case class FooInt(
  @Key("_id") name: String,
  hey: Option[Int],
  big: Int,
  lv: IntVC,
  olv: Option[IntVC],
  llv: List[IntVC],
  mlv: Map[String, IntVC])
case class FooBoolean(
  @Key("_id") name: String,
  hey: Option[Boolean],
  big: Boolean,
  lv: BooleanVC,
  olv: Option[BooleanVC],
  llv: List[BooleanVC],
  mlv: Map[String, BooleanVC])
case class FooFloat(
  @Key("_id") name: String,
  hey: Option[Float],
  big: Float,
  lv: FloatVC,
  olv: Option[FloatVC],
  llv: List[FloatVC],
  mlv: Map[String, FloatVC])
case class FooDouble(
  @Key("_id") name: String,
  hey: Option[Double],
  big: Double,
  lv: DoubleVC,
  olv: Option[DoubleVC],
  llv: List[DoubleVC],
  mlv: Map[String, DoubleVC])

class ValueClassSpec extends Specification {

  MongoConnection().dropDatabase("salat_test_vc")
  "Value Class support" should {
    "handle converting value class objects to JObject" in {
      "serialize Long types" in {
        val v = 99123456789L
        val foo = FooLong("Fred",
          Some(v),
          v,
          new LongVC(v),
          Some(new LongVC(v)),
          List(new LongVC(v), new LongVC(v)),
          Map("Hey" -> new LongVC(v), "You" -> new LongVC(v)))
        val g = grater[FooLong]
        val rendered = g.toPrettyJSON(foo)
        val obj = g.fromJSON(rendered)
        g.toPrettyJSON(obj) must_== rendered
        obj.lv must_== new LongVC(v)
        object FooLongModel extends ModelCompanion[FooLong, String] {
          val dao = new SalatDAO[FooLong, String](collection = MongoConnection("localhost")("salat_test_vc")("vc_long")) {}
          override def defaultWriteConcern = com.mongodb.WriteConcern.ACKNOWLEDGED
        }
        FooLongModel.save(foo)
        val m = FooLongModel.findOneById("Fred")
        m.get.lv must_== new LongVC(v)
      }
      "serialize String types" in {
        val v = "Testing"
        val foo = FooString("Fred",
          Some(v),
          v,
          new StringVC(v),
          Some(new StringVC(v)),
          List(new StringVC(v), new StringVC(v)),
          Map("Hey" -> new StringVC(v), "You" -> new StringVC(v)))
        val g = grater[FooString]
        val rendered = g.toPrettyJSON(foo)
        val obj = g.fromJSON(rendered)
        g.toPrettyJSON(obj) must_== rendered
        obj.lv must_== new StringVC(v)
        object FooStringModel extends ModelCompanion[FooString, String] {
          val dao = new SalatDAO[FooString, String](collection = MongoConnection("localhost")("salat_test_vc")("vc_string")) {}
          override def defaultWriteConcern = com.mongodb.WriteConcern.ACKNOWLEDGED
        }
        FooStringModel.save(foo)
        val m = FooStringModel.findOneById("Fred")
        m.get.lv must_== new StringVC(v)
      }
      "serialize Int types" in {
        val v: Int = 25
        val foo = FooInt("Fred",
          Some(v),
          v,
          new IntVC(v),
          Some(new IntVC(v)),
          List(new IntVC(v), new IntVC(v)),
          Map("Hey" -> new IntVC(v), "You" -> new IntVC(v)))
        val g = grater[FooInt]
        val rendered = g.toPrettyJSON(foo)
        val obj = g.fromJSON(rendered)
        g.toPrettyJSON(obj) must_== rendered
        obj.lv must_== new IntVC(v)
        object FooIntModel extends ModelCompanion[FooInt, String] {
          val dao = new SalatDAO[FooInt, String](collection = MongoConnection("localhost")("salat_test_vc")("vc_Int")) {}
          override def defaultWriteConcern = com.mongodb.WriteConcern.ACKNOWLEDGED
        }
        FooIntModel.save(foo)
        val m = FooIntModel.findOneById("Fred")
        m.get.lv must_== new IntVC(v)
      }
      "serialize Boolean types" in {
        val v: Boolean = true
        val foo = FooBoolean("Fred",
          Some(v),
          v,
          new BooleanVC(v),
          Some(new BooleanVC(v)),
          List(new BooleanVC(v), new BooleanVC(v)),
          Map("Hey" -> new BooleanVC(v), "You" -> new BooleanVC(v)))
        val g = grater[FooBoolean]
        val rendered = g.toPrettyJSON(foo)
        val obj = g.fromJSON(rendered)
        g.toPrettyJSON(obj) must_== rendered
        obj.lv must_== new BooleanVC(v)
        object FooBooleanModel extends ModelCompanion[FooBoolean, String] {
          val dao = new SalatDAO[FooBoolean, String](collection = MongoConnection("localhost")("salat_test_vc")("vc_Boolean")) {}
          override def defaultWriteConcern = com.mongodb.WriteConcern.ACKNOWLEDGED
        }
        FooBooleanModel.save(foo)
        val m = FooBooleanModel.findOneById("Fred")
        m.get.lv must_== new BooleanVC(v)
      }
      "serialize Double types" in {
        val v: Double = 12.34
        val foo = FooDouble("Fred",
          Some(v),
          v,
          new DoubleVC(v),
          Some(new DoubleVC(v)),
          List(new DoubleVC(v), new DoubleVC(v)),
          Map("Hey" -> new DoubleVC(v), "You" -> new DoubleVC(v)))
        val g = grater[FooDouble]
        val rendered = g.toPrettyJSON(foo)
        val obj = g.fromJSON(rendered)
        g.toPrettyJSON(obj) must_== rendered
        obj.lv must_== new DoubleVC(v)
        object FooDoubleModel extends ModelCompanion[FooDouble, String] {
          val dao = new SalatDAO[FooDouble, String](collection = MongoConnection("localhost")("salat_test_vc")("vc_Double")) {}
          override def defaultWriteConcern = com.mongodb.WriteConcern.ACKNOWLEDGED
        }
        FooDoubleModel.save(foo)
        val m = FooDoubleModel.findOneById("Fred")
        m.get.lv must_== new DoubleVC(v)
      }
      "serialize Float types" in {
        val v = 12.34F
        val foo = FooFloat("Fred",
          Some(v),
          v,
          new FloatVC(v),
          Some(new FloatVC(v)),
          List(new FloatVC(v), new FloatVC(v)),
          Map("Hey" -> new FloatVC(v), "You" -> new FloatVC(v)))
        val g = grater[FooFloat]
        val rendered = g.toPrettyJSON(foo)
        val obj = g.fromJSON(rendered)
        g.toPrettyJSON(obj) must_== rendered
        obj.lv must_== new FloatVC(v)
        object FooFloatModel extends ModelCompanion[FooFloat, String] {
          val dao = new SalatDAO[FooFloat, String](collection = MongoConnection("localhost")("salat_test_vc")("vc_Float")) {}
          override def defaultWriteConcern = com.mongodb.WriteConcern.ACKNOWLEDGED
        }
        FooFloatModel.save(foo)
        val m = FooFloatModel.findOneById("Fred")
        m.get.lv must_== new FloatVC(v)
      }
    }
  }
}
