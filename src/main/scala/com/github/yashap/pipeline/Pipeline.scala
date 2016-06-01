package com.github.yashap.pipeline

import com.github.yashap.dao._
import com.github.yashap.model.SocialNetworkType._
import com.github.yashap.transformer._
import com.github.yashap.model.OrganizationStatistic

import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

class Pipeline (
  orgDAO: OrganizationDAO,
  orgUserEventDAO: OrganizationUserEventDAO,
  orgPaymentDAO: OrganizationPaymentDAO,
  orgEventDAO: OrganizationEventDAO,
  orgStatDAO: OrganizationStatisticDAO
) {
  import Pipeline._

  private[pipeline] def getOrgStats(
    organizationId: Long,
    runDate: LocalDate
  )(implicit ec: ExecutionContext): Future[Seq[OrganizationStatistic]] = {
    val oneWeekAgo = runDate.minusDays(7L).plusDays(1L)
    val oneMonthAgo = runDate.minusMonths(1L).plusDays(1L)
    val tenWeeksAgo = runDate.minusDays(70L).plusDays(1L)

    // Get all relevant org events
    val fOrgUserEvents = orgUserEventDAO.get(organizationId, tenWeeksAgo, runDate)
    val fOrgPayments = orgPaymentDAO.get(organizationId, oneMonthAgo, runDate)
    val fOrgEvents = orgEventDAO.get(organizationId, oneMonthAgo, runDate)

    for {
      // Get events from db for org
      orgUserEvents <- fOrgUserEvents
      orgPayments <- fOrgPayments
      orgEvents <- fOrgEvents

      // Transform events
      loginsPastWeek = OrgLoginTransformer.logins(LoginsPastWeek, orgUserEvents, oneWeekAgo, runDate)
      avgWeeklyLogins = OrgLoginTransformer.averageWeeklyLogins(AvgWeeklyLogins, orgUserEvents, tenWeeksAgo, runDate)
      msgsPastWeek = OrgMessageTransformer.messagesSent(MsgsPastWeek, orgUserEvents, oneWeekAgo, runDate)
      avgWeeklyMsgs = OrgMessageTransformer.averageWeeklyMessages(AvgWeeklyMsgs, orgUserEvents, tenWeeksAgo, runDate)
      paidPastMonth = OrgPaymentTransformer.amountPaid(AmountPaidPastMonth, orgPayments, oneMonthAgo, runDate)
      ticketsPastMonth = OrgEventTransformer.numSupportTickets(NumTicketsPastMonth, orgEvents, oneMonthAgo, runDate)
    } yield loginsPastWeek ++ avgWeeklyLogins ++ msgsPastWeek ++ avgWeeklyMsgs ++ paidPastMonth ++ ticketsPastMonth
  }

  def run(runDate: LocalDate)(implicit ec: ExecutionContext): Future[Int] = {
    val orgIds: Future[Seq[Long]] = orgDAO.getAll().map(_.map(_.organizationId))

    orgIds.flatMap { orgIds =>
      val calcAndSave: Seq[Future[Int]] = orgIds.map { orgId =>
        getOrgStats(orgId, runDate).flatMap(orgStatDAO.upsert)
      }

      Future.sequence(calcAndSave).map(_.sum)
    }
  }
}

object Pipeline {
  val LoginsPastWeek = "logins_past_week"
  val AvgWeeklyLogins = "average_weekly_logins"
  val MsgsPastWeek: SocialNetworkType => String = sn => s"${sn}_messages_sent_past_week".toLowerCase
  val AvgWeeklyMsgs: SocialNetworkType => String = sn => s"average_weekly_${sn}_messages_sent".toLowerCase
  val AmountPaidPastMonth = "amount_paid_past_month"
  val NumTicketsPastMonth = "num_support_tickets_past_month"
}
