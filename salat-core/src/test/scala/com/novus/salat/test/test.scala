/*
 * Copyright (c) 2010 - 2012 Novus Partners, Inc. <http://novus.com>
 *
 * Module:        salat-core
 * Class:         test.scala
 * Last modified: 2012-04-28 20:39:09 EDT
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
 * Project:      http://github.com/novus/salat
 * Wiki:         http://github.com/novus/salat/wiki
 * Mailing list: http://groups.google.com/group/scala-salat
 */
package com.novus.salat

import com.novus.salat.test.model._
import com.novus.salat.test.global._
import scala.collection.immutable.{ Map => IMap }
import scala.math.{ BigDecimal => ScalaBigDecimal }
import org.joda.time.format.{ PeriodFormat, PeriodFormatterBuilder }
import org.scala_tools.time.TypeImports._
import org.joda.time.{ DateTimeZone, Period, DateTime }
import scala.collection.mutable.{ ArrayBuffer, Map => MMap }

package object test {

  val SalatSpecDb = "test_salat"
  val AlphaColl = "alpha_dao_spec"
  val EpsilonColl = "epsilon_dao_spec"
  val ThetaColl = "theta_dao_spec"
  val XiColl = "xi_dao_spec"
  val KappaColl = "kappa_dao_spec"
  val ParentColl = "parent_dao_spec"
  val ChildColl = "child_dao_spec"
  val UserColl = "user_dao_spec"
  val RoleColl = "role_dao_spec"
  val MyModelColl = "my_model_coll"

  def graph = Alice("x", Some("y"),
    Basil(Some(80), 81,
      Clara(Seq("l1", "l2"), List(1, 2), List(
        Desmond(IMap("foo1" -> Alice("foo", None, Basil(p = None, r = Clara(m = Nil, n = Nil))),
          "baz1" -> Alice("baz", Some("quux"), Basil(p = None, r = Clara(m = Nil, n = Nil)))),
          MMap("a1" -> 1, "c1" -> 2),
          Some(Basil(
            None, 24, Clara(
              List("l3", "l4"), List(1, 2, 3), Nil))))))))

  def numbers = Edward(a = "a value", aa = None, aaa = Some("aaa value"),
    b = 2, bb = None, bbb = Some(22),
    c = ScalaBigDecimal(3.30003), cc = None, ccc = Some(ScalaBigDecimal(33.30003)))

  def mucho_numbers(factor: Long = 10) = Fanny((0L until factor).toList.map {
    i =>
      Edward(
        a = "a %d".format(i), aa = Some("aa %d".format(i)), aaa = None,
        b = (i * i * 123 / 1000).toInt, bb = None, bbb = Some((i * i * 321 / 100).toInt),
        c = ScalaBigDecimal((i * i).toDouble / 123d, ctx.bigDecimalStrategy.mathCtx), cc = None, ccc = None)
  })

  def evil_empire = Company(name = "Evil Empire, Inc.",
    year_of_inception = 2000,
    departments = Map(
      "MoK" -> Department(name = "Murder of Kittens",
        head_honcho = Some(Employee("Dick Cheney", None, Some(ScalaBigDecimal(12345.6789)))),
        cya_factor = ScalaBigDecimal(0.01980082),
        minions = List(
          Employee("George W. Bush", Some(66), None),
          Employee(name = "Michael Eisner", age = None, annual_salary = None))),
      "FOSS_Sabotage" -> Department(name = "Sabotage of F/OSS projects",
        head_honcho = Some(Employee("Bill Gates", None, Some(ScalaBigDecimal(28823.772383)))),
        cya_factor = ScalaBigDecimal(14.982023002),
        minions = List(
          Employee(name = "Darl McBridge", age = Some(123), annual_salary = None),
          Employee(name = "Patent Trolls Everywhere", age = None, annual_salary = Some(ScalaBigDecimal(1000000.00022303)))))))
}