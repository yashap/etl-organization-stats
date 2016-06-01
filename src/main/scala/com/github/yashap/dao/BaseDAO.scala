package com.github.yashap.dao

import com.github.yashap.model.{SocialNetworkType, PaymentProcessor}
import com.github.yashap.model.PaymentProcessor._
import com.github.yashap.model.SocialNetworkType._
import com.typesafe.scalalogging.LazyLogging
import slick.driver.JdbcProfile

import java.time.{LocalDate, LocalDateTime}
import java.sql.{Date, Timestamp}
import java.time.format.DateTimeFormatter

trait BaseDAO extends HasDatabaseConfig[JdbcProfile] with LazyLogging {
  import driver.api._

  protected implicit val localDateColumnType = MappedColumnType.base[LocalDate, Date](
    ld => Date.valueOf(ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))),
    d => d.toLocalDate
  )

  protected implicit val localDateTimeColumnType = MappedColumnType.base[LocalDateTime, Timestamp](
    ldt => Timestamp.valueOf(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
    ts => ts.toLocalDateTime
  )

  protected implicit val paymentProcessorColumnType = MappedColumnType.base[PaymentProcessor, String](
    e => e.toString,
    s => PaymentProcessor.withName(s)
  )

  protected implicit val socialNetworkTypeColumnType = MappedColumnType.base[SocialNetworkType, String](
    e => e.toString,
    s => SocialNetworkType.withName(s)
  )
}
