/*
 * Copyright (c) 2010 - 2015 Novus Partners, Inc. (http://www.novus.com)
 * Copyright (c) 2015 - 2016 Rose Toomey (https://github.com/rktoomey) and other individual contributors where noted
 *
 * Module:        salat-core
 * Class:         MongoJavaDriver252IntLongBugSpec.scala
 * Last modified: 2016-07-10 23:49:08 EDT
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
 *           Project:  http://github.com/salat/salat
 *              Wiki:  http://github.com/salat/salat/wiki
 *             Slack:  https://scala-salat.slack.com
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 *
 */
package com.novus.salat.test

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON.parse
import com.novus.salat._
import com.novus.salat.test.global._
import com.novus.salat.test.model._

class MongoJavaDriver252IntLongBugSpec extends SalatSpec {
  "mongo-java-driver 2.5.2" should {
    "not be confused about the difference between Int and Long when parsing from stringified JSON" in {

      // BACKGROUND: mongo-java-driver used to read bare numbers as Int but 2.5.x treats them as Long
      // See https://github.com/novus/salat/issues/7

      //[error]   argument type mismatch
      //[error]
      //[error]   Grater(class com.novus.salat.test.model.Company @ com.novus.salat.global.package$$anon$1@222380e2) toObject failed on:
      //[error]   SYM: com.novus.salat.test.model.Company
      //[error]   CONSTRUCTOR: public com.novus.salat.test.model.Company(java.lang.String,int,scala.collection.immutable.Map)
      //[error]   ARGS:
      //[error]   [0]	class java.lang.String
      //[error] 		Novus
      //[error] [1]	class java.lang.Long
      //[error] 		2007
      //[error] [2]	class scala.collection.immutable.Map$EmptyMap$
      //[error] 		Map()

      val c = HasCompany(Company(name = "Novus", year_of_inception = 2007, departments = Map.empty))
      val dbo: DBObject = grater[HasCompany].asDBObject(c)
      val c_* = grater[HasCompany].asObject(parse(dbo.toString).asInstanceOf[DBObject])
      c_* must_== c
    }
  }
}
