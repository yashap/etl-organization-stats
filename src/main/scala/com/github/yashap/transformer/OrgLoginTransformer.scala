package com.github.yashap.transformer

import com.github.yashap.model.{OrganizationStatistic, OrganizationUserEvent}
import com.github.yashap.datetime._

import java.time.{LocalTime, LocalDateTime, LocalDate}

object OrgLoginTransformer extends BaseTransformer[OrganizationUserEvent] {
  val WebLogin = "web_login"
  val AndroidLogin = "android_login"
  val IOSLogin = "ios_login"
  val LoginEvents = Set(WebLogin, AndroidLogin, IOSLogin)

  def logins(
    statName: String,
    events: Seq[OrganizationUserEvent],
    start: LocalDate,
    end: LocalDate
  ): Seq[OrganizationStatistic] = {

    val earliestDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val latestDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val filterCondition: OrganizationUserEvent => Boolean =
      event =>
        LoginEvents.contains(event.event) &&
        event.timestamp.isBetween(earliestDT, latestDT)

    val statFormula: Seq[OrganizationUserEvent] => Double =
      orgsLogings => orgsLogings.length.toDouble

    calculateOrganizationStatistic(statName, end, events, filterCondition, statFormula)
  }

  def averageWeeklyLogins(
    statName: String,
    events: Seq[OrganizationUserEvent],
    start: LocalDate,
    end: LocalDate
  ): Seq[OrganizationStatistic] = {

    val earliestDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val latestDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val filterCondition: OrganizationUserEvent => Boolean =
      event => LoginEvents.contains(event.event)

    val statFormula: Seq[OrganizationUserEvent] => Double =
      orgsLogins => {
        val firstLoginInEvents = orgsLogins.map(_.timestamp).min
        val weeksInPeriod = firstLoginInEvents.weeksUntil(latestDT)
        val weeksBetweenProvidedDate = earliestDT.weeksUntil(latestDT)
        val totalWeeks = Seq(weeksInPeriod, weeksBetweenProvidedDate).min

        orgsLogins.length / totalWeeks.toDouble
      }

    calculateOrganizationStatistic(statName, end, events, filterCondition, statFormula)
  }
}
