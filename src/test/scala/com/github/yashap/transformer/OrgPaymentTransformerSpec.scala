package com.github.yashap.transformer

import com.github.yashap.pipeline.Pipeline.AmountPaidPastMonth
import com.github.yashap.model.{OrganizationPayment, OrganizationStatistic}
import com.github.yashap.model.PaymentProcessor._
import com.github.yashap.spec.TransformerSpec

class OrgPaymentTransformerSpec extends TransformerSpec {

  def amountPaid(events: Seq[OrganizationPayment]): Seq[OrganizationStatistic] = {
    OrgPaymentTransformer.amountPaid(
      AmountPaidPastMonth, events, oneMonthAgo, runDate
    ).sortBy(_.organizationId)
  }

  val DoesntMatter = "doesnt_matter"

  "numSupportTicketsPastMonth" should "only consider events in the past month" in {
    val events = Seq(
      OrganizationPayment("1", oneDayAgoDT, orgId, DoesntMatter, 10.0, Chase),
      OrganizationPayment("2", oneMonthAgoDT.minusSeconds(1), orgId, DoesntMatter, 11.0, PayPal)
    )

    amountPaid(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, AmountPaidPastMonth, 10.0)
    )
  }

  it should "sum payments in the past mont, per org" in {
    val events = Seq(
      OrganizationPayment("1", oneDayAgoDT, orgId, DoesntMatter, 10.0, Chase),
      OrganizationPayment("2", oneDayAgoDT, orgId, DoesntMatter, 11.0, Chase),
      OrganizationPayment("3", oneDayAgoDT, orgId2, DoesntMatter, 100.0, PayPal),
      OrganizationPayment("4", oneDayAgoDT, orgId2, DoesntMatter, 101.0, PayPal),
      OrganizationPayment("5", oneDayAgoDT, orgId2, DoesntMatter, 102.0, PayPal)
    )

    amountPaid(events) shouldBe Seq(
      OrganizationStatistic(orgId, runDate, AmountPaidPastMonth, 21.0),
      OrganizationStatistic(orgId2, runDate, AmountPaidPastMonth, 303.0)
    ).sortBy(_.organizationId)
  }
}
