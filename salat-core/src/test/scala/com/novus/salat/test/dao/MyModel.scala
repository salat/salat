/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         MyModel.scala
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

package com.novus.salat.test.dao

import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import com.novus.salat.test._
import com.novus.salat.test.global._
import com.novus.salat.dao.{ SalatDAO, ModelCompanion }

object MyModel extends ModelCompanion[MyModel, ObjectId] {
  val collection = MongoConnection()(SalatSpecDb)(MyModelColl)
  val dao = new SalatDAO[MyModel, ObjectId](collection = collection) {}
}

case class MyModel(@Key("_id") id: ObjectId,
                   x: String,
                   y: Int,
                   z: List[Double],
                   d: DateTime)