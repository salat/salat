/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. (http://www.novus.com)
 *
 * Module:        salat-core
 * Class:         ValidatingDAO.scala
 * Last modified: 2012-12-04 17:16:30 EST
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

package com.novus.salat.dao

import com.mongodb.casbah.Imports._
import scala.Right
import com.novus.salat.Context
import com.mongodb

object Validation {
  /** Example of a validation that does nothing but return success
   *  @tparam T the object to validate
   *  @return the input wrapped in the successful side of an Either[Throwable, T]
   */
  implicit def nil[T]: Validation[T] = new Validation[T] {
    def apply(x: T) = Right(x)
  }
}

/** Simple validation type class
 *  @tparam T type to validate
 */
trait Validation[T] {
  /** Simple validation method
   *  @param x the input to validate
   *  @return if successful, the input wrapped in Right; if unsuccessful, a Throwable of your choice wrapped in Left
   */
  def apply(x: T): Either[Throwable, T]
}

trait ChainedValidation[T] extends Validation[T] {

  def validationChain: Iterable[Validation[T]]

  require(validationChain.nonEmpty, "%s: validation chain must have at least one entry!".format(getClass.getName))

  /** Validation method that applies a chain of checks to the input object
   *  @param x the input to validate
   *  @return if successful, the input wrapped in Right; if unsuccessful, a Throwable of your choice wrapped in Left
   */
  def apply(x: T) = {
    val (failed, success) = validationChain.map(_.apply(x)).partition(_.isLeft)
    if (failed.nonEmpty) {
      Left(ValidationErrorChain(x, failed.map(_.left.get)))
    }
    else Right(success.last.right.get)
  }
}

case class ValidationErrorChain(t: Any, iter: Iterable[Throwable]) extends Error(
  """
    |VALIDATION FAILED
    |
    |INPUT
    |%s
    |
    |BECAUSE
    |%s
  """.stripMargin.format(t, iter.map(_.getMessage).mkString("\n")))

abstract class Validates[T: Validation] {
  def apply(x: T) = implicitly[Validation[T]].apply(x)
}

case class MutilValidateError(ts: Traversable[Throwable]) extends Error(
  """
    |Multidoc insert failed with the following errors:
    |%s
  """.stripMargin.format(ts.map(_.getMessage).mkString("\n")))

abstract class ValidatingSalatDAO[ObjectType <: AnyRef, ID <: Any](override val collection: MongoCollection)(implicit mot: Manifest[ObjectType],
                                                                                                             mid: Manifest[ID], ctx: Context) extends SalatDAO[ObjectType, ID](collection)(mot, mid, ctx) {

  implicit def validator: Validation[ObjectType]

  log.debug(
    """
      |%s
    """.stripMargin, description)

  object validates extends Validates[ObjectType]

  /** @param t instance of ObjectType
   *  @param wc write concern
   *  @return if insert succeeds, ID of inserted object
   */
  override def insert(t: ObjectType, wc: mongodb.WriteConcern) = {
    validates(t) match {
      case Right(v) => super.insert(v, wc)
      case Left(e)  => throw e
    }
  }

  /** @param docs collection of `ObjectType` instances to insert
   *  @param wc write concern
   *  @return list of object ids
   *         TODO: flatten list of IDs - why on earth didn't I do that in the first place?
   */
  override def insert(docs: Traversable[ObjectType], wc: mongodb.WriteConcern) = if (docs.nonEmpty) {
    val (failed, validated) = docs.map(validates(_)).partition(_.isLeft)
    val r = if (validated.nonEmpty) {
      super.insert(validated.map(_.right.get), wc)
    }
    else Nil
    if (failed.nonEmpty) {
      throw MutilValidateError(failed.map(_.left.get))
    }
    r
  }
  else Nil

  /** Performs an update operation.
   *  @param q search query for old object to update
   *  @param t object with which to update <tt>q</tt>
   *  @param upsert if the database should create the element if it does not exist
   *  @param multi if the update should be applied to all objects matching
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  override def update(q: mongodb.DBObject, t: ObjectType, upsert: Boolean, multi: Boolean, wc: mongodb.WriteConcern) = validates(t) match {
    case Right(v) => super.update(q, v, upsert, multi, wc)
    case Left(e)  => throw e
  }

  override lazy val description = "ValidatingSalatDAO[%s,%s](%s) using validator %s".format(mot.erasure.getSimpleName, mid.erasure.getSimpleName, collection.name, implicitly[Validation[ObjectType]].getClass.getName)

  /** @param t object to save
   *  @param wc write concern
   *  @return (WriteResult) result of write operation
   */
  override def save(t: ObjectType, wc: mongodb.WriteConcern) = validates(t) match {
    case Right(v) => super.save(v, wc)
    case Left(e)  => throw e
  }
}
