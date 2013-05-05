/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         JsonSpec.scala
 * Last modified: 2013-02-25 21:07:26 EST
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

import com.novus.salat._
import com.novus.salat.util._
import com.novus.salat.json._
import org.bson.types.ObjectId
import org.joda.time.DateTimeConstants._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }
import org.json4s._
import org.specs2.mutable.Specification
import scala.util.parsing.json.{ JSONObject, JSONArray }

class JsonSpec extends Specification with Logging {

  // TODO: @Key
  // TODO: @Ignore
  // TODO: @Persist

  val zone = DateTimeZone.forID("US/Eastern")
  val o = new ObjectId("4fd0bead4ceab231e6f3220b")
  val bd = BigDecimal(-9.123456789)
  val bi = BigInt(123456)
  val a = Adam(a = "string", b = 99, c = 3.14, d = false, e = testDate, u = testURL, bd = bd, bi = bi, o = o)
  val ints = List(1, 2, 3)
  val strings = List("a", "b", "c")
  val b = Bertil(ints = ints, strings = strings)
  val g = Gustav(o = Some("OG"))
  val n = Niklas(Some(g))
  val date = new DateTime(2012, SEPTEMBER, 13, 8, 30, 5, 237, zone)

  "JSON support" should {
    "handle converting model objects to JObject" in {
      "serialize simple types" in {
        val rendered = grater[Adam].toPrettyJSON(a)
        rendered must /("a" -> "string")
        rendered must /("b" -> 99)
        rendered must /("c" -> 3.14)
        rendered must /("d" -> false)
        rendered must /("e" -> "2011-12-28T14:37:56.008Z")
        rendered must /("u" -> testURL.toString)
        rendered must /("bd" -> -9.123456789)
        rendered contains """"bi":[1,-30,64]""" must_== true
        //        rendered must /("bi" -> "[1.0, -30.0, 64.0]")
        //        rendered must /("bi" -> 123456)
        rendered must /("o") / ("$oid" -> "4fd0bead4ceab231e6f3220b")
      }
      "serialize lists of simple types" in {
        val rendered = grater[Bertil].toPrettyJSON(b)
        rendered must /("ints" -> JSONArray(ints))
        rendered must /("strings" -> JSONArray(strings))
      }
      "serialize lists of case classes" in {
        val ints = List(1, 2, 3)
        val strings = List("a", "b", "c")
        val b1 = Bertil(ints = ints, strings = strings)
        val b2 = Bertil(ints = ints.map(_ * 2), strings = strings.map(_.capitalize))
        val c = Caesar(l = List(b1, b2))
        val rendered = grater[Caesar].toPrettyJSON(c)
        rendered must /("l" -> JSONArray(List(
          JSONObject(Map("ints" -> JSONArray(ints), "strings" -> JSONArray(strings))),
          JSONObject(Map("ints" -> JSONArray(ints.map(_ * 2)), "strings" -> JSONArray(strings.map(_.capitalize)))))))
      }
      "serialize maps of simple types" in {
        val d = David(m = Map("a" -> 1, "b" -> 2, "c" -> 3))
        val rendered = grater[David].toPrettyJSON(d)
        rendered must /("m") / ("a" -> 1.0) // by default, specs2 parses numbers in JSON as doubles
        rendered must /("m") / ("b" -> 2.0)
        rendered must /("m") / ("c" -> 3.0)
      }
      "serialize maps of case classes" in {
        val f = Filip(m = Map("e1" -> Erik(e = "Erik"), "e2" -> Erik(e = "Another Erik")))
        val rendered = grater[Filip].toPrettyJSON(f)
        rendered must /("m") / ("e1") / ("e" -> "Erik")
        rendered must /("m") / ("e2") / ("e" -> "Another Erik")
      }
      "serialize Options Some[A]" in {
        "simple type" in {
          grater[Gustav].toPrettyJSON(g) must /("o" -> "OG")
        }
        "case class" in {
          grater[Niklas].toPrettyJSON(n) must /("g") */ ("o" -> "OG")
        }
        val petter = Petter(Some(date))
        "DateTime using timestamp strategy" in {
          implicit val ctx = new Context {
            val name = "timestamp"
            override val jsonConfig = JSONConfig(dateStrategy = TimestampDateStrategy(DateTimeZone.UTC))
          }
          grater[Petter].toCompactJSON(petter) must_== "{\"d\":1347539405237}"
        }
        "DateTime using strict JSON strategy" in {
          implicit val ctx = new Context {
            val name = "strict-JSON"
            override val jsonConfig = JSONConfig(dateStrategy = StrictJSONDateStrategy(DateTimeZone.UTC))
          }
          grater[Petter].toCompactJSON(petter) must_== "{\"d\":{\"$date\":1347539405237}}"
        }
        "DateTime using string strategy" in {
          grater[Petter].toCompactJSON(petter) must_== "{\"d\":\"2012-09-13T12:30:05.237Z\"}"
        }
        "BigDecimal using double strategy" in {
          grater[Qvintus].toCompactJSON(Qvintus(bd = Some(bd))) must_== "{\"bd\":-9.123456789}"
        }
        "BigInt using long strategy" in {
          grater[Rudolf].toCompactJSON(Rudolf(bi = Some(bi))) must_== "{\"bi\":[1,-30,64]}" //"{\"bi\":123456}"
        }
        "ObjectId using string strategy" in {
          implicit val ctx = new Context {
            val name = "test_oid_string"
            override val jsonConfig = JSONConfig(objectIdStrategy = StringObjectIdStrategy)
          }
          grater[Sigurd].toCompactJSON(Sigurd(o = Some(o))) must_== "{\"o\":\"4fd0bead4ceab231e6f3220b\"}"
        }
        "ObjectId using strict strategy" in {
          implicit val ctx = new Context {
            val name = "test_oid_strict"
            override val jsonConfig = JSONConfig(objectIdStrategy = StrictJSONObjectIdStrategy)
          }
          grater[Sigurd].toCompactJSON(Sigurd(o = Some(o))) must_== "{\"o\":{\"$oid\":\"4fd0bead4ceab231e6f3220b\"}}"
        }
      }
      "serailize Options None" in {
        grater[Gustav].toCompactJSON(Gustav(o = None)) must_== "{}"
      }
      "serialize DateTime using timestamp strategy" in {
        val olof = Olof(date)
        implicit val ctx = new Context {
          val name = "timestamp"
          override val jsonConfig = JSONConfig(dateStrategy = TimestampDateStrategy(DateTimeZone.UTC))
        }
        grater[Olof].toCompactJSON(olof) must_== "{\"d\":1347539405237}"
      }
      "serialize DateTime using strict JSON strategy" in {
        val olof = Olof(date)
        implicit val ctx = new Context {
          val name = "strict-JSON"
          override val jsonConfig = JSONConfig(dateStrategy = StrictJSONDateStrategy(DateTimeZone.UTC))
        }
        grater[Olof].toCompactJSON(olof) must_== "{\"d\":{\"$date\":1347539405237}}"
      }
      "serialize DateTime using string strategy" in {
        val olof = Olof(date)
        grater[Olof].toCompactJSON(olof) must_== "{\"d\":\"2012-09-13T12:30:05.237Z\"}"
      }
      // TODO: sort out type hinting when concrete grater is accessed via proxy grater without @Salat annotation
      "serialize class hierarchies with a top-level trait" in {
        val i = Ivar(s = "Hello")
        val i_* = grater[Helge].toPrettyJSON(i)
        i_* must /("_t" -> "com.novus.salat.test.json.Ivar")
        i_* must /("s" -> "Hello")
        val j = Johan(s = "Hello", d = 3.14)
        val j_* = grater[Helge].toPrettyJSON(j)
        j_* must /("_t" -> "com.novus.salat.test.json.Johan")
        j_* must /("s" -> "Hello")
        j_* must /("d" -> 3.14)
      }
      "serialize class hierarchies with an abstract superclass" in {
        val l = Ludvig(s = "Hello")
        val l_* = grater[Kalle].toPrettyJSON(l)
        l_* must /("_t" -> "com.novus.salat.test.json.Ludvig")
        l_* must /("s" -> "Hello")
        val m = Martin(s = "Hello", d = 3.14)
        val m_* = grater[Kalle].toPrettyJSON(m)
        m_* must /("_t" -> "com.novus.salat.test.json.Martin")
        m_* must /("s" -> "Hello")
        m_* must /("d" -> 3.14)
      }

      // ------------- Nested Combinations

      "serialize lists of case classes" in {
        val ln = ListList("Fred", List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4))))
        val js = grater[ListList].toCompactJSON(ln)
        js must_== """{"name":"Fred","stuff":[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]]}"""
        grater[ListList].fromJSON(js) must_== ln
      }
      "serialize nested lists of case classes" in {
        val ln = ListListList("Fred",
          List(
            List(
              List(
                Animal("mouse", 4),
                Animal("bug", 6)),
              List(
                Animal("whale", 0),
                Animal("elephant", 4))),
            List(
              List(
                Animal("millipede", 1000),
                Animal("slug", 0)),
              List(
                Animal("bird", 2),
                Animal("tiger", 4)))))
        val js = grater[ListListList].toCompactJSON(ln)
        js must_== """{"name":"Fred","stuff":[[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]],[[{"name":"millipede","legs":1000},{"name":"slug","legs":0}],[{"name":"bird","legs":2},{"name":"tiger","legs":4}]]]}"""
        grater[ListListList].fromJSON(js) must_== ln
      }
      // NOTE: If your list has a None it it, this will be lost upon re-marshal from JSON as JSON has no representation
      //       for a None (it's simply missing from the list).
      "serialize a list of option of case classe" in {
        val lop = ListOpt("Jenny", List(Some(Animal("mouse", 4)), None, Some(Animal("whale", 0))))
        val js = grater[ListOpt].toCompactJSON(lop)
        js must_== """{"name":"Jenny","stuff":[{"name":"mouse","legs":4},{"name":"whale","legs":0}]}"""
        grater[ListOpt].fromJSON(js) must_== lop.copy(stuff = lop.stuff.filter(_.isDefined))
      }
      "serialize a list of map of case classe" in {
        val lm = ListMap("Jenny", List(Map("a" -> Animal("mouse", 4)), Map("b" -> Animal("whale", 0))))
        val js = grater[ListMap].toCompactJSON(lm)
        js must_== """{"name":"Jenny","stuff":[{"a":{"name":"mouse","legs":4}},{"b":{"name":"whale","legs":0}}]}"""
        grater[ListMap].fromJSON(js) must_== lm
      }
      "serialize an option of list of case classes" in {
        val oln = OpList("Wow", Some(List(Animal("mouse", 4), Animal("bug", 6))))
        val js = grater[OpList].toCompactJSON(oln)
        js must_== """{"name":"Wow","opList":[{"name":"mouse","legs":4},{"name":"bug","legs":6}]}"""
        grater[OpList].fromJSON(js) must_== oln
      }
      "serailize an option of nested lists of case classes" in {
        val oln = OpListList("Yay", Some(List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4)))))
        val js = grater[OpListList].toCompactJSON(oln)
        js must_== """{"name":"Yay","opListList":[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]]}"""
        grater[OpListList].fromJSON(js) must_== oln
      }
      "serailize an option of map of case classes" in {
        val om = OpMap("Wow", Some(Map("hello" -> (Animal("mouse", 4)))))
        val js = grater[OpMap].toCompactJSON(om)
        js must_== """{"name":"Wow","opMap":{"hello":{"name":"mouse","legs":4}}}"""
        grater[OpMap].fromJSON(js) must_== om
        val om2 = OpMap("Wow", None)
        val js2 = grater[OpMap].toCompactJSON(om2)
        js2 must_== """{"name":"Wow"}"""
        grater[OpMap].fromJSON(js2) must_== om2
      }
      "serailize an nested option of case class" in {
        val oop = OpOp("Oops", Some(Some(Animal("mouse", 4))))
        val js = grater[OpOp].toCompactJSON(oop)
        js must_== """{"name":"Oops","opts":{"name":"mouse","legs":4}}"""
        grater[OpOp].fromJSON(js) must_== oop
        val oop2 = OpOp("Oops", None)
        val js2 = grater[OpOp].toCompactJSON(oop2)
        js2 must_== """{"name":"Oops"}"""
        grater[OpOp].fromJSON(js2) must_== oop2
      }
      "serialize a map of list of case classes" in {
        val mln = MapList("Bob", Map("Mike" -> List(Animal("mouse", 4), Animal("bug", 6)), "Sally" -> List(Animal("whale", 0), Animal("elephant", 4))))
        val js = grater[MapList].toCompactJSON(mln)
        js must_== """{"name":"Bob","mapList":{"Mike":[{"name":"mouse","legs":4},{"name":"bug","legs":6}],"Sally":[{"name":"whale","legs":0},{"name":"elephant","legs":4}]}}"""
        grater[MapList].fromJSON(js) must_== mln
      }
      "serialize a map of nested lists of case classes" in {
        val mln = MapListList("Bob", Map("Everyone" -> List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4)))))
        val js = grater[MapListList].toCompactJSON(mln)
        js must_== """{"name":"Bob","mapList":{"Everyone":[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]]}}"""
        grater[MapListList].fromJSON(js) must_== mln
      }
      "serialize a map of option of case classe" in {
        val a: Option[Animal] = None
        val mln = MapOpt("Bob", Map("things" -> Some(Animal("mouse", 4)), "otherthings" -> a))
        val js = grater[MapOpt].toCompactJSON(mln)
        js must_== """{"name":"Bob","mapOpt":{"things":{"name":"mouse","legs":4}}}"""
        grater[MapOpt].fromJSON(js) must_== mln.copy(mapOpt = mln.mapOpt.filter({ case (k, v) => v.isDefined }))
      }
      "serialize a map of map of case class" in {
        val mm = MapMap("Bob", Map("things" -> Map("a" -> Animal("mouse", 4), "b" -> Animal("horse", 4)), "stuff" -> Map("c" -> Animal("sloth", 2))))
        val js = grater[MapMap].toCompactJSON(mm)
        js must_== """{"name":"Bob","mapmap":{"things":{"a":{"name":"mouse","legs":4},"b":{"name":"horse","legs":4}},"stuff":{"c":{"name":"sloth","legs":2}}}}"""
        grater[MapMap].fromJSON(js) must_== mm
      }
    }
    "handle converting a list of model objects to a JArray" in {
      val jarr = grater[Kalle].toJSONArray(List(Martin("one", 1.1), Martin("two", 2.2), Martin("three", 3.3)))
      jarr.arr must have size (3)
      jarr.arr must contain(
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("one")) :: JField("d", JDouble(1.1)) :: Nil).asInstanceOf[JValue],
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("two")) :: JField("d", JDouble(2.2)) :: Nil).asInstanceOf[JValue],
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("three")) :: JField("d", JDouble(3.3)) :: Nil).asInstanceOf[JValue]).only.inOrder
    }
    "deserialize JObjects containing simple types" in {
      val j = JObject(
        JField("a", JString("string")) ::
          JField("b", JInt(99)) ::
          JField("c", JDouble(3.14)) ::
          JField("d", JBool(false)) ::
          JField("e", JString("2011-12-28T14:37:56.008Z")) ::
          JField("u", JString(testURL.toString)) ::
          JField("bd", JDouble(bd.doubleValue())) ::
          JField("bi", JInt(bi)) ::
          JField("o", JObject(JField("$oid", JString("4fd0bead4ceab231e6f3220b")) :: Nil)) ::
          Nil)
      grater[Adam].fromJSON(j) must_== a
    }
    "deserialize JObjects containing DateTimes" in {
      val olof = Olof(date)
      val petter = Petter(Some(date))
      "DateTime using timestamp strategy" in {
        implicit val ctx = new Context {
          val name = "timestamp"
          override val jsonConfig = JSONConfig(dateStrategy = TimestampDateStrategy(zone))
        }
        val s = "{\"d\":1347539405237}"
        grater[Petter].fromJSON(s) must_== petter
        grater[Olof].fromJSON("{\"d\":1347539405237}") must_== olof
      }
      "DateTime using strict JSON strategy" in {
        implicit val ctx = new Context {
          val name = "strict-JSON"
          override val jsonConfig = JSONConfig(dateStrategy = StrictJSONDateStrategy(zone))
        }
        val s = "{\"d\":{\"$date\":1347539405237}}"
        grater[Petter].fromJSON(s) must_== petter
        grater[Olof].fromJSON("{\"d\":{\"$date\":1347539405237}}") must_== olof
      }
      "DateTime using string strategy" in {
        implicit val ctx = new Context {
          val name = "string-datetime"
          override val jsonConfig = JSONConfig(dateStrategy = StringDateStrategy(ISODateTimeFormat.dateTime().withZone(zone)))
        }
        val s = "{\"d\":\"2012-09-13T08:30:05.237-04:00\"}"
        grater[Petter].fromJSON(s) must_== petter
        grater[Olof].fromJSON(s) must_== olof
      }
    }
    "deserialize JObjects containing lists" in {
      "of simple types" in {
        val j = JObject(
          JField("ints", JArray(ints.map(JInt(_)))) ::
            JField("strings", JArray(strings.map(JString(_)))) ::
            Nil)
        grater[Bertil].fromJSON(j) must_== b
      }
      "of case classes" in {
        val j = JObject(JField("l",
          JArray(
            JObject(JField("ints", JArray(ints.map(JInt(_)))) :: JField("strings", JArray(strings.map(JString(_)))) :: Nil) ::
              JObject(JField("ints", JArray(ints.map(i => JInt(i * 2)))) :: JField("strings", JArray(strings.map(s => JString(s.capitalize)))) :: Nil) ::
              Nil))
          :: Nil)
        val c = Caesar(l = List(
          Bertil(ints = ints, strings = strings), Bertil(ints = ints.map(_ * 2),
            strings = strings.map(_.capitalize))))
        grater[Caesar].fromJSON(j) must_== c
      }
    }
    "deserialize JObjects containing maps" in {
      "of simple types" in {
        val j = JObject(
          JField("m", JObject(
            JField("a", JInt(1)) ::
              JField("b", JInt(2)) ::
              JField("c", JInt(3)) ::
              Nil)) ::
            Nil)
        grater[David].fromJSON(j) must_== David(m = Map("a" -> 1, "b" -> 2, "c" -> 3))
      }
      "of case classes" in {
        val j = JObject(
          JField("m", JObject(
            JField("e1", JObject(JField("e", JString("Erik")) :: Nil)) ::
              JField("e2", JObject(JField("e", JString("Another Erik")) :: Nil)) ::
              Nil)) ::
            Nil)
        grater[Filip].fromJSON(j) must_== Filip(m = Map("e1" -> Erik(e = "Erik"), "e2" -> Erik(e = "Another Erik")))
      }
    }
    "deserialize JObjects where the model object contains Option fields" in {
      "Some[A]" in {
        "simple type" in {
          grater[Gustav].fromJSON(JObject(JField("o", JString("OG")) :: Nil)) must_== g
        }
        "BigDecimal using Double strategy" in {
          grater[Qvintus].fromJSON("{\"bd\":-9.123456789}") must_== Qvintus(Some(bd))
        }
        "BigInt using Long strategy" in {
          // ZZZ ERROR: 
          //   Salat is (properly?) encoding a BI as a byte array but isn't able to decode it.
          //   Check Salat root to see if it has the same problem.
          grater[Rudolf].fromJSON("{\"bi\":123456}") must_== Rudolf(Some(bi))
        }
        "ObjectId using String strategy" in {
          implicit val ctx = new Context {
            val name = "test_oid_string"
            override val jsonConfig = JSONConfig(objectIdStrategy = StringObjectIdStrategy)
          }
          grater[Sigurd].fromJSON("{\"o\":\"4fd0bead4ceab231e6f3220b\"}") must_== Sigurd(Some(o))
        }
        "ObjectId using strict strategy" in {
          implicit val ctx = new Context {
            val name = "test_oid_strict"
            override val jsonConfig = JSONConfig(objectIdStrategy = StrictJSONObjectIdStrategy)
          }
          grater[Sigurd].fromJSON("{\"o\":{\"$oid\":\"4fd0bead4ceab231e6f3220b\"}}") must_== Sigurd(Some(o))
        }

        "case class" in {
          grater[Niklas].fromJSON(JObject(
            JField("g", JObject(JField("o", JString("OG")) :: Nil)) :: Nil)) must_== n
        }
      }
      "None" in {
        grater[Gustav].fromJSON(JObject(Nil)) must_== Gustav(o = None)
        grater[Qvintus].fromJSON("{}") must_== Qvintus(None)
        grater[Rudolf].fromJSON("{}") must_== Rudolf(None)
      }
    }
    "deserialize JObjects containing class hierarchies" in {
      "with a top-level trait" in {
        grater[Helge].fromJSON(JObject(
          JField("_t", JString("com.novus.salat.test.json.Ivar")) ::
            JField("s", JString("Hello")) ::
            Nil)) must_== Ivar(s = "Hello")
        grater[Helge].fromJSON(JObject(
          JField("_t", JString("com.novus.salat.test.json.Johan")) ::
            JField("s", JString("Hello")) ::
            JField("d", JDouble(3.14)) ::
            Nil)) must_== Johan(s = "Hello", d = 3.14)
      }
      "with an abstract superclass" in {
        grater[Kalle].fromJSON(JObject(
          JField("_t", JString("com.novus.salat.test.json.Ludvig")) ::
            JField("s", JString("Hello")) ::
            Nil)) must_== Ludvig(s = "Hello")
        grater[Kalle].fromJSON(JObject(
          JField("_t", JString("com.novus.salat.test.json.Martin")) ::
            JField("s", JString("Hello")) ::
            JField("d", JDouble(3.14)) ::
            Nil)) must_== Martin(s = "Hello", d = 3.14)
      }
    }
    "handle Enums" in {
      val b = Blather("Greg", Map("a" -> Scope.TWO), Scope.TWO)
      val js = grater[Blather].toCompactJSON(b)
      val b2 = grater[Blather].fromJSON(js)
      val z = b2.scope.get("a")
      z must_== Some(Scope.TWO)
      val y = z.flatMap(b => Some(true)) // This goes boom w/ClassCastException before fix
      y must_== Some(true) // ensure the above didn't blow up
    }
    "strings" in {
      "a string that can be parsed to JSON object" in {
        val adam = """{"a":"string","b":99,"c":3.14,"d":false,"e":"2011-12-28T14:37:56.008Z","bd":-9.123456789,"bi":123456,"u":"http://www.typesafe.com","o":{"$oid":"4fd0bead4ceab231e6f3220b"}}"""
        grater[Adam].fromJSON(adam) must_== a
        grater[Bertil].fromJSON("""{"ints":[1,2,3],"strings":["a","b","c"]}""") must_== b
        grater[Niklas].fromJSON("""{"g":{"o":"OG"}}""") must_== n
      }
      "a string that can be parsed to a JSON array" in {
        val arr = """[{"_t":"com.novus.salat.test.json.Martin","s":"one","d":1.1},{"_t":"com.novus.salat.test.json.Martin","s":"two","d":2.2},{"_t":"com.novus.salat.test.json.Martin","s":"three","d":3.3}]"""
        grater[Kalle].fromJSONArray(arr) must_== List(Martin("one", 1.1), Martin("two", 2.2), Martin("three", 3.3))
      }
      "throw an exception when string cannot be parsed to valid JSON" in {
        val invalid = """?"""
        grater[Adam].fromJSON(invalid) must throwA[org.json4s.ParserUtil.ParseException]
      }
      "throw an exception when string parses to valid but unexpected JSON" in {
        grater[Adam].fromJSON("""["a","b","c"]""") must throwA[RuntimeException]
      }
    }
    "JArray" in {
      val j = JArray(
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("one")) :: JField("d", JDouble(1.1)) :: Nil) ::
          JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("two")) :: JField("d", JDouble(2.2)) :: Nil) ::
          JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("three")) :: JField("d", JDouble(3.3)) :: Nil) ::
          Nil)
      grater[Kalle].fromJSONArray(j) must_== List(Martin("one", 1.1), Martin("two", 2.2), Martin("three", 3.3))
    }
  }
}
