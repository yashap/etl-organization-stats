package com.github.yashap.app

import com.github.yashap.dao._
import com.github.yashap.config._
import com.github.yashap.pipeline.Pipeline
import com.typesafe.config.Config
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait ObjectGraph {

  val config: Config
  lazy val jobTimeout = config.getFiniteDuration("job.timeout")
  lazy val dwConfig = config.getConfig("dataWarehouse")

  // Readers
  lazy val readerConnectionPool = DatabaseConfig.forConfig[JdbcProfile]("reader", dwConfig)
  lazy val orgDAO = new OrganizationDAO(readerConnectionPool)
  lazy val orgUserEventDAO = new OrganizationUserEventDAO(readerConnectionPool)
  lazy val orgPaymentDAO = new OrganizationPaymentDAO(readerConnectionPool)
  lazy val orgEventDAO = new OrganizationEventDAO(readerConnectionPool)

  // Writers
  lazy val writerConnectionPool = DatabaseConfig.forConfig[JdbcProfile]("writer", dwConfig)
  lazy val orgStatDAO = new OrganizationStatisticDAO(writerConnectionPool)

  // Pipeline
  lazy val pipeline = new Pipeline(orgDAO, orgUserEventDAO, orgPaymentDAO, orgEventDAO, orgStatDAO)

}
