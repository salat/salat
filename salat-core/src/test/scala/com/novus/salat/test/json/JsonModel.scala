/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         JsonModel.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
package com.novus.salat.test.json

import org.joda.time.DateTime
import org.bson.types.ObjectId

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
                o: ObjectId)

case class Bertil(ints: List[Int], strings: List[String])
case class Caesar(l: List[Bertil])
