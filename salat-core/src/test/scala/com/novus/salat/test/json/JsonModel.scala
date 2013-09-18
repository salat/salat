/*
 * Copyright (c) 2010 - 2013 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         JsonModel.scala
 * Last modified: 2013-02-25 18:56:50 EST
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
package com.novus.salat.test.json

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.novus.salat.annotations._

//  A	Adam
//  B	Bertil
//  C	Caesar (pronounced Cesar)
//  D	David
//  E	Erik
//  F	Filip
//  G	Gustav
//  H	Helge
//  I	Ivar
//  J	Johan
//  K	Kalle
//  L	Ludvig
//  M	Martin
//  N	Niklas
//  O	Olof
//  P	Petter
//  Q	Qvintus
//  R	Rudolf
//  S	Sigurd
//  T	Tore
//  U	Urban
//  V	Viktor
//  W	Wilhelm
//  X	Xerxes (Pron. Zer-seeze)
//  Y	Yngve
//  Z	Zäta
//  Å	Åke
//  Ä	Ärlig
//  Ö	Östen

case class Adam(a: String,
                b: Int,
                c: Double,
                d: Boolean,
                e: DateTime,
                u: java.net.URL,
                bd: BigDecimal,
                bi: BigInt,
                o: ObjectId)

case class Bertil(ints: List[Int], strings: List[String])
case class Caesar(l: List[Bertil])
case class David(m: Map[String, Int])
case class Erik(e: String)
case class Filip(m: Map[String, Erik])
case class Gustav(o: Option[String] = None)
@Salat
trait Helge {
  val s: String
}
case class Ivar(s: String) extends Helge
case class Johan(s: String, d: Double) extends Helge
@Salat
abstract class Kalle(s: String)
case class Ludvig(s: String) extends Kalle(s)
case class Martin(s: String, d: Double) extends Kalle(s)
case class Niklas(g: Option[Gustav])
case class Olof(d: DateTime)
case class Petter(d: Option[DateTime])
case class Qvintus(bd: Option[BigDecimal])
case class Rudolf(bi: Option[BigInt])
case class Sigurd(o: Option[ObjectId])
case class Tore(i: Int, d: Some(Double))
