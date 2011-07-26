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
package com.novus.salat.transform

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Implicits._

object `package` {

  def isBigDecimal(path: String) = path match {
    case "scala.math.BigDecimal" => true
    case "scala.package.BigDecimal" => true
    case "scala.Predef.BigDecimal" => true
    case "scala.BigDecimal" => true
    case _ => false
  }

  def isFloat(path: String) = path match {
    case "scala.Float" => true
    case "java.lang.Float" => true
    case _ => false
  }

  def isChar(path: String) = path match {
    case "scala.Char" => true
    case "java.lang.Character" => true
    case _ => false
  }

  def isBigInt(path: String) = path match {
    case "scala.package.BigInt" => true
    case "scala.math.BigInt" => true
    case "java.math.BigInteger" => true
    case _ => false
  }

  def isJodaDateTime(path: String) = path match {
    case "org.joda.time.DateTime" => true
    case "org.scala_tools.time.TypeImports.DateTime" => true
    case _ => false
  }

  def isInt(path: String) = path match {
    case "java.lang.Integer" => true
    case "scala.Int" => true
    case _ => false
  }

  object IsDbo {
    def unapply(x: Any): Option[MongoDBObject] = x match {
      case dbo: MongoDBObject => Some(dbo)
      case dbo: DBObject => {
        val m: MongoDBObject = dbo    // TODO: this is really stupid, I must be missing an implict somewhere
        Some(m)
      }
      case _ => None
    }
  }
}