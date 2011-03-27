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
package com.novus.salat

import com.mongodb.casbah.commons.Logging
import org.scala_tools.time.Imports._

package object transformers extends Logging {

  /**
   * Thanks to Predef....  the path name is not always the same.  @jsuereth is laughing somewhere...
   */
  def isBigDecimal(path: String) = path match {
    case path if path == "scala.math.BigDecimal" => true
    case path if path == "scala.package.BigDecimal" => true
    case path if path == "scala.Predef.BigDecimal" => true
    case path if path == "scala.BigDecimal" => true
    case _ => false
  }

  def isChar(path: String) = path match {
    case path if path == "scala.Char" => true
    case path if path == "java.lang.Character" => true
    case _ => false
  }

  def isBigInt(path: String) = path match {
    case path if path == "scala.package.BigInt" => true
    case path if path == "scala.math.BigInteger" => true
    case path if path == "java.math.BigInteger" => true
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
}