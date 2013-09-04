package com.novus.salat.test

package object model {

  case class OrderStatus(s: String)
  object OrderStatus {
    val New = OrderStatus("0")
    val PartiallyFilled = OrderStatus("1")
    val Filled = OrderStatus("2")
  }

}
