package pl.smtc.smartwords.service

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._

class HealthService(serviceName: String = "QUIZ", statusSupplier: () => String = () => "OK") {

  private val statusOk = "OK"

  /**
   * Method used to check current health status of quiz service
   * @return response of status 200 if health ok, otherwise status 500 will be returned
   */
  def checkHealth(): IO[Response[IO]] = {
    val status: String = statusSupplier()
    if (status.startsWith(statusOk)) {
      Ok(s"Service: $serviceName - status: $status")
    } else {
      InternalServerError(s"Service: $serviceName - status: $status")
    }
  }
}
