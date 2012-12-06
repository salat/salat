/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         GraterSpec.scala
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
import com.novus.salat.test.model.useful._
import com.novus.salat._
import com.novus.salat.test.global._
import com.mongodb.casbah.Imports._

class GraterSpec extends SalatSpec {

  "Grater" should {
    "support case class <-> Map[String, _]" in {
      val _id = new ObjectId
      val a = TestString
      val b = KaprekarsConstant
      val c = scala.math.Pi
      val d = BigDecimal(GoldenRatio)
      val e = BigInt(KaprekarsConstant)
      val f = true
      val g = TestDate
      val h = TestChar
      val i = TestTimeZone

      "case class <-> map" in {
        val aural = Aural(_id = _id, a = a, b = b, c = c, d = d, e = e, f = f, g = g, h = h, i = i)
        val map = grater[Aural].toMap(aural)
        map must havePair(ctx.typeHintStrategy.typeHint, "com.novus.salat.test.model.Aural")
        map must havePair("_id", _id)
        map must havePair("a", a)
        map must havePair("b", b)
        map must havePair("c", c)
        map must havePair("d", d)
        map must havePair("e", e)
        map must havePair("f", f)
        map must havePair("g", g)
        map must havePair("h", h)
        map must havePair("i", i)
        val aural_* = grater[Aural].fromMap(map)
        aural_* must_== aural
      }
      "concrete trait impl <-> map" in {
        val ctenoid = Ctenoid(a = a, b = b, c = c)
        val map = grater[Bdellatomy].toMap(ctenoid)
        map must havePair(ctx.typeHintStrategy.typeHint, "com.novus.salat.test.model.Ctenoid")
        map must havePair("a", a)
        map must havePair("b", b)
        map must havePair("c", c)
        val ctenoid_* = grater[Bdellatomy].fromMap(map)
        ctenoid_* must_== ctenoid
      }

      "handle default args when serializing case class -> map" in {
        val defaultArgA = TestString
        val defaultArgB = KaprekarsConstant

        val djinn = Djinn(_id = _id, c = GoldenRatio)
        djinn.a must_== defaultArgA
        djinn.b must_== defaultArgB

        val map = grater[Djinn].toMap(djinn)
        map must havePair(ctx.typeHintStrategy.typeHint, "com.novus.salat.test.model.Djinn")
        map must havePair("_id", _id)
        map must havePair("a", defaultArgA)
        map must havePair("b", defaultArgB)
        map must havePair("c", GoldenRatio)
        val djinn_* = grater[Djinn].fromMap(map)
        djinn_* must_== djinn
      }

      "handle default args when serializing map -> case class" in {
        val map = Map("_id" -> _id, "b" -> 99, "c" -> GoldenRatio)
        val djinn_* = grater[Djinn].fromMap(map)
        djinn_*.a must_== TestString
        djinn_* must_== Djinn(_id = _id, b = 99, c = GoldenRatio)
      }

      "respect @Key when serializing case class -> map" in {
        val ewe = Ewe(fat = true)
        val map = grater[Ewe].toMap(ewe)
        map must havePair(ctx.typeHintStrategy.typeHint, "com.novus.salat.test.model.Ewe")
        map must havePair("fluffy", true)
        val ewe_* = grater[Ewe].fromMap(map)
        ewe_* must_== ewe
      }

      "respect @Key when serializing case class <- map" in {
        grater[Ewe].fromMap(Map("fluffy" -> false)) must_== Ewe(fat = false)
      }

      "respect @Ignore when serializing case class -> map" in {
        val fantasm = Fantasm(_id = _id, which = "spectre", rationalExplanation = Some("just a passing breeze"))
        val map = grater[Fantasm].toMap(fantasm)
        map must havePair(ctx.typeHintStrategy.typeHint, "com.novus.salat.test.model.Fantasm")
        map must havePair("_id", _id)
        map must havePair("which", "spectre")
        map must haveKey("rationalExplanation").not // TODO: for some reason, not haveKey is unhappy here
        val fantasm_* = grater[Fantasm].fromMap(map)
        // because rationalExplanation is annotated with @Ignore, value in fantasm is not serialized to map and doesn't
        // appear in the deserialized fantasm_* object
        fantasm_* must_== fantasm.copy(rationalExplanation = None)
      }

      "respect @Ignore when serializing case class <- map" in {
        val map = Map("_id" -> _id, "which" -> "spectre", "rationalExplanation" -> Some("just a passing breeze"))
        val fantasm_* = grater[Fantasm].fromMap(map)
        // because rationalExplanation is annotated with @Ignore, value in map doesn't appear in the deserialized
        // fantasm_* object
        fantasm_* must_== Fantasm(_id = _id, which = "spectre", rationalExplanation = None)
      }

      "respect @Persist when serializing case class -> map" in {
        val gneiss = Gneiss(igneous = true)
        gneiss.classification must_== "orthogneiss"
        val map = grater[Gneiss].toMap(gneiss)
        map must havePair(ctx.typeHintStrategy.typeHint, "com.novus.salat.test.model.Gneiss")
        map must havePair("igneous", true)
        map must havePair("classification", "orthogneiss")
        val gneiss_* = grater[Gneiss].fromMap(map)
        gneiss_* must_== gneiss
      }

      "respect @Persist when serializing case class <- map" in {
        val map = Map("igneous" -> true, "classification" -> "orthogneiss")
        val gneiss_* = grater[Gneiss].fromMap(map)
        gneiss_* must_== Gneiss(true)
      }
    }
  }

}
