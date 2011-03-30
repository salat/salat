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
 *
 */
package com.novus.salat.test.dao

import com.novus.salat._
import com.novus.salat.test._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._


@Salat
trait Beta {
   val y: String
}
case class Gamma(y: String) extends Beta
case class Delta(y: String, z: String) extends Beta
case class Alpha(@Key("_id") id: Int, beta: List[Beta] = Nil)


//
// Instant Salat DAO in under two minutes:
// 1.  extend com.novus.salat.dao.SalatDAO
// 2.  define a grater for the class
// 3.  specify a collection
//
object AlphaDAO extends com.novus.salat.dao.SalatDAO[Alpha] {

  val _grater = grater[Alpha]

  val collection = MongoConnection()(SalatSpecDb)(DaoSpecColl)

}