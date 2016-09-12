/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         JsonMapSupport.scala
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

package salat.test.json

import salat.json.MapToJSON
import salat.test.SalatSpec

class JsonMapSupport extends SalatSpec {

  val uglyMap = Map(
    "text" -> "root", "root" -> true, "depth" -> 0, "children" -> List(
      Map(
        "children" -> List(
          Map("text" -> "Orange", "leaf" -> true, "depth" -> 2, "checked" -> true)
        ),
        "checked" -> true, "expanded" -> true, "text" -> "Orange", "depth" -> 1
      ),
      Map(
        "children" -> List(
          Map("text" -> "Acerola", "leaf" -> true, "depth" -> 2, "checked" -> true)
        ),
        "checked" -> true, "expanded" -> true, "text" -> "Acerola", "depth" -> 1
      ),
      Map(
        "children" -> List(
          Map("text" -> "Apple", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Strawberry", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Guava", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Sapote", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Mango", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Limequat", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Langsat", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Papaya", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Cherimoya", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Citron", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Apricot", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Feijoa", "leaf" -> true, "depth" -> 2, "checked" -> true)
        ),
        "checked" -> true, "expanded" -> true, "text" -> "Calamondin", "depth" -> 1
      ),
      Map(
        "children" -> List(
          Map("text" -> "Pear", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Pumpkin", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Pineapple", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Blueberry", "leaf" -> true, "depth" -> 2, "checked" -> true),
          Map("text" -> "Nance", "leaf" -> true, "depth" -> 2, "checked" -> true)
        ),
        "checked" -> true, "expanded" -> true, "text" -> "Persimmons", "depth" -> 1
      )
    )
  )

  val expected = """{"text":"root","root":true,"depth":0,"children":[{"children":[{"text":"Orange","leaf":true,"depth":2,"checked":true}],"checked":true,"expanded":true,"text":"Orange","depth":1},{"children":[{"text":"Acerola","leaf":true,"depth":2,"checked":true}],"checked":true,"expanded":true,"text":"Acerola","depth":1},{"children":[{"text":"Apple","leaf":true,"depth":2,"checked":true},{"text":"Strawberry","leaf":true,"depth":2,"checked":true},{"text":"Guava","leaf":true,"depth":2,"checked":true},{"text":"Sapote","leaf":true,"depth":2,"checked":true},{"text":"Mango","leaf":true,"depth":2,"checked":true},{"text":"Limequat","leaf":true,"depth":2,"checked":true},{"text":"Langsat","leaf":true,"depth":2,"checked":true},{"text":"Papaya","leaf":true,"depth":2,"checked":true},{"text":"Cherimoya","leaf":true,"depth":2,"checked":true},{"text":"Citron","leaf":true,"depth":2,"checked":true},{"text":"Apricot","leaf":true,"depth":2,"checked":true},{"text":"Feijoa","leaf":true,"depth":2,"checked":true}],"checked":true,"expanded":true,"text":"Calamondin","depth":1},{"children":[{"text":"Pear","leaf":true,"depth":2,"checked":true},{"text":"Pumpkin","leaf":true,"depth":2,"checked":true},{"text":"Pineapple","leaf":true,"depth":2,"checked":true},{"text":"Blueberry","leaf":true,"depth":2,"checked":true},{"text":"Nance","leaf":true,"depth":2,"checked":true}],"checked":true,"expanded":true,"text":"Persimmons","depth":1}]}"""

  "JSON map support" should {
    "turn a map with simple values into a JSON document" in {
      val simpleMap = Map("a" -> 1, "b" -> 2.34, "c" -> testDate)
      MapToJSON(simpleMap) must_== """{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z"}"""
    }
    "turn a map whose value is a list of simple values into a JSON document" in {
      val m = Map("a" -> 1, "b" -> 2.34, "c" -> testDate, "d" -> List(1, "x", 3.14, testDate))
      MapToJSON(m) must_== """{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z","d":[1,"x",3.14,"2011-12-28T14:37:56.008Z"]}"""
    }
    "turn a map whose value is a list of maps into a JSON document" in {
      val mm = Map("a" -> 1, "b" -> 2.34, "c" -> testDate)
      val m = Map("a" -> 1, "b" -> 2.34, "c" -> testDate, "d" -> List(mm, mm, mm))
      MapToJSON(m) must_==
        """{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z","d":[{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z"},{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z"},{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z"}]}"""
    }
    "turn a list of maps into a JSON array" in {
      val m = Map("a" -> 1, "b" -> 2.34, "c" -> testDate)
      val l = List(m, m, Map("a" -> 1, "b" -> 2.34, "c" -> testDate, "d" -> List(1, "x", 3.14, testDate)))
      MapToJSON(l) must_==
        """[{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z"},{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z"},{"a":1,"b":2.34,"c":"2011-12-28T14:37:56.008Z","d":[1,"x",3.14,"2011-12-28T14:37:56.008Z"]}]"""
    }
    "deal with ugly nested maps" in {
      MapToJSON(uglyMap) must_== expected
    }
  }
}
