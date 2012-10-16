/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         TestModel2.scala
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

package com.novus.salat.test.model

import org.bson.types.ObjectId
import com.novus.salat.annotations._
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants._

// a reboot of test model using the Nearly Anacrophonic Phonetic Alphabet

//aural
//bdellatomy
//ctenoid
//djinn
//ewe
//fantasm
//gneiss
//heir
//ingénue
//jipijapa
//knead
//llareta
//mneme
//ngoma
//oneing
//pteris
//qi
//rath
//segar
//Tlingit
//uakari
//voetganger
//wrest
//Xhosa
//yttric
//zwiebac

case class Aural(_id: ObjectId = new ObjectId,
                 a: String,
                 b: Int,
                 c: Double,
                 d: BigDecimal,
                 e: BigInt,
                 f: Boolean,
                 g: DateTime,
                 h: Char)

trait Bdellatomy {
  val a: String
  val b: Int
  val c: Double
}

case class Ctenoid(a: String,
                   b: Int,
                   c: Double) extends Bdellatomy

case class Djinn(_id: ObjectId = new ObjectId,
                 a: String = useful.TestString,
                 b: Int = useful.KaprekarsConstant,
                 c: Double = scala.math.E)

case class Ewe(@Key("fluffy") fat: Boolean)

case class Fantasm(_id: ObjectId = new ObjectId,
                   which: String,
                   @Ignore rationalExplanation: Option[String] = None)

case class Gneiss(igneous: Boolean) {
  @Persist val classification = if (igneous) "orthogneiss" else "paragneiss"
}