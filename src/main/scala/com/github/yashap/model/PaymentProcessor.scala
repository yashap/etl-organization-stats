package com.github.yashap.model

object PaymentProcessor extends Enumeration {
  type PaymentProcessor = Value

  val Chase = Value("Chase")
  val PayPal = Value("PayPal")
}
