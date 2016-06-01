package com.github.yashap.pipeline

import com.github.yashap.dao._
import com.github.yashap.model.PaymentProcessor._
import com.github.yashap.model.SocialNetworkType._
import com.github.yashap.model._
import com.github.yashap.spec.BaseSpec
import com.github.yashap.transformer.OrgEventTransformer._
import com.github.yashap.transformer.OrgLoginTransformer._
import com.github.yashap.transformer.OrgMessageTransformer._
import com.github.yashap.pipeline.Pipeline._
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => mockEq, _}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.time.{LocalDateTime, LocalDate, LocalTime}

class PipelineSpec extends BaseSpec {
  val runDay = LocalDate.of(2016, 4, 12)
  val timePart = LocalTime.of(0, 1)

  val oneWeekAgo = runDay.minusDays(7).plusDays(1)
  val withinAWeek = LocalDateTime.of(oneWeekAgo, timePart)

  val oneMonthAgo = runDay.minusMonths(1).plusDays(1)
  val withinAMonth =  LocalDateTime.of(oneMonthAgo, timePart)

  val tenWeeksAgo = runDay.minusDays(70).plusDays(1)
  val withinTenWeeks = LocalDateTime.of(tenWeeksAgo, timePart)

  val reallyOld = LocalDateTime.of(runDay.minusMonths(12), timePart)

  val orgId1 = 1L
  val org1 = Organization(orgId1, "org_one")
  val orgId2 = 2L
  val org2 = Organization(orgId2, "org_two")

  val user1 = 11L
  val user2 = 12L
  val user3 = 13L

  val DoesntMatter = "doesnt_matter"
  val SomeOtherEvent = "some_other_event"

  "a run" should "compute and load org stats for a single org" in new PipelineTest {
    // Stub out relevant DAO methods
    when(orgDAO.getAll()(any[ExecutionContext]))
      .thenReturn(Future.successful(Seq(org1)))

    setStubsForOrg(orgId1)

    // Run pipeline
    pipeline.run(runDay)

    // Verify expected stats get written
    eventually {
      verify(orgStatDAO).upsert(expectedStats(orgId1))
    }
  }

  it should "compute and load org stats for multiple orgs" in new PipelineTest {
    // Stub out relevant DAO methods
    when(orgDAO.getAll()(any[ExecutionContext]))
      .thenReturn(Future.successful(Seq(org1, org2)))

    setStubsForOrg(orgId1)
    setStubsForOrg(orgId2)

    // Run pipeline
    pipeline.run(runDay)

    // Verify expected stats get written
    eventually {
      verify(orgStatDAO).upsert(expectedStats(orgId1))
      verify(orgStatDAO).upsert(expectedStats(orgId2))
    }
  }

  it should "not explode with empty inputs" in new PipelineTest {
    when(orgDAO.getAll()(any[ExecutionContext]))
      .thenReturn(Future.successful(Seq()))

    when(orgUserEventDAO.get(any[Long], any[LocalDate], any[LocalDate])(any[ExecutionContext]))
      .thenReturn(Future.successful(Seq()))

    when(orgPaymentDAO.get(any[Long], any[LocalDate], any[LocalDate])(any[ExecutionContext]))
      .thenReturn(Future.successful(Seq()))

    when(orgEventDAO.get(any[Long], any[LocalDate], any[LocalDate])(any[ExecutionContext]))
      .thenReturn(Future.successful(Seq()))

    Await.result(pipeline.run(runDay), 1.second)

    verifyZeroInteractions(orgStatDAO)
  }

  def orgUserEvents(orgId: Long) = Seq(
    // Messages
    OrganizationUserEvent(withinAWeek, user1, MessageSent, Some(Twitter), orgId),
    OrganizationUserEvent(withinAWeek, user1, MessageSent, Some(Facebook), orgId),
    OrganizationUserEvent(withinAWeek, user2, MessageSent, Some(Instagram), orgId),
    OrganizationUserEvent(withinAWeek, user2, MessageSent, Some(LinkedIn), orgId),
    OrganizationUserEvent(withinAWeek, user3, MessageSent, Some(LinkedIn), orgId),

    // Logins
    OrganizationUserEvent(withinAWeek, user1, WebLogin, None, orgId),
    OrganizationUserEvent(withinAWeek, user2, AndroidLogin, None, orgId),
    OrganizationUserEvent(withinAWeek, user3, IOSLogin, None, orgId),

    // Shouldn't be counted
    OrganizationUserEvent(withinAWeek, user3, SomeOtherEvent, None, orgId),
    OrganizationUserEvent(reallyOld, user3, MessageSent, Some(LinkedIn), orgId),
    OrganizationUserEvent(reallyOld, user3, IOSLogin, None, orgId)
  )

