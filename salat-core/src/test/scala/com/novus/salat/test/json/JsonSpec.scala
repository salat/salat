/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         JsonSpec.scala
 * Last modified: 2012-06-28 15:37:34 EDT
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
package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.util._
import org.specs2.mutable.Specification
import net.liftweb.json._
import scala.util.parsing.json.{ JSONObject, JSONArray }
import org.bson.types.ObjectId
import net.liftweb.json.JsonParser.ParseException

class JsonSpec extends Specification with Logging {

  // TODO: @Key
  // TODO: @Ignore
  // TODO: @Persist

  val o = new ObjectId("4fd0bead4ceab231e6f3220b")
  val a = Adam(a = "string", b = 99, c = 3.14, d = false, e = testDate, u = testURL, o = o)
  val ints = List(1, 2, 3)
  val strings = List("a", "b", "c")
  val b = Bertil(ints = ints, strings = strings)
  val g = Gustav(o = Some("OG"))
  val n = Niklas(Some(g))

  "JSON support" should {
    "handle converting model objects to JObject" in {
      "simple types" in {
        val rendered = grater[Adam].toPrettyJSON(a)
        //        00:15:46.298 [specs2.DefaultExecutionStrategy4] DEBUG c.novus.salat.test.json.JsonSpec - {
        //          "a":"string",
        //          "b":99,
        //          "c":3.14,
        //          "d":false,
        //          "e":"2011-12-28T14:37:56.008-05:00",
        //          "u":"http://www.typesafe.com",
        //          "o":{
        //            "$oid":"4fd0bead4ceab231e6f3220b"
        //          }
        //        }
        rendered must /("a" -> "string")
        rendered must /("b" -> 99)
        rendered must /("c" -> 3.14)
        rendered must /("d" -> false)
        rendered must /("e" -> "2011-12-28T14:37:56.008Z")
        rendered must /("u" -> testURL.toString)
        rendered must /("o") / ("$oid" -> "4fd0bead4ceab231e6f3220b")
      }
      "lists" in {
        "of simple types" in {
          val rendered = grater[Bertil].toPrettyJSON(b)
          //        log.debug(rendered)
          //        09:47:43.440 [specs2.DefaultExecutionStrategy4] DEBUG c.novus.salat.test.json.JsonSpec - {
          //          "ints":[1,2,3],
          //          "strings":["a","b","c"]
          //        }

          rendered must /("ints" -> JSONArray(ints))
          rendered must /("strings" -> JSONArray(strings))
        }
        "of case classes" in {
          val ints = List(1, 2, 3)
          val strings = List("a", "b", "c")
          val b1 = Bertil(ints = ints, strings = strings)
          val b2 = Bertil(ints = ints.map(_ * 2), strings = strings.map(_.capitalize))
          val c = Caesar(l = List(b1, b2))
          val rendered = grater[Caesar].toPrettyJSON(c)
          //        log.debug(rendered)
          //        09:50:47.309 [specs2.DefaultExecutionStrategy4] DEBUG c.novus.salat.test.json.JsonSpec - {
          //          "l":[{
          //            "ints":[1,2,3],
          //            "strings":["a","b","c"]
          //          },{
          //            "ints":[2,4,6],
          //            "strings":["A","B","C"]
          //          }]
          //        }
          rendered must /("l" -> JSONArray(List(
            JSONObject(Map("ints" -> JSONArray(ints), "strings" -> JSONArray(strings))),
            JSONObject(Map("ints" -> JSONArray(ints.map(_ * 2)), "strings" -> JSONArray(strings.map(_.capitalize)))))))
        }
      }
      "maps" in {
        "of simple types" in {
          val d = David(m = Map("a" -> 1, "b" -> 2, "c" -> 3))
          val rendered = grater[David].toPrettyJSON(d)
          //          log.debug(rendered)
          //          11:12:38.127 [specs2.DefaultExecutionStrategy4] DEBUG c.novus.salat.test.json.JsonSpec - {
          //            "m":{
          //              "a":1,
          //              "b":2,
          //              "c":3
          //            }
          //          }
          rendered must /("m") / ("a" -> 1.0) // by default, specs2 parses numbers in JSON as doubles
          rendered must /("m") / ("b" -> 2.0)
          rendered must /("m") / ("c" -> 3.0)
        }
        "of case classes" in {
          val f = Filip(m = Map("e1" -> Erik(e = "Erik"), "e2" -> Erik(e = "Another Erik")))
          val rendered = grater[Filip].toPrettyJSON(f)
          //          "m":{
          //            "e1":{
          //              "e":"Erik"
          //            },
          //            "e2":{
          //              "e":"Another Erik"
          //            }
          //          }
          //        }
          rendered must /("m") / ("e1") / ("e" -> "Erik")
          rendered must /("m") / ("e2") / ("e" -> "Another Erik")
        }
      }
      "Options" in {
        "Some[A]" in {
          "simple type" in {
            grater[Gustav].toPrettyJSON(g) must /("o" -> "OG")
          }
          "case class" in {
            grater[Niklas].toPrettyJSON(n) must /("g") */ ("o" -> "OG")
          }
        }
        "None" in {
          grater[Gustav].toCompactJSON(Gustav(o = None)) must_== "{}"
        }
      }
      "class hierarchies" in {
        // TODO: sort out type hinting when concrete grater is accessed via proxy grater without @Salat annotation
        "with a top-level trait" in {
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
        "with an abstract superclass" in {
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
      }
    }
    "handle converting a list of model objects to a JArray" in {
      val jarr = grater[Kalle].toJSONArray(List(Martin("one", 1.1), Martin("two", 2.2), Martin("three", 3.3)))
      //        [{
      //          "_t":"com.novus.salat.test.json.Martin",
      //          "s":"one",
      //          "d":1.1
      //        },{
      //          "_t":"com.novus.salat.test.json.Martin",
      //          "s":"two",
      //          "d":2.2
      //        },{
      //          "_t":"com.novus.salat.test.json.Martin",
      //          "s":"three",
      //          "d":3.3
      //        }]
      jarr.arr must have size (3)
      jarr.arr must contain(
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("one")) :: JField("d", JDouble(1.1)) :: Nil).asInstanceOf[JValue],
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("two")) :: JField("d", JDouble(2.2)) :: Nil).asInstanceOf[JValue],
        JObject(JField("_t", JString("com.novus.salat.test.json.Martin")) :: JField("s", JString("three")) :: JField("d", JDouble(3.3)) :: Nil).asInstanceOf[JValue]).only.inOrder
    }
    "handle converting JSON to model objects" in {
      "JObjects" in {
        "containing simple types" in {
          val j = JObject(
            JField("a", JString("string")) ::
              JField("b", JInt(99)) ::
              JField("c", JDouble(3.14)) ::
              JField("d", JBool(false)) ::
              JField("e", JString("2011-12-28T14:37:56.008Z")) ::
              JField("u", JString(testURL.toString)) ::
              JField("o", JObject(JField("$oid", JString("4fd0bead4ceab231e6f3220b")) :: Nil)) ::
              Nil)
          grater[Adam].fromJSON(j) must_== a
        }
        "containing lists" in {
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
        "containing maps" in {
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
        "where the model object contains Option fields" in {
          "Some[A]" in {
            "simple type" in {
              grater[Gustav].fromJSON(JObject(JField("o", JString("OG")) :: Nil)) must_== g
            }
            "case class" in {
              grater[Niklas].fromJSON(JObject(
                JField("g", JObject(JField("o", JString("OG")) :: Nil)) :: Nil)) must_== n
            }
          }
          "None" in {
            grater[Gustav].fromJSON(JObject(Nil)) must_== Gustav(o = None)
          }
        }
        "class hierarchies" in {
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
      }
      "strings" in {
        "a string that can be parsed to JSON object" in {
          val adam = """{"a":"string","b":99,"c":3.14,"d":false,"e":"2011-12-28T14:37:56.008Z","u":"http://www.typesafe.com","o":{"$oid":"4fd0bead4ceab231e6f3220b"}}"""
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
          grater[Adam].fromJSON(invalid) must throwA[ParseException]
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
    "handle Enums" in {
      val b = Blather("Greg", Map("a" -> Scope.TWO), Scope.TWO)
      val js = grater[Blather].toCompactJSON(b)
      val b2 = grater[Blather].fromJSON(js)
      val z = b2.scope.get("a")
      z must_== Some(Scope.TWO)
      val y = z.flatMap(b => Some(true)) // This goes boom w/ClassCastException before fix
      y must_== Some(true) // ensure the above didn't blow up
    }
  }

}
