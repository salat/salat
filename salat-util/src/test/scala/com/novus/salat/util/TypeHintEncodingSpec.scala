/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
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
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 */
package com.novus.salat.util

import org.specs2.mutable.Specification
import com.novus.salat.util.model._

class TypeHintEncodingSpec extends Specification with Logging {

  val hintEncoding = TypeHintEncoding.UsAsciiClassNames


//  "instantiate with a list of chars" in {
//    "a simple test succeeds" in {
//
//    }.pendingUntilFixed
//    "blow up if there are duplicate chars" in {
//
//    }.pendingUntilFixed
//  }

  "encode and decode to BigInt" in {

    "a simple class name consisting of a-zA-z and dots" in {
      val clazzName = classOf[OneConstructorWithArgs].getName
      log.info("clazzName: %s", clazzName)
      val encoded = hintEncoding.encode(clazzName)
      encoded.toByteArray.size must be lessThan clazzName.size
      val decoded = hintEncoding.decode(encoded)
      decoded.size must_== clazzName.length()
      hintEncoding.format(decoded) must_== clazzName
    }

    "a class name with an underscore" in {
      val clazzName = classOf[foo_bar.ClassInsidePackageWithUnderscore].getName
      clazzName must contain("_")   // TODO: replace with regex
      log.info("clazzName: %s", clazzName)
      val encoded = hintEncoding.encode(clazzName)
      encoded.toByteArray.size must be lessThan clazzName.size
      val decoded = hintEncoding.decode(encoded)
      decoded.size must_== clazzName.length() // TODO: this assumption fails :( the terminal e is getting dropped on the floor
      hintEncoding.format(decoded) must_== clazzName
    }.pendingUntilFixed

    "a class name with a dollar sign" in {
      val clazzName = ConcreteCaseObject.getClass.getName
      clazzName must contain("$")   // TODO: replace with regex
      log.info("clazzName: %s", clazzName)
      val encoded = hintEncoding.encode(clazzName)
      encoded.toByteArray.size must be lessThan clazzName.size
      val decoded = hintEncoding.decode(encoded)
      decoded.size must_== clazzName.length()
      hintEncoding.format(decoded) must_== clazzName
    }

  }

}