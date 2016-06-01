package com.github.yashap.spec

import java.time.{LocalDateTime, LocalTime, LocalDate}

trait TransformerSpec extends BaseSpec {
  val runDate = LocalDate.of(2015, 12, 19)
  val oneDayAgo = runDate.minusDays(1)
  val oneWeekAgo = runDate.minusDays(7L).plusDays(1L)
  val oneMonthAgo = runDate.minusMonths(1L).plusDays(1L)
  val tenWeeksAgo = runDate.minusDays(70L).plusDays(1L)

  val timePart = LocalTime.of(0, 0)
  val runDateDT = LocalDateTime.of(runDate, timePart)
  val oneDayAgoDT = LocalDateTime.of(oneDayAgo, timePart)
  val oneWeekAgoDT = LocalDateTime.of(oneWeekAgo, timePart)
  val oneMonthAgoDT = LocalDateTime.of(oneMonthAgo, timePart)
  val tenWeeksAgoDT = LocalDateTime.of(tenWeeksAgo, timePart)

  val (orgId, orgId2) = (1L, 2L)
  val (userId, userId2, userId3) = (1L, 2L, 3L)

  val EventOther = "some_other_event"
}
