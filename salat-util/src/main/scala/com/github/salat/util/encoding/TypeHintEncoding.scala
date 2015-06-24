/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-util
 * Class:         TypeHintEncoding.scala
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
package com.github.salat.util.encoding

import com.github.salat.util.Logging

/**
 */
protected[salat] object Digits {
  // TODO: couldn't find a less cumbersome way to do this - curious!
  val ZeroToNine = Range(0, 10).toList.map(_.toString.charAt(0))
}

protected[salat] object LetterFrequency {
  // http://en.wikipedia.org/wiki/Letter_frequency
  val English = {
    val mostCommon = "etaoinsrhldcumfpgwybvkxjqz".toArray
    (mostCommon ++ mostCommon.map(_.toUpper)).toList
  }
}

protected[salat] object CharSets {

  // scala> chars.filter(c => "%s".format(c.toChar).trim == "")
  // res14: List[Int] = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)
  //
  // scala> chars.filter(c => "%s".format(c.toChar).trim == "").map(Character.getType).distinct
  // res15: List[Int] = List(15)

  // ACHTUNG! dot must be first because the encoding will be 0, and an ending dot (which is not legal) would be truncated
  // TODO: not optimized even a little tiny bit, nope, nope - should be labelled with unicode skull & crossbones
  lazy val FullJLS = '.' :: {
    Range(Character.MIN_VALUE, Character.MAX_VALUE + 1)
      .toList
      // TODO: why is the ASCII bell a legal Java identifier?  ding dong.
      .filter(c => Character.isJavaIdentifierPart(c) &&
        Character.getType(c) != Character.CONTROL)
      .map(_.toChar)
  }

  val UsAscii = '.' :: // ACHTUNG! dot must be first because the encoding will be 0, and an ending dot would be truncated
    LetterFrequency.English :::
    '$' ::
    Digits.ZeroToNine :::
    '_' ::
    Nil
}

object TypeHintEncoding {

  /**
   * JLS 3.8 (http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.8)
   *
   *  Every character that is legally part of a Java identifier at any point, plus the . separator.
   *
   *  Unless you name your files in Unicode, probably not necessary!
   */
  lazy val FullJavaLangSpec = TypeHintEncoding(CharSets.FullJLS)

  /**
   * Representing the smallest set of most likely class names: US ASCII, dot, dollar, underscore, and 0-9
   */
  val UsAsciiEncoding = TypeHintEncoding(CharSets.UsAscii)

}

case class TypeHintEncoding(chars: List[Char]) extends Logging {
  require(chars.nonEmpty, "chars must not be empty")
  require(chars.distinct.size == chars.size, "no duplicate chars allowed")

  // TODO: memoize this!

  private val Zero = BigInt(0)

  lazy val base = BigInt(c2n.size)
  lazy val c2n = chars.zipWithIndex.map {
    case (c, i) => c -> BigInt(i)
  }.toMap
  lazy val n2c = c2n.map(_.swap).toMap

  def encode(s: String): BigInt = {
    //    val sb = new StringBuilder
    //    sb ++= "\n\n\nencode: BEGIN '%s'\n".format(s)
    val encoded = s.zipWithIndex.map {
      case (c, i) => {
        // TODO: better error here
        val num = c2n.get(c).getOrElse(
          throw new Error("Char '%s' is missing from input chars='%s'".format(c, chars.mkString("")))
        ) * base.pow(i)
        //        sb ++= "encode[%d]: '%s' ---> '%s'\n".format(i, c, num)
        num
      }
    }.sum
    //    sb ++= "END: '%s' ---> '%s'\n\n\n".format(s, encoded)
    //    log.info(sb.result())
    encoded
  }

  def decode(n: BigInt): List[BigInt] = {
    if (n == Zero) Nil else n.mod(base) :: decode(n / base)
  }

  def format(cs: List[BigInt]) = {
    //  cs.map(n2c.apply).mkString("")
    //    log.info("format: %s", cs)
    val sb = new StringBuilder
    val iter = cs.iterator
    var counter = 0
    while (iter.hasNext) {
      val num = iter.next()
      // TODO: better error message
      val char = n2c.get(num).getOrElse(sys.error("Num '%s' has no corresponding char in n2c".format(num)))
      //      log.info("format[%d]: '%s' ---> '%s'", counter, num, char)
      sb += char
      counter += 1
    }

    sb.result()
  }

  override def toString = "TypeHintEncoding: base=%s chars='%s'".format(base, chars.mkString(""))
}
