package com.github.yashap.datetime

import java.time.LocalDateTime

import com.github.yashap.spec.BaseSpec

class LocalDateTimeMethodsSpec extends BaseSpec {
  def dt = LocalDateTime.of(2016, 1, 15, 23, 59, 59)
  def earlier = dt.minusNanos(1L)
  def later = dt.plusNanos(1L)

  "isBetween" should "return true when the main time is between the bounding times" in {
    dt.isBetween(earlier, later) shouldBe true
  }

  it should "return true when the main time is equal to the lower bound" in {
    dt.isBetween(dt, later) shouldBe true
  }

  it should "return false when the main time is equal to the upper bound" in {
    dt.isBetween(earlier, dt) shouldBe false
  }

  it should "return false when the main time is outside the bounding times" in {
    earlier.isBetween(dt, later) shouldBe false
    later.isBetween(earlier, dt) shouldBe false
  }

  "weeksUntil" should "be 0 for equal times" in {
    dt.weeksUntil(dt) shouldBe 0
  }

  it should "be count weeks between two times, rounding up, when the second time is before the first" in {
    dt.weeksUntil(dt.plusDays(1L)) shouldBe 1
    dt.weeksUntil(dt.plusDays(7L)) shouldBe 1
    dt.weeksUntil(dt.plusDays(7L).plusNanos(1L)) shouldBe 2
    dt.weeksUntil(dt.plusDays(70L)) shouldBe 10
  }

  it should "be count weeks between two times, rounding down, when the second time is after the first" in {
    dt.weeksUntil(dt.minusDays(1L)) shouldBe -1
    dt.weeksUntil(dt.minusDays(7L)) shouldBe -1
    dt.weeksUntil(dt.minusDays(7L).minusNanos(1L)) shouldBe -2
    dt.weeksUntil(dt.minusDays(70L)) shouldBe -10
  }

  "the implicit LocalDateTime ordering" should "properly allow the computation of the minimum time" in {
    Seq(earlier, dt, later).min shouldBe earlier
  }

  it should "properly allow the computation of the maximum time" in {
    Seq(earlier, dt, later).max shouldBe later
  }
}
