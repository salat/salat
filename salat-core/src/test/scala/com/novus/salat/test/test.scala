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

import com.novus.salat.test.model._
import com.novus.salat.global._
import scala.collection.immutable.{Map => IMap}
import scala.collection.mutable.{Map => MMap}
import scala.math.{BigDecimal => ScalaBigDecimal}

package object test {

  val SalatSpecDb = "test_salat"

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
        c = ScalaBigDecimal((i * i).toDouble / 123d, mathCtx), cc = None, ccc = None
      )
  })

  def evil_empire = Company(name = "Evil Empire, Inc.",
    year_of_inception = 2000,
    departments = Map(
      "MoK" -> Department(name = "Murder of Kittens",
        head_honcho = Some(Employee("Dick Cheney", None, Some(ScalaBigDecimal(12345.6789)))),
        cya_factor = ScalaBigDecimal(0.01980082),
        minions = List(
          Employee("George W. Bush", Some(66), None),
          Employee(name = "Michael Eisner", age = None, annual_salary = None)
        )
      ),
      "FOSS_Sabotage" -> Department(name = "Sabotage of F/OSS projects",
        head_honcho = Some(Employee("Bill Gates", None, Some(ScalaBigDecimal(28823.772383)))),
        cya_factor = ScalaBigDecimal(14.982023002),
        minions = List(
          Employee(name = "Darl McBridge", age = Some(123), annual_salary = None),
          Employee(name = "Patent Trolls Everywhere", age = None, annual_salary = Some(ScalaBigDecimal(1000000.00022303)))
        )
      )
    )
  )
}

