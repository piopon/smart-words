package pl.smtc.smartwords.controller

import cats.effect._
import org.http4s.circe._
import io.circe._
import org.http4s._
import org.http4s.dsl._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.service._

class ModeController(database: ModeDatabase) {

  implicit val ModeDecoder: Decoder[Mode] = ModeDao.getModeDecoder

  /**
   * Routes (request -> response) for quiz modes
   * <ul>
   *  <li>Get all supported quiz modes: <u>GET</u> /modes -> RET: OK 200 + Mode JSON</li>
   *  <li>Get all supported modes settings: <u>GET</u> /modes/settings -> RET: OK 200 + Setting JSON</li>
   *  <li>Create new quiz mode: <u>POST</u> /modes -> RET: OK 200 + ID | ERR 500</li>
   *  <li>Update new quiz mode: <u>PUT</u> /modes/{id} + JSON -> RET: OK 200 + JSON | ERR 500</li>
   * </ul>
   */
  def getRoutes: HttpRoutes[IO] = {
    val service: ModeService = new ModeService()
    val dsl = Http4sDsl[IO]; import dsl._
    implicit val modeDecoder: EntityDecoder[IO, Mode] = jsonOf[IO, Mode]
    HttpRoutes.of[IO] {
      case GET -> Root =>
        service.getQuizModes
      case GET -> Root / "settings" =>
        service.getSupportedSettings
      case POST -> Root =>
        for {
          response <- service.createQuizMode
        } yield response
      case request@PUT -> Root / IntVar(id) =>
        for {
          newMode <- request.as[Mode]
          response <- service.updateQuizMode(id, newMode)
        } yield response
    }
  }
}
