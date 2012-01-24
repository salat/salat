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
package com.novus.salat.test.json

import com.novus.salat._
import com.novus.salat.util._
import org.specs2.mutable.Specification
import net.liftweb.json._
import scala.util.parsing.json.{ JSONObject, JSONArray }
import org.bson.types.ObjectId

class JsonSpec extends Specification with Logging {

  "JSON support" should {
    "handle simple types" in {
      val o = new ObjectId
      val a = Adam(a = "string", b = 99, c = 3.14, d = false, e = testDate, u = testURL, o = o)
      val rendered = grater[Adam].toPrettyJSON(a)
//      log.debug(rendered)

      rendered must /("a" -> "string")
      rendered must /("b" -> 99)
      rendered must /("c" -> 3.14)
      rendered must /("d" -> false)
      rendered must /("e" -> TestDateFormatter.print(testDate))
      rendered must /("u" -> testURL.toString)
      rendered must /("o") / ("$oid" -> o.toString)
    }
    "handle lists" in {
      "lists with simple types" in {
        val ints = List(1, 2, 3)
        val strings = List("a", "b", "c")
        val b = Bertil(ints = ints, strings = strings)
        val rendered = grater[Bertil].toPrettyJSON(b)
        //        log.debug(rendered)
        //        09:47:43.440 [specs2.DefaultExecutionStrategy4] DEBUG c.novus.salat.test.json.JsonSpec - {
        //          "ints":[1,2,3],
        //          "strings":["a","b","c"]
        //        }

        rendered must /("ints" -> JSONArray(ints))
        rendered must /("strings" -> JSONArray(strings))
      }
      "handle lists of case classes" in {
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
  }

}
