/** Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  For questions and comments about this product, please see the project page at:
 *
 *  http://github.com/novus/salat
 *
 */
package com.novus.salat.transform

import org.specs2.mutable.Specification
import com.novus.salat.util.Logging
import com.novus.salat._
import com.novus.salat.global._
import scala.tools.scalap.scalax.rules.scalasig._
import scala.math.{ BigDecimal => SBigDecimal, BigInt => SBigInt }
import com.mongodb.casbah.commons.Imports._
import com.mongodb.DBObject

case class SimpleCaseClass(a: Option[String], b: Int)
case class CaseClassXform(x: SimpleCaseClass)
case class OptCaseClassXform(x: Option[SimpleCaseClass])

case class StringXform(x: String)
case class OptStringXform(x: Option[String])
case class DateXform(x: java.util.Date)
case class SBigDecimalXform(x: SBigDecimal)
case class OptSBigDecimalXform(x: Option[SBigDecimal])
case class SBigIntXform(x: SBigInt)
case class OptSBigIntXform(x: Option[SBigInt])

object Helpers {

  // ugly hack - see http://groups.google.com/group/scala-user/browse_thread/thread/ceb88872b12e5b1a
  def extractTypeRefType[X <: CaseClass: Manifest](x: Class[X]): TypeRefType = {
    grater[X].asInstanceOf[ConcreteGrater[X]].indexedFields.head.typeRefType
  }

  val DummyPath = "shrug"
  val TestStringValue = "hi"
  val TestBigDecimalValue = SBigDecimal("3.14", ctx.bigDecimalStrategy.mathCtx)
  val TestBigIntValue = SBigInt(3)
  val TestSimpleCaseClass = SimpleCaseClass(a = Some("whatever"), b = 42)
  val TestSimpleCaseClassMdbo = MongoDBObject("a" -> "whatever", "b" -> 42)
  val TestSimpleCaseClassDbo: DBObject = MongoDBObject("a" -> "whatever", "b" -> 42).asDBObject
}

class DBObjectInjectorChainTest extends Specification with Logging {

  import Helpers._

  "DBObject injector chain" should {

    "offer a straight-through transformation that does not alter type" in {
      "for java.util.Date" in {
        // java.util.Date is serialized down in the BSON layer, so Salat transformer should leave it alone
        val input: Any = new java.util.Date()
        val expectedOutput: Any = input
        val trt = extractTypeRefType(classOf[DateXform])
        val actualOutput = DBObjectInjectorChain.straightThrough(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput // TODO: actualOutput must be the same instance as input
      }
      "for java.lang.String" in {
        // java.util.Date is serialized down in the BSON layer, so Salat transformer should leave it alone
        val input: Any = TestStringValue
        val expectedOutput: Any = input
        val trt = extractTypeRefType(classOf[StringXform])
        val actualOutput = DBObjectInjectorChain.straightThrough(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput // TODO: actualOutput must be the same instance as input
      }
    }

    "transform BigDecimal" in {
      val trt = extractTypeRefType(classOf[SBigDecimalXform])
      "from a Double" in {
        val input: Any = 3.14d
        val expectedOutput: Any = TestBigDecimalValue
        val actualOutput = DBObjectInjectorChain.bigDecimalTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a Long" in {
        val input: Any = 3L
        val expectedOutput: Any = SBigDecimal(3L, ctx.bigDecimalStrategy.mathCtx)
        val actualOutput = DBObjectInjectorChain.bigDecimalTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from an Int" in {
        val input: Any = 3
        val expectedOutput: Any = SBigDecimal(3, ctx.bigDecimalStrategy.mathCtx)
        val actualOutput = DBObjectInjectorChain.bigDecimalTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a Float" in {
        val input: Any = 3.14f
        val expectedOutput: Any = TestBigDecimalValue
        val actualOutput = DBObjectInjectorChain.bigDecimalTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a Short" in {
        val input: Any = 3.shortValue()
        val expectedOutput: Any = SBigDecimal(3, ctx.bigDecimalStrategy.mathCtx)
        val actualOutput = DBObjectInjectorChain.bigDecimalTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a BigDecimal (how this comes IN as a big decimal I still have no idea!)" in {
        val input: Any = TestBigDecimalValue
        val expectedOutput: Any = TestBigDecimalValue
        val actualOutput = DBObjectInjectorChain.bigDecimalTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
    }

    "transform BigInt" in {
      val trt = extractTypeRefType(classOf[SBigIntXform])
      "from a String" in {
        val input: Any = "3"
        val expectedOutput: Any = TestBigIntValue
        val actualOutput = DBObjectInjectorChain.bigIntTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a byte array" in {
        val input: Any = TestBigIntValue.toByteArray
        val expectedOutput: Any = TestBigIntValue
        val actualOutput = DBObjectInjectorChain.bigIntTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a scala.BigInt" in {
        val input: Any = TestBigIntValue
        val expectedOutput: Any = TestBigIntValue
        val actualOutput = DBObjectInjectorChain.bigIntTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a java.math.BigInteger" in {
        val input: Any = TestBigIntValue.bigInteger
        val expectedOutput: Any = TestBigIntValue
        val actualOutput = DBObjectInjectorChain.bigIntTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from a Long" in {
        val input: Any = 3L
        val expectedOutput: Any = TestBigIntValue
        val actualOutput = DBObjectInjectorChain.bigIntTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from an Int" in {
        val input: Any = 3
        val expectedOutput: Any = TestBigIntValue
        val actualOutput = DBObjectInjectorChain.bigIntTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
    }

    "transform case classes" in {
      val trt = extractTypeRefType(classOf[CaseClassXform])
      val path = "com.novus.salat.transform.SimpleCaseClass"
      "from DBObject" in {
        val input: Any = TestSimpleCaseClassDbo
        val expectedOutput: Any = TestSimpleCaseClass
        val actualOutput = DBObjectInjectorChain.caseClassTransformer(path, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "from MongoDBObject" in {
        val input: Any = TestSimpleCaseClassMdbo
        val expectedOutput: Any = TestSimpleCaseClass
        val actualOutput = DBObjectInjectorChain.caseClassTransformer(path, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
    }

    "transform Options" in {
      "String to Option[String]" in {
        val input: Any = TestStringValue
        val expectedOutput: Any = Some(TestStringValue)
        val trt = extractTypeRefType(classOf[OptStringXform])
        val actualOutput = DBObjectInjectorChain.optionTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "isBigDecimal type to Option[BigDecimal]" in {
        val input: Any = 3.14d
        val expectedOutput: Any = Some(TestBigDecimalValue)
        val trt = extractTypeRefType(classOf[OptSBigDecimalXform])
        val actualOutput = DBObjectInjectorChain.optionTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "isBigInt type to Option[BigInt]" in {
        val input: Any = 3L
        val expectedOutput: Any = Some(TestBigIntValue)
        val trt = extractTypeRefType(classOf[OptSBigIntXform])
        val actualOutput = DBObjectInjectorChain.optionTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
      "DBO to Option[A<:CaseClass]" in {
        val input: Any = TestSimpleCaseClassDbo
        val expectedOutput: Any = Some(TestSimpleCaseClass)
        val trt = extractTypeRefType(classOf[OptCaseClassXform])
        val actualOutput = DBObjectInjectorChain.optionTransformer(DummyPath, trt, ctx, input)
        actualOutput must_== expectedOutput
      }
    }

  }

}