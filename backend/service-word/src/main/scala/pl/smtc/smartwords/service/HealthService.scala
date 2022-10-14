package pl.smtc.smartwords.service

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._

class HealthService {

  private val statusOk = "OK"

  def checkHealth(): IO[Response[IO]] = {
    val status: String = checkStatus()
    if (status.startsWith(statusOk)) {
      Ok(s"Service: WORD - status: ${status}")
    } else {
      InternalServerError(s"Service: WORD - status: ${status}")
    }
  }

  private def checkStatus(): String = {
    // add custom defined checks here
    statusOk
  }
}
