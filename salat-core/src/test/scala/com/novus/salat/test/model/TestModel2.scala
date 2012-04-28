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