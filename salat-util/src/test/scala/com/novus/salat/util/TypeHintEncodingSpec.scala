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

  "instantiate with a list of chars" in {
    val chars = CharSets.UsAscii
    val encoding = TypeHintEncoding(chars)
    "setting the base to the number of distinct chars" in {
      encoding.base must_== BigInt(chars.size)
    }
    "creating two lookup maps BigInt <-> Char" in {
      encoding.n2c.size must_== chars.size
      encoding.c2n.size must_== chars.size
      encoding.c2n.toList.map(_.swap).toMap must_== encoding.n2c
    }
    "blow up if the char list is empty" in {
      TypeHintEncoding(Nil) must throwAn[IllegalArgumentException]
    }
    "blow up if there are duplicate chars" in {
      TypeHintEncoding("aaa".toCharArray.toList) must throwAn[IllegalArgumentException]
    }
  }

  def testUsAsciiEncoding(clazzName: String) = {
//    log.debug("testUsAsciiEncoding: clazzName='%s'", clazzName)
    val encoded = TypeHintEncoding.UsAsciiEncoding.encode(clazzName)
    encoded.toByteArray.size must be lessThan clazzName.size
    val decoded = TypeHintEncoding.UsAsciiEncoding.decode(encoded)
    decoded.size must_== clazzName.length()
    TypeHintEncoding.UsAsciiEncoding.format(decoded) must_== clazzName
  }
  
  def testFullJLSEncoding(clazzName: String) = {
//    log.debug("testUsAsciiEncoding: clazzName='%s'", clazzName)
    val encoded = TypeHintEncoding.FullJavaLangSpec.encode(clazzName)
    // well, you got your unicode support here, but.... ugh.
    encoded.toByteArray.size must be lessThan 2 * clazzName.size
    val decoded = TypeHintEncoding.FullJavaLangSpec.decode(encoded)
    decoded.size must_== clazzName.length()
    TypeHintEncoding.FullJavaLangSpec.format(decoded) must_== clazzName
  }
  
  "encode and decode to BigInt" in {
    "a simple class name consisting of a-zA-z and dots" in {
      testUsAsciiEncoding(classOf[OneConstructorWithArgs].getName)
      testFullJLSEncoding(classOf[OneConstructorWithArgs].getName)
    }

    "a class name with an underscore" in {
      val clazzName = classOf[foo_bar.ClassInsidePackageWithUnderscore].getName
      clazzName must contain("_")
      testUsAsciiEncoding(clazzName)
      testFullJLSEncoding(clazzName)
    }

    "a class name with a dollar sign" in {
      val clazzName = ConcreteCaseObject.getClass.getName
      clazzName must contain("$")
      log.info("clazzName: %s", clazzName)
      testUsAsciiEncoding(clazzName)
      testFullJLSEncoding(clazzName)
    }

    "a class name with unicode characters" in {
      val clazzName = classOf[ἀρετή].getName
      "full JLS unicode encoding must succeed" in {
        testFullJLSEncoding(clazzName)
      }
      "US ASCII encoding must fail" in {
        TypeHintEncoding.UsAsciiEncoding.encode(clazzName) must throwAn[Error]
      }
    }

  }

}