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

import com.novus.salat._
import com.novus.salat.impls._
import com.novus.salat.annotations._

import scala.collection.Traversable
import scala.tools.scalap.scalax.rules.scalasig._
import com.mongodb.casbah.commons.Imports._
import com.novus.salat.annotations.raw.EnumAs
import com.novus.salat.util._

trait Transformation {

  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context): Any

  def before(path: String, t: TypeRefType, value: Any)(implicit ctx: Context): Option[Any] = Some(value)

  def after(path: String, t: TypeRefType, value: Any)(implicit ctx: Context): Option[Any] = Some(value)

  def transform_!(path: String, t: TypeRefType, x: Any)(implicit ctx: Context): Option[Any] =
    before(path, t, x).flatMap(x => after(path, t, transform(path, t, x)))

}

trait TransformationWithParentType {

  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context): Any

  def before(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context): Option[Any] = Some(value)

  def after(path: String, t: TypeRefType, pt: TypeRefType, value: Any)(implicit ctx: Context): Option[Any] = Some(value)

  def transform_!(path: String, t: TypeRefType, pt: TypeRefType, x: Any)(implicit ctx: Context): Option[Any] =
    before(path, t, pt, x).flatMap(x => after(path, t, pt, transform(path, t, x)))

}

trait StraightThrough extends Transformation {
  def transform(path: String, t: TypeRefType, value: Any)(implicit ctx: Context) = value
}


















