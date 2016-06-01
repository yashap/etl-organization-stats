package com.github.yashap.transformer

import com.github.yashap.model.{BaseOrgEvent, OrganizationStatistic}

import java.time.LocalDate

trait BaseTransformer[T <: BaseOrgEvent] {

  protected def calculateOrganizationStatistic(
    statName: String,
    statDay: LocalDate,
    events: Seq[T],
    filterCondition: T => Boolean,
    statFormula: Seq[T] => Double
  ): Seq[OrganizationStatistic] = {

    events
      .filter(filterCondition)
      .groupBy(_.organizationId)
      .toSeq
      .map { case (orgId, tickets) =>
        OrganizationStatistic(orgId, statDay, statName, statFormula(tickets))
      }
  }
}
