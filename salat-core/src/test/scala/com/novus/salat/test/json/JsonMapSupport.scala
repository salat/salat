package com.novus.salat.test.json

import com.novus.salat.json.MapToJSON
import com.novus.salat.test.SalatSpec

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
