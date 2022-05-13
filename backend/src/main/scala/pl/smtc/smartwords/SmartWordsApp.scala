package pl.smtc.smartwords

import cats.effect._
import com.comcast.ip4s._
import io.circe._
import io.circe.literal._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.ember.server._
import pl.smtc.smartwords.controller._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.service._

object SmartWordsApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val wordDB: WordDatabase = new WordDatabase()
    val wordController: WordController = new WordController(wordDB)
    val quizController: QuizController = new QuizController(wordDB)

    if (wordDB.initDatabase()) {
      val apis = Router(
        "/quiz" -> quizController.getRoutes,
        "/admin" -> wordController.getRoutes
      ).orNotFound

      EmberServerBuilder.default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"1234")
        .withHttpApp(apis)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } else {
      IO.canceled.as(ExitCode.Error)
    }
  }
}
