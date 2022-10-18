package pl.smtc.smartwords.controller

import cats.effect._
import org.http4s._
import org.http4s.dsl._
import pl.smtc.smartwords.service._

class HealthController {

  /**
   * Routes (request -> response) for health endpoints/resources
   * <ul>
   *  <li>Check service health: <u>GET</u> /health -> RET: OK 200 / 500 + HEALTH STATUS JSON</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: HealthService = new HealthService()
    val dsl = Http4sDsl[IO]; import dsl._
    HttpRoutes.of[IO] {
      case GET -> Root => service.checkHealth()
    }
  }
}
