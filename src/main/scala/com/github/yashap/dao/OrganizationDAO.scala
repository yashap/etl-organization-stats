package com.github.yashap.dao

import com.github.yashap.model.Organization
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success

class OrganizationDAO(val dbConfig: DatabaseConfig[JdbcProfile]) extends BaseDAO {
  import driver.api._

  val tableName = "organization"

  private class OrganizationTable(tag: Tag) extends Table[Organization](tag, tableName) {
    def organizationId = column[Long]("organization_id", O.PrimaryKey)
    def organizationName = column[String]("organization_name")

    def * = (organizationId, organizationName) <> (Organization.tupled, Organization.unapply)
  }

  private val organizationEventsTQ = TableQuery[OrganizationTable]

  def getAll()(implicit ec: ExecutionContext): Future[Seq[Organization]] = {
    val action = organizationEventsTQ.result

    db.run(action)
      .andThen { case Success(orgs) =>
        logger.debug(s"Pulled ${orgs.length} organizations")
      }
  }
}
