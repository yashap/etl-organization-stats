package com.github.yashap.dao

import com.github.yashap.model._
import com.github.yashap.model.SocialNetworkType.SocialNetworkType
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success
import java.time.{LocalTime, LocalDateTime, LocalDate}

class OrganizationUserEventDAO(val dbConfig: DatabaseConfig[JdbcProfile]) extends BaseDAO {
  import driver.api._

  private class UserEventTable(tag: Tag) extends Table[UserEvent](tag, "user_event") {
    def id = column[String]("id", O.PrimaryKey)
    def timestamp = column[LocalDateTime]("timestamp")
    def userId = column[Long]("user_id")
    def event = column[String]("event")
    def socialNetworkType = column[Option[SocialNetworkType]]("social_network_type")

    def * = (id, timestamp, userId, event, socialNetworkType) <> (UserEvent.tupled, UserEvent.unapply)
  }

  private class OrganizationUserTable(tag: Tag) extends Table[OrganizationUser](tag, "organization_user") {
    def organizationId = column[Long]("organization_id")
    def userId = column[Long]("user_id")

    def * = (organizationId, userId) <> (OrganizationUser.tupled, OrganizationUser.unapply)

    def pk = primaryKey(s"pk_organization_user", (organizationId, userId))
  }

  private val userEventsTQ = TableQuery[UserEventTable]
  private val organizationUserTQ = TableQuery[OrganizationUserTable]

  def get(organizationId: Long, start: LocalDate, end: LocalDate)
    (implicit ec: ExecutionContext): Future[Seq[OrganizationUserEvent]] = {

    val startDT = LocalDateTime.of(start, LocalTime.of(0, 0))
    val endDT = LocalDateTime.of(end.plusDays(1), LocalTime.of(0, 0))

    val action = userEventsTQ
      .filter(_.timestamp >= startDT)
      .filter(_.timestamp < endDT)
      .join(organizationUserTQ).on(_.userId === _.userId)
      .filter(_._2.organizationId === organizationId)
      .result

    db.run(action)
      .map { _.map { case (u, o) =>
          OrganizationUserEvent(u.timestamp, u.userId, u.event, u.socialNetworkType, o.organizationId)
      }}
      .andThen { case Success(orgUserEvents) =>
        logger.debug(s"Pulled ${orgUserEvents.length} organization payments for org $organizationId")
      }
  }
}
