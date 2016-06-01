package com.github.yashap.model

import java.time.LocalDate

case class OrganizationStatistic(
  organizationId: Long,
  day: LocalDate,
  statistic: String,
  value: Double
)