  def orgEvents(orgId: Long) = Seq(
    // Support tickets
    OrganizationEvent("1", withinAMonth, orgId, FileSupportTicket),
    OrganizationEvent("2", withinAMonth, orgId, FileSupportTicket),

    // Shouldn't be counted
    OrganizationEvent("3", withinAMonth, orgId, SomeOtherEvent),
    OrganizationEvent("4", withinTenWeeks, orgId, FileSupportTicket)
  )

  def orgPayments(orgId: Long) = Seq(
    OrganizationPayment("1", withinAMonth, orgId, DoesntMatter, 10.0, Chase),
    OrganizationPayment("2", withinAMonth, orgId, DoesntMatter, 11.0, Chase),
    OrganizationPayment("3", withinAMonth, orgId, DoesntMatter, 100.0, PayPal),

    // Shouldn't be counted
    OrganizationPayment("4", withinTenWeeks, orgId, DoesntMatter, 100.0, PayPal)
  )

  def expectedStats(orgId: Long) = Seq(
    OrganizationStatistic(orgId, runDay, LoginsPastWeek, 3.0),
    OrganizationStatistic(orgId, runDay, AvgWeeklyLogins, 0.4),
    OrganizationStatistic(orgId, runDay, MsgsPastWeek(LinkedIn), 2.0),
    OrganizationStatistic(orgId, runDay, MsgsPastWeek(Facebook), 1.0),
    OrganizationStatistic(orgId, runDay, MsgsPastWeek(Twitter), 1.0),
    OrganizationStatistic(orgId, runDay, MsgsPastWeek(Instagram), 1.0),
    OrganizationStatistic(orgId, runDay, AvgWeeklyMsgs(LinkedIn), 0.3),
    OrganizationStatistic(orgId, runDay, AvgWeeklyMsgs(Facebook), 1.0),
    OrganizationStatistic(orgId, runDay, AvgWeeklyMsgs(Twitter), 1.0),
    OrganizationStatistic(orgId, runDay, AvgWeeklyMsgs(Instagram), 1.0),
    OrganizationStatistic(orgId, runDay, AmountPaidPastMonth, 121.0),
    OrganizationStatistic(orgId, runDay, NumTicketsPastMonth, 2.0)
  )

  trait PipelineTest {
    val orgDAO = mock[OrganizationDAO]
    val orgUserEventDAO = mock[OrganizationUserEventDAO]
    val orgPaymentDAO = mock[OrganizationPaymentDAO]
    val orgEventDAO = mock[OrganizationEventDAO]
    val orgStatDAO = mock[OrganizationStatisticDAO]

    // Never check what this returns, just stub out
    when(orgStatDAO.upsert(any[Seq[OrganizationStatistic]])(any[ExecutionContext]))
      .thenReturn(Future.successful(1))

    val pipeline = new Pipeline(orgDAO, orgUserEventDAO, orgPaymentDAO, orgEventDAO, orgStatDAO)

    def setStubsForOrg(orgId: Long) = {
      when(orgUserEventDAO.get(mockEq(orgId), any[LocalDate], any[LocalDate])(any[ExecutionContext]))
        .thenReturn(Future.successful(orgUserEvents(orgId)))

      when(orgPaymentDAO.get(mockEq(orgId), any[LocalDate], any[LocalDate])(any[ExecutionContext]))
        .thenReturn(Future.successful(orgPayments(orgId)))

      when(orgEventDAO.get(mockEq(orgId), any[LocalDate], any[LocalDate])(any[ExecutionContext]))
        .thenReturn(Future.successful(orgEvents(orgId)))
    }
  }
}
