/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.test.json

import org.joda.time.DateTime

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
                u: java.net.URL)

case class Bertil(ints: List[Int], strings: List[String])
case class Caesar(l: List[Bertil])
