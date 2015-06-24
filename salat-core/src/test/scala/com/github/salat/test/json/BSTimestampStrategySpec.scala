/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         BSTimestampStrategySpec.scala
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

package com.github.salat.test.json

import com.github.salat.json.StrictBSONTimestampStrategy
import com.github.salat.util.Logging
import org.bson.types.BSONTimestamp
import org.json4s.JsonAST._
import org.specs2.mutable.Specification

class BSONTimestampStrategySpec extends Specification with Logging {

  val ts = new BSONTimestamp(1345193830, 10)
  val formatted = "{\"$ts\" : 1345193830 , \"$inc\" : 10}"

  "JSON BSONTimestamp strategy " should {
    "Strict JSON" in {
      val s = StrictBSONTimestampStrategy
      val j = JObject(JField("$ts", JInt(1345193830)) :: (JField("$inc", JInt(10))) :: Nil)

      "from BSONTimestamp to JSON" in {
        s.out(ts) must_== j
      }
      "from JSON to BSONTimestamp" in {
        s.in(j) must_== ts
      }
      "throw an error when an unexpected JSON field type is submitted" in {
        s.in(j \ "$ts") must throwA[RuntimeException]
      }
    }
  }
}
