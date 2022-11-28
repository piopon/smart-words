package pl.smtc.smartwords.controller

import cats.effect._
import org.http4s._
import org.http4s.dsl._
import pl.smtc.smartwords.service._

class ModeController {

  /**
   * Routes (request -> response) for quiz modes
   * <ul>
   *  <li>Get all supported modes: <u>GET</u> /modes -> RET: OK 200 + Mode JSON</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: ModeService = new ModeService()
    val dsl = Http4sDsl[IO]; import dsl._
    HttpRoutes.of[IO] {
      case GET -> Root =>
        service.getQuizModes
    }
  }
}
