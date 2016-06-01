package com.github.yashap.app

import com.github.yashap.app.lambda.{ImmutableRequest, Request, Response}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.language.reflectiveCalls
import java.time.format.DateTimeFormatter

class LambdaHandler extends RequestHandler[Request,Response] with ObjectGraph with LazyLogging {
  val config: Config = ConfigFactory.load("production")
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  /**
   * Don't close DB connection pools after each request, AWS Lambda will keep runtime alive and just re-call
   * handleRequest to improve startup time, so closing connection pool after each request will cause failures
   */
  sys.addShutdownHook {
    readerConnectionPool.db.close()
    writerConnectionPool.db.close()
  }

  override def handleRequest(request: Request, context: Context): Response = {
    logger.info(s"Running event loading app with args $request")

    // Convenient way to handle errors parsing the request, by wrapping parsing in Future
    val jobRun: Future[Int] = Future(ImmutableRequest.from(request).get.runDate)
      .flatMap(pipeline.run)

    val recordsSaved: Try[Int] = Try(Await.result(jobRun, jobTimeout))

    recordsSaved match {
      case Success(n) =>
        logger.info(s"Organization Statistics job completed successfully, $n statistics saved")
        Response(200, s"Saved $n statistics")
      case Failure(t) =>
        logger.error("Job failed", t)
        Response(500, s"Job failed")
    }

  }

}
