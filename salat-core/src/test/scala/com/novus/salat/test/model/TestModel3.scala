package com.novus.salat.test.model

import com.novus.salat.annotations._
import com.novus.salat.Context

package object suppress_default_args {
  implicit val ctx = new Context {
    val name = "suppres-default args"
    override val suppressDefaultArgs = true
  }
}

package sda {

  object Foo {
    val empty = Foo(0)
  }

  case class Foo(x: Int = 0)

  object Bar {
    val S = "xyz"
  }

  case class Bar(foo: Foo = Foo.empty, s: String = Bar.S, o: Option[String] = None, baz: Baz = Qux.empty)

  @Salat
  trait Baz {
    def thingy: String
  }

  object Qux {
    val Thingy = "thingy"
    val empty = Qux(Thingy)
  }

  case class Qux(thingy: String = Qux.Thingy) extends Baz

}