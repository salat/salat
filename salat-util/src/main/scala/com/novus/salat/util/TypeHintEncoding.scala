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

protected[salat] object Digits {
  // TODO: couldn't find a less cumbersome way to do this - curious!
  val ZeroToNine = Range(0, 10).toList.map(_.toString.charAt(0))
}

protected[salat] object LetterFrequency {
  // http://letterfrequency.org/
  val English = {
    val mostCommon = "etaoinsrhldcumfpgwybvkxjqz".toArray
    (mostCommon ++ mostCommon.map(_.toUpper)).toList
  }
}

object TypeHintEncoding {

  /**
   * JLS 3.8 (http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.8)
   *
   * Every character that is legally part of a Java identifier at any point, plus the . separator.
   *
   * Unless you name your files in Unicode, probably not necessary!
   */
  val FullJavaLangSpec = TypeHintEncoding({

    // scala> chars.filter(c => "%s".format(c.toChar).trim == "")
    // res14: List[Int] = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)
    //
    // scala> chars.filter(c => "%s".format(c.toChar).trim == "").map(Character.getType).distinct
    // res15: List[Int] = List(15)

    '.' :: {
      Range(Character.MIN_VALUE, Character.MAX_VALUE + 1)
        .toList
        // TODO: why is the ASCII bell a legal Java identifier?  ding dong.
        .filter(c => Character.isJavaIdentifierPart(c) &&
        Character.getType(c) != Character.CONTROL)
        .map(_.toChar)
    }
  })

  /**
   * Representing the smallest set of most likely class names: US ASCII, dot, dollar, underscore, and 0-9
   */
  val UsAsciiClassNames = TypeHintEncoding({
    LetterFrequency.English :::
      '.' ::
      '$' ::
      Digits.ZeroToNine :::
      '_' ::
      Nil
  })

}

case class TypeHintEncoding(chars: List[Char]) extends Logging {
  require(chars.distinct.size == chars.size, "no duplicate chars allowed")

  private val Zero = BigInt(0)

  lazy val base = BigInt(c2n.size)
  lazy val c2n = chars.zipWithIndex.map {
    case (c, i) => c -> BigInt(i)
  }.toMap
  lazy val n2c = c2n.map(_.swap).toMap

  def encode(s: String): BigInt = {
//    log.info("\n\n\nencode: BEGIN '%s'", s)
    val encoded = s.zipWithIndex.map {
      case (c, i) => {
        val num = c2n(c) * base.pow(i)
//        log.info("encode[%d]: '%s' ---> '%s'", i, c, num)
        num
      }
    }.sum
//    log.info("encode: END '%s' ---> %s \n\n\n", s, encoded)
    encoded
  }


  def decode(n: BigInt): List[BigInt] = {
    if (n == Zero) Nil else n.mod(base) :: decode(n / base)
  }

  def format(cs: List[BigInt]) = {
    //  cs.map(n2c.apply).mkString("")
    val sb = new StringBuilder
    val iter = cs.iterator
    var counter = 0
    while (iter.hasNext) {
      val num = iter.next()
      val char = n2c.apply(num)
//      log.info("format[%d]: '%s' ---> '%s'", counter, num, char)
      sb += char
      counter += 1
    }
    sb.result()
  }
}