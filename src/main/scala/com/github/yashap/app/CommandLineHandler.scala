package com.github.yashap.app

import com.github.yashap.app.cli.CommandLineParser
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.reflectiveCalls
import java.time.LocalDateTime

import scala.util.{Failure, Success, Try}

object CommandLineHandler extends App with ObjectGraph with LazyLogging {
  val config = ConfigFactory.load("dev")
  val cliArgs = CommandLineParser.parse(args)

  logger.info(s"Running Organization Statistic job with args $cliArgs")

  val jobRun: Future[Int] = pipeline
    .run(cliArgs.runDate)
    .andThen { case _ =>
      readerConnectionPool.db.close()
      writerConnectionPool.db.close()
    }

  val recordsSaved: Try[Int] = Try(Await.result(jobRun, jobTimeout))

  recordsSaved match {
    case Success(n) =>
      logger.info(s"Organization Statistics job completed successfully, $n statistics saved")
    case Failure(t) =>
      logger.error("Job failed", t)
  }

}
