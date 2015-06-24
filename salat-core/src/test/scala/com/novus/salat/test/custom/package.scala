package com.novus.salat.test

import com.novus.salat.{Context, StringTypeHintStrategy, TypeHintFrequency}

package object custom {
  implicit val ctx = new Context() {
    val name = "custom_transformer_spec"
    override val typeHintStrategy = StringTypeHintStrategy(TypeHintFrequency.Always, "_t")
    registerCustomTransformer(BarTransformer)
    registerCustomTransformer(BicycleTransformer)
    registerCustomTransformer(WibbleTransformer)
  }

}

