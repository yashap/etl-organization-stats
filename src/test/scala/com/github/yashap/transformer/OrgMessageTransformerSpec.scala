package com.github.yashap.transformer

import com.github.yashap.spec.TransformerSpec
import com.github.yashap.transformer.OrgMessageTransformer.MessageSent
import com.github.yashap.pipeline.Pipeline.{AvgWeeklyMsgs, MsgsPastWeek}
import com.github.yashap.model.{OrganizationStatistic, OrganizationUserEvent}
import com.github.yashap.model.SocialNetworkType._

class OrgMessageTransformerSpec extends TransformerSpec {

  val TwitterMsgsSentPastWeek = MsgsPastWeek(Twitter)
  val FacebookMsgsSentPastWeek = MsgsPastWeek(Facebook)
  val InstagramMsgsSentPastWeek = MsgsPastWeek(Instagram)
  val LinkedInMsgsSentPastWeek = MsgsPastWeek(LinkedIn)

  val TwitterMsgsAvgWeekly = AvgWeeklyMsgs(Twitter)
  val FacebookMsgsAvgWeekly = AvgWeeklyMsgs(Facebook)
  val InstagramMsgsAvgWeekly = AvgWeeklyMsgs(Instagram)
  val LinkedInMsgsAvgWeekly = AvgWeeklyMsgs(LinkedIn)

  "messagesSentPastWeek" should "only consider message events with social networks" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Facebook), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Instagram), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, None, orgId)
    )

    messagesSent(events).sortBy(stat => (stat.organizationId, stat.statistic)) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, TwitterMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId, runDate, FacebookMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId, runDate, InstagramMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId, runDate, LinkedInMsgsSentPastWeek, 2.0)
    ).sortBy(stat => (stat.organizationId, stat.statistic))
  }

  it should "only consider events in the past week" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(oneWeekAgoDT.minusSeconds(1), userId, MessageSent, Some(Twitter), orgId)
    )

    messagesSent(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, TwitterMsgsSentPastWeek, 1.0)
    )
  }

  it should "count the number of message events in the past week, per org, and per social network" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Facebook), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Instagram), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId2, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId3, MessageSent, Some(Twitter), orgId2),
      OrganizationUserEvent(oneDayAgoDT, userId3, MessageSent, Some(Facebook), orgId2)
    )

    messagesSent(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, TwitterMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId, runDate, FacebookMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId, runDate, InstagramMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId, runDate, LinkedInMsgsSentPastWeek, 2.0),
      OrganizationStatistic(orgId2, runDate, TwitterMsgsSentPastWeek, 1.0),
      OrganizationStatistic(orgId2, runDate, FacebookMsgsSentPastWeek, 1.0)
    ).sortBy(stat => (stat.organizationId, stat.statistic))
  }

  "averageWeeklyMessages" should "only consider login events" in {
    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Facebook), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Instagram), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, None, orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, EventOther, None, orgId)
    )

    averageWeeklyMessages(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, TwitterMsgsAvgWeekly, 1.0),
      OrganizationStatistic(orgId, runDate, FacebookMsgsAvgWeekly, 1.0),
      OrganizationStatistic(orgId, runDate, InstagramMsgsAvgWeekly, 1.0),
      OrganizationStatistic(orgId, runDate, LinkedInMsgsAvgWeekly, 2.0)
    ).sortBy(stat => (stat.organizationId, stat.statistic))
  }

  it should "base the number of weeks to average over on the earliest login event its passed" in {
    val numWeeks = 5

    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(runDateDT.plusDays(1).minusWeeks(numWeeks), userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId)
    )

    averageWeeklyMessages(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, TwitterMsgsAvgWeekly, 3.0 / numWeeks)
    )
  }

  it should "average the number of login events per org" in {
    val weeksForOrg1 = 5
    val weeksForOrg2 = 6

    val events = Seq(
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Twitter), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(Facebook), orgId),
      OrganizationUserEvent(runDateDT.plusDays(1).minusWeeks(weeksForOrg1), userId, MessageSent, Some(Instagram), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId, MessageSent, Some(LinkedIn), orgId),
      OrganizationUserEvent(oneDayAgoDT, userId2, MessageSent, Some(LinkedIn), orgId),

      OrganizationUserEvent(oneDayAgoDT, userId3, MessageSent, Some(Instagram), orgId2),
      OrganizationUserEvent(runDateDT.plusDays(1).minusWeeks(weeksForOrg2), userId3, MessageSent, Some(Instagram), orgId2)
    )

    averageWeeklyMessages(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, TwitterMsgsAvgWeekly, 1.0),
      OrganizationStatistic(orgId, runDate, FacebookMsgsAvgWeekly, 1.0),
      OrganizationStatistic(orgId, runDate, InstagramMsgsAvgWeekly, 1.0 / weeksForOrg1),
      OrganizationStatistic(orgId, runDate, LinkedInMsgsAvgWeekly, 2.0),

      OrganizationStatistic(orgId2, runDate, InstagramMsgsAvgWeekly, 2.0 / weeksForOrg2)
    ).sortBy(stat => (stat.organizationId, stat.statistic))
  }

  def messagesSent(events: Seq[OrganizationUserEvent]): Seq[OrganizationStatistic] = {
    OrgMessageTransformer.messagesSent(
      MsgsPastWeek, events, oneWeekAgo, runDate
    ).sortBy(stat => (stat.organizationId, stat.statistic))
  }

  def averageWeeklyMessages(events: Seq[OrganizationUserEvent]): Seq[OrganizationStatistic] = {
    OrgMessageTransformer.averageWeeklyMessages(
      AvgWeeklyMsgs, events, tenWeeksAgo, runDate
    ).sortBy(stat => (stat.organizationId, stat.statistic))
  }
}
