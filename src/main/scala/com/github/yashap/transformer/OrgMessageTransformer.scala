package com.github.yashap.transformer

import com.github.yashap.model.SocialNetworkType.SocialNetworkType
import com.github.yashap.model.{OrganizationStatistic, OrganizationUserEvent}
import com.github.yashap.datetime._

import java.time.{LocalTime, LocalDateTime, LocalDate}

object OrgMessageTransformer extends BaseTransformer[OrganizationUserEvent] {
  val MessageSent = "message_sent"

  private def calcMessageStats(
    statName: SocialNetworkType => String,
    statDate: LocalDate,
    events: Seq[OrganizationUserEvent],
    filterCondition: OrganizationUserEvent => Boolean,
    statFormula: Seq[OrganizationUserEvent] => Double
  ): Seq[OrganizationStatistic] = {

    val messageEvents = events
      .filter(_.event == MessageSent)
      .filter(_.socialNetworkType.isDefined)
      .filter(filterCondition)

    val orgSNMessages: Map[Long, Map[SocialNetworkType, Seq[OrganizationUserEvent]]] = messageEvents
      .groupBy(_.organizationId)
      .map { case (orgId, orgsMessages) =>
        orgId -> orgsMessages
          .groupBy(_.socialNetworkType)
          .map { case (optSN, messagesForOrgPerSN) =>
            optSN.get -> messagesForOrgPerSN  // safe because of the filter on messageEvents
          }
      }

    orgSNMessages.toSeq.flatMap { case (orgId, snMsgMap) =>
      snMsgMap.map { case (sn, messages) =>
        OrganizationStatistic(orgId, statDate, statName(sn), statFormula(messages))
      }
    }
  }

  def messagesSent(
    statName: SocialNetworkType => String,
    events: Seq[OrganizationUserEvent],
    start: LocalDate,
    end: LocalDate
  ): Seq[OrganizationStatistic] = {

    val earliestDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val latestDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val filterCondition: OrganizationUserEvent => Boolean =
      event => event.timestamp.isBetween(earliestDT, latestDT)

    val statFormula: Seq[OrganizationUserEvent] => Double =
      orgsSNMessages => orgsSNMessages.length.toDouble

    calcMessageStats(statName, end, events, filterCondition, statFormula)
  }

  def averageWeeklyMessages(
    statName: SocialNetworkType => String,
    events: Seq[OrganizationUserEvent],
    start: LocalDate,
    end: LocalDate
  ): Seq[OrganizationStatistic] = {

    val earliestDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val latestDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val filterCondition: OrganizationUserEvent => Boolean =
      event => true

    val statFormula: Seq[OrganizationUserEvent] => Double =
      orgsSNMessages => {
        val firstSNMessageInEvents = orgsSNMessages.map(_.timestamp).min
        val weeksInPeriod = firstSNMessageInEvents.weeksUntil(latestDT)
        val weeksBetweenProvidedDate = earliestDT.weeksUntil(latestDT)
        val totalWeeks = Seq(weeksInPeriod, weeksBetweenProvidedDate).min

        orgsSNMessages.length / totalWeeks.toDouble
      }

    calcMessageStats(statName, end, events, filterCondition, statFormula)
  }
}
