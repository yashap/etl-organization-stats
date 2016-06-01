package com.github.yashap.transformer

import com.github.yashap.model.{OrganizationStatistic, OrganizationEvent}
import com.github.yashap.datetime._

import java.time.{LocalTime, LocalDateTime, LocalDate}

object OrgEventTransformer extends BaseTransformer[OrganizationEvent] {
  val FileSupportTicket = "file_support_ticket"

  def numSupportTickets(
    statName: String,
    events: Seq[OrganizationEvent],
    start: LocalDate,
    end: LocalDate
  ): Seq[OrganizationStatistic] = {

    val earliestDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val latestDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val filterCondition: OrganizationEvent => Boolean =
      event =>
        event.event == FileSupportTicket &&
        event.timestamp.isBetween(earliestDT, latestDT)

    val statFormula: Seq[OrganizationEvent] => Double =
      orgsEvents => orgsEvents.length.toDouble

    calculateOrganizationStatistic(statName, end, events, filterCondition, statFormula)
  }
}
