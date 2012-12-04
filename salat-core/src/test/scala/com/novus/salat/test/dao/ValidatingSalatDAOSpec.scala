/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ValidatingSalatDAOSpec.scala
 * Last modified: 2012-12-04 17:17:43 EST
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
 *           Project:  http://github.com/novus/salat
 *              Wiki:  http://github.com/novus/salat/wiki
 *      Mailing list:  http://groups.google.com/group/scala-salat
 *     StackOverflow:  http://stackoverflow.com/questions/tagged/salat
 */

package com.novus.salat.test.dao

import com.novus.salat.test.SalatSpec
import org.bson.types.ObjectId
import com.novus.salat.dao.ValidationErrorChain

class ValidatingSalatDAOSpec extends SalatSpec {

  "Simple validating DAO" should {
    "insert a valid object" in {
      val _id = new ObjectId
      val valid = ToValidate(id = _id, a = 1, b = "Hello")
      ToValidate.validator.apply(valid) must beRight(valid)
      SimpleValidationDAO.insert(valid) must beSome(_id)
    }
    "throw an exception instead of inserting an invalid object" in {
      val _id = new ObjectId
      val invalid = ToValidate(id = _id, a = -1, b = "Hello")
      ToValidate.validator.apply(invalid) must beLeft(ToValidate.NegativeA(invalid))
      SimpleValidationDAO.insert(invalid) must throwA[ToValidate.NegativeA]
      SimpleValidationDAO.findOneById(_id) must beNone
      val invalid2 = ToValidate(id = _id, a = 1, b = "")
      ToValidate.validator.apply(invalid2) must beLeft(ToValidate.EmptyB(invalid2))
      SimpleValidationDAO.insert(invalid2) must throwA[ToValidate.EmptyB]
      SimpleValidationDAO.findOneById(_id) must beNone
    }
  }
  "Validating DAO using a chained validator" should {
    "insert a valid object" in {
      val _id = new ObjectId
      val valid = ToValidate(id = _id, a = 1, b = "Hello")
      ToValidateChain.validator.apply(valid) must beRight(valid)
      ChainedValidationDAO.insert(valid) must beSome(_id)
    }
    "throw a validation chain exception containing a single validation failure" in {
      val _id = new ObjectId
      val invalid = ToValidate(id = _id, a = -1, b = "Hello")
      ToValidateChain.validator.apply(invalid) must beLeft(ValidationErrorChain(invalid, ToValidate.NegativeA(invalid) :: Nil))
      ChainedValidationDAO.insert(invalid) must throwA[ValidationErrorChain]
      ChainedValidationDAO.findOneById(_id) must beNone
    }
    "throw a validation chain exception containing multiple validation failures" in {
      val _id = new ObjectId
      val invalid2 = ToValidate(id = _id, a = -1, b = "")
      ToValidateChain.validator.apply(invalid2) must beLeft(ValidationErrorChain(invalid2, ToValidate.NegativeA(invalid2) :: ToValidate.EmptyB(invalid2) :: Nil))
      ChainedValidationDAO.insert(invalid2) must throwA[ValidationErrorChain]
      ChainedValidationDAO.findOneById(_id) must beNone
    }
  }
}
