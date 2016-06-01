package com.github.yashap.dao

import com.github.yashap.model.OrganizationStatistic
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Success
import java.time.LocalDate

class OrganizationStatisticDAO(val dbConfig: DatabaseConfig[JdbcProfile]) extends BaseDAO {
  import driver.api._

  val tableName = "organization_statistic"

  private class OrganizationStatisticTable(tag: Tag) extends Table[OrganizationStatistic](tag, tableName) {
    def organizationId = column[Long]("organization_id")
    def day = column[LocalDate]("day")
    def statistic = column[String]("statistic")
    def value = column[Double]("value")

    def * = (organizationId, day, statistic, value) <> (OrganizationStatistic.tupled, OrganizationStatistic.unapply)

    def pk = primaryKey(s"pk_$tableName", (organizationId, day, statistic))
  }

  private val orgStatsTQ = TableQuery[OrganizationStatisticTable]

  def upsert(orgStats: Seq[OrganizationStatistic])(implicit ec: ExecutionContext): Future[Int] = {
    val action = DBIO.seq(orgStats.map(orgStatsTQ.insertOrUpdate): _*)

    db.run(action)
      .map(_ => orgStats.length)
      .andThen { case Success(statsSaved) =>
        logger.debug(s"Saved $statsSaved organization statistics")
      }
  }
}
