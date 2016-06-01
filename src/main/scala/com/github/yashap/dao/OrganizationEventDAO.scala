package com.github.yashap.dao

import com.github.yashap.model.OrganizationEvent
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success
import java.time.{LocalDateTime, LocalDate, LocalTime}

class OrganizationEventDAO(val dbConfig: DatabaseConfig[JdbcProfile]) extends BaseDAO {
  import driver.api._

  val tableName = "organization_event"

  private class OrganizationEventTable(tag: Tag) extends Table[OrganizationEvent](tag, tableName) {
    def id = column[String]("id", O.PrimaryKey)
    def timestamp = column[LocalDateTime]("timestamp")
    def organizationId = column[Long]("organization_id")
    def event = column[String]("event")

    def * = (
      id, timestamp, organizationId, event
    ) <> (OrganizationEvent.tupled, OrganizationEvent.unapply)
  }

  private val organizationEventsTQ = TableQuery[OrganizationEventTable]

  def get(organizationId: Long, start: LocalDate, end: LocalDate)
    (implicit ec: ExecutionContext): Future[Seq[OrganizationEvent]] = {

    val startDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val endDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val action = organizationEventsTQ
      .filter(_.organizationId === organizationId)
      .filter(_.timestamp >= startDT)
      .filter(_.timestamp < endDT)
      .result

    db.run(action)
      .andThen { case Success(orgEvents) =>
        logger.debug(s"Pulled ${orgEvents.length} organization events for org $organizationId")
      }
  }
}
