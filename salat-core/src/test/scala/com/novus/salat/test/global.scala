package com.novus.salat.test

import com.novus.salat._

package object global {
  implicit val ctx = new Context {
    val name = "Global test context"
    override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.Always, typeHint = TypeHint)
  }
}
