package pl.smtc.smartwords.service

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._

class HealthService {

  private val statusOk = "OK"

  /**
   * Method used to check current health status of quiz service
   * @return response of status 200 if health ok, otherwise status 500 will be returned
   */
  def checkHealth(): IO[Response[IO]] = {
    val status: String = checkStatus()
    if (status.startsWith(statusOk)) {
      Ok(s"Service: QUIZ - status: $status")
    } else {
      InternalServerError(s"Service: QUIZ - status: $status")
    }
  }

  /**
   * Method used to check service status (currently always returns status OK)
   * @return String containing service status ("OK" if service is working correctly)
   */
  private def checkStatus(): String = {
    // add custom defined checks here
    statusOk
  }
}
