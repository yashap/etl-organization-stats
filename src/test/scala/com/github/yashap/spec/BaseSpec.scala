package com.github.yashap.spec

import org.scalatest.concurrent.Eventually
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FlatSpecLike, Matchers}

trait BaseSpec extends FlatSpecLike
  with Eventually
  with Matchers
  with MockitoSugar {

  override implicit val patienceConfig = PatienceConfig(
    timeout = scaled(Span(2, Seconds)),
    interval = scaled(Span(20, Millis))
  )
}
