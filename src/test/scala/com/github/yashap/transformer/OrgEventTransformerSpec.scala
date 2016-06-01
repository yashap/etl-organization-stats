package com.github.yashap.transformer

import com.github.yashap.spec.TransformerSpec
import com.github.yashap.transformer.OrgEventTransformer.FileSupportTicket
import com.github.yashap.pipeline.Pipeline.NumTicketsPastMonth
import com.github.yashap.model.{OrganizationEvent, OrganizationStatistic}

class OrgEventTransformerSpec extends TransformerSpec {

  def numSupportTickets(events: Seq[OrganizationEvent]): Seq[OrganizationStatistic] = {
    OrgEventTransformer.numSupportTickets(
      NumTicketsPastMonth, events, oneMonthAgo, runDate
    ).sortBy(_.organizationId)
  }

  "numSupportTickets" should "only consider support ticket events" in {
    val events = Seq(
      OrganizationEvent("1", oneDayAgoDT, orgId, FileSupportTicket),
      OrganizationEvent("2", oneDayAgoDT, orgId, "some_other_event")
    )

    numSupportTickets(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, NumTicketsPastMonth, 1.0)
    )
  }

  it should "only consider events in the specified period month" in {
    val events = Seq(
      OrganizationEvent("1", oneDayAgoDT, orgId, FileSupportTicket),
      OrganizationEvent("2", oneMonthAgoDT.minusSeconds(1), orgId, FileSupportTicket)
    )

    numSupportTickets(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, NumTicketsPastMonth, 1.0)
    )
  }

  it should "count the number of support ticket events, per org" in {
    val events = Seq(
      OrganizationEvent("1", oneDayAgoDT, orgId, FileSupportTicket),
      OrganizationEvent("2", oneDayAgoDT, orgId, FileSupportTicket),
      OrganizationEvent("3", oneDayAgoDT, orgId, "some_other_event"),
      OrganizationEvent("4", oneDayAgoDT, orgId2, FileSupportTicket),
      OrganizationEvent("5", oneMonthAgoDT.minusSeconds(1), orgId2, FileSupportTicket)
    )

    numSupportTickets(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, NumTicketsPastMonth, 2.0),
      OrganizationStatistic(orgId2, runDate, NumTicketsPastMonth, 1.0)
    ).sortBy(_.organizationId)
  }
}
