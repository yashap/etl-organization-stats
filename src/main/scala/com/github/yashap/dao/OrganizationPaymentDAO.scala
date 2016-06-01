package com.github.yashap.dao

import com.github.yashap.model._
import com.github.yashap.model.PaymentProcessor.PaymentProcessor
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success
import java.time.{LocalDateTime, LocalDate, LocalTime}

class OrganizationPaymentDAO(val dbConfig: DatabaseConfig[JdbcProfile]) extends BaseDAO {
  import driver.api._

  val tableName = "organization_payment"

  private class OrganizationPaymentTable(tag: Tag) extends Table[OrganizationPayment](tag, tableName) {
    def id = column[String]("id", O.PrimaryKey)
    def timestamp = column[LocalDateTime]("timestamp")
    def organizationId = column[Long]("organization_id")
    def event = column[String]("event")
    def paymentAmount = column[Double]("payment_amount")
    def paymentProcessor = column[PaymentProcessor]("payment_processor")

    def * = (
      id, timestamp, organizationId, event, paymentAmount, paymentProcessor
    ) <> (OrganizationPayment.tupled, OrganizationPayment.unapply)
  }

  private val organizationPaymentsTQ = TableQuery[OrganizationPaymentTable]

  def get(organizationId: Long, start: LocalDate, end: LocalDate)
    (implicit ec: ExecutionContext): Future[Seq[OrganizationPayment]] = {

    val startDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val endDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val action = organizationPaymentsTQ
      .filter(_.organizationId === organizationId)
      .filter(_.timestamp >= startDT)
      .filter(_.timestamp < endDT)
      .result

    db.run(action)
      .andThen { case Success(orgEvents) =>
        logger.debug(s"Pulled ${orgEvents.length} organization payments for org $organizationId")
      }
  }
}
