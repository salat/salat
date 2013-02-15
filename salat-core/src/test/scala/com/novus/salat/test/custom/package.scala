package com.novus.salat.test

import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }

package object custom {
  implicit val ctx = new Context() {
    val name = "custom_transformer_spec"
    override val typeHintStrategy = StringTypeHintStrategy(TypeHintFrequency.Always, "_t")
    registerCustomTransformer(BarTransformer)
  }

}

