package com.novus.salat.transformers

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
    case "scala.math.BigInteger" => true
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
}