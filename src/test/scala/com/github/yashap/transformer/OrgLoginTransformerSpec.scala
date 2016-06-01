package com.github.yashap.transformer

import com.github.yashap.spec.TransformerSpec
import com.github.yashap.transformer.OrgLoginTransformer.{AndroidLogin, IOSLogin, WebLogin}
import com.github.yashap.pipeline.Pipeline.{AvgWeeklyLogins, LoginsPastWeek}
import com.github.yashap.model.{OrganizationUserEvent, OrganizationStatistic}

class OrgLoginTransformerSpec extends TransformerSpec {

  def logins(events: Seq[OrganizationUserEvent]): Seq[OrganizationStatistic] = {
    OrgLoginTransformer.logins(
      LoginsPastWeek, events, oneWeekAgo, runDate
    ).sortBy(_.organizationId)
  }

  def averageWeeklyLogins(events: Seq[OrganizationUserEvent]): Seq[OrganizationStatistic] = {
    OrgLoginTransformer.averageWeeklyLogins(
      AvgWeeklyLogins, events, tenWeeksAgo, runDate
    ).sortBy(_.organizationId)
  }

  "logins" should "only consider login events" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, WebLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, AndroidLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, IOSLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, None, orgId)
    )

    logins(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, LoginsPastWeek, 3.0)
    )
  }

  it should "only consider events in the past week" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, WebLogin, None, orgId),
      OrganizationUserEvent(oneWeekAgoDT.minusSeconds(1), userId, AndroidLogin, None, orgId)
    )

    logins(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, LoginsPastWeek, 1.0)
    )
  }

  it should "count the number of login events in the past week, per org" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, WebLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, AndroidLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId2, IOSLogin, None, orgId2),
      OrganizationUserEvent(oneDayAgoDT, userId2, EventOther, None, orgId2)
    )

    logins(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, LoginsPastWeek, 2.0),
      OrganizationStatistic(orgId2, runDate, LoginsPastWeek, 1.0)
    ).sortBy(_.organizationId)
  }

  "averageWeeklyLogins" should "only consider login events" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, WebLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, AndroidLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, IOSLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, None, orgId)
    )

    averageWeeklyLogins(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, AvgWeeklyLogins, 3.0)
    )
  }

  it should "base the number of weeks to average over on the earliest login event its passed" in {
    val numWeeks = 5

    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, AndroidLogin, None, orgId),
      OrganizationUserEvent(runDateDT.plusDays(1).minusWeeks(numWeeks), userId, WebLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, IOSLogin, None, orgId)
    )

    averageWeeklyLogins(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, AvgWeeklyLogins, events.length / numWeeks.toDouble)
    )
  }

  it should "average the number of login events per org" in {
    val weeksForOrg1 = 5
    val weeksForOrg2 = 6

    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, WebLogin, None, orgId),
      OrganizationUserEvent(runDateDT.plusDays(1).minusWeeks(weeksForOrg1), userId, WebLogin, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId2, AndroidLogin, None, orgId2),
      OrganizationUserEvent(runDateDT.plusDays(1).minusWeeks(weeksForOrg2), userId2, IOSLogin, None, orgId2)
    )

    averageWeeklyLogins(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, AvgWeeklyLogins, 2.0 / weeksForOrg1),
      OrganizationStatistic(orgId2, runDate, AvgWeeklyLogins, 2.0 / weeksForOrg2)
    ).sortBy(_.organizationId)
  }
}
