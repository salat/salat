package com.novus.salat.transform_scratch

import com.novus.salat.{IsTraitLike, IsEnum, Context}
import scala.tools.scalap.scalax.rules.scalasig.{SingleType, TypeRefType}

object `package` {
  
  val BigDecimalType = "BigDecimalType"
  val BigIntType = "BigIntType"
  val IntType = "BigDecimalType"
  val FloatType = "FloatType"
  val CharType = "CharType"
  val DateTimeType = "DateTimeType"
  val EnumType = "EnumType"
  val CaseClassType = "CaseClassType"
  val StraightThroughType = "CaseClassType"
  
}

//object IsIdentifiableType {
//
//  def unapply(t: TypeRefType, ctx: Context) = t.symbol.path match {
//    case IdentifiableBigDecimal(t) => t
//    case IdentifiableBigInt(t) => t
//    case IdentifiableInt(t) => t
//    case IdentifiableDateTime(t) => t
//    case IdentifiableFloat(t) => t
//    case IdentifiableChar(t) => t
//    case IsEnum(t) => EnumType
//    case IsTraitLike(t) => CaseClassType
//    case _ => StraightThroughType
//  }
//}

object IdentifiableBigDecimal {
  def unapply(t: TypeRefType) = t.symbol.path match {
    case "scala.math.BigDecimal" => Some(BigDecimalType)
    case "scala.package.BigDecimal" => Some(BigDecimalType)
    case "scala.Predef.BigDecimal" => Some(BigDecimalType)
    case "scala.BigDecimal" => Some(BigDecimalType)
    case _ => None
  }
}

object IdentifiableFloat {
  def unapply(t: TypeRefType) = t.symbol.path match {
    case "scala.Float" => Some(FloatType)
    case "java.lang.Float" => Some(FloatType)
    case _ => None
  }
}

object IdentifiableChar {
  def unapply(t: TypeRefType) = t.symbol.path match {
    case "scala.Char" => Some(CharType)
    case "java.lang.Character" => Some(CharType)
    case _ => None
  }
}

object IdentifiableBigInt {
  def unapply(t: TypeRefType) = t.symbol.path match {
    case "scala.package.BigInt" => Some(BigIntType)
    case "scala.math.BigInteger" => Some(BigIntType)
    case "java.math.BigInteger" => Some(BigIntType)
    case _ => None
  }
}

object IdentifiableDateTime {
  def unapply(t: TypeRefType) = t.symbol.path match {
    case "org.joda.time.DateTime" => Some(DateTimeType)
    case "org.scala_tools.time.TypeImports.DateTime" => Some(DateTimeType)
    case _ => None
  }
}

object IdentifiableInt {
  def unapply(t: TypeRefType) = t.symbol.path match {
    case "java.lang.Integer" => Some(IntType)
    case "scala.Int" => Some(IntType)
    case _ => None
  }
}