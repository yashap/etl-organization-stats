package com.github.yashap.transformer

import com.github.yashap.model.{OrganizationPayment, OrganizationStatistic}
import com.github.yashap.datetime._

import java.time.{LocalTime, LocalDateTime, LocalDate}

object OrgPaymentTransformer extends BaseTransformer[OrganizationPayment] {

  def amountPaid(
    statName: String,
    events: Seq[OrganizationPayment],
    start: LocalDate,
    end: LocalDate
  ): Seq[OrganizationStatistic] = {

    val earliestDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val latestDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val filterCondition: OrganizationPayment => Boolean =
      event => event.timestamp.isBetween(earliestDT, latestDT)

    val statFormula: Seq[OrganizationPayment] => Double =
      orgsPayments => orgsPayments.map(_.paymentAmount).sum

    calculateOrganizationStatistic(statName, end, events, filterCondition, statFormula)
  }
}
