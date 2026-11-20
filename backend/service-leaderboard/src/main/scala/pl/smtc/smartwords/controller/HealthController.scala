package pl.smtc.smartwords.controller

import cats.effect._
import org.http4s._
import org.http4s.dsl._
import pl.smtc.smartwords.service._

class HealthController(serviceName: String = "LEADERBOARD") {

  /**
   * Routes (request -> response) for health endpoints/resources.
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: HealthService = new HealthService(serviceName)
    val dsl = Http4sDsl[IO]; import dsl._
    HttpRoutes.of[IO] {
      case GET -> Root => service.checkHealth()
    }
  }
}
