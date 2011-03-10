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
package com.novus.salat.test

import com.mongodb.casbah.commons.Logging
import org.specs2.execute.PendingUntilFixed
import com.mongodb.casbah.Imports._
import org.specs2.mutable._

trait SalatSpec extends Specification with PendingUntilFixed with Logging {

  val SalatSpecDb = "test_salat"

  com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
  com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

  // TODO: would be after, but after as we know it has gone away...  damn it, I miss you after
  MongoConnection().dropDatabase(SalatSpecDb)

}