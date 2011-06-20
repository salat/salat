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

import com.novus.salat.util.Logging
import org.specs2.execute.PendingUntilFixed
import com.mongodb.casbah.Imports._
import org.specs2.mutable._
import org.specs2.specification.Step

trait SalatSpec extends Specification with Logging {

  override def is =
    Step {
//      log.info("beforeSpec: registering BSON conversion helpers")
      com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers()
      com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

    } ^
      super.is ^
      Step {
//        log.info("afterSpec: dropping test MongoDB '%s'".format(SalatSpecDb))
        MongoConnection().dropDatabase(SalatSpecDb)
      }

}