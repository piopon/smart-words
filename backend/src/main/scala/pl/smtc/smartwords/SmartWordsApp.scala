package pl.smtc.smartwords

import cats.effect._
import com.comcast.ip4s._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.middleware._
import org.http4s.ember.server._
import pl.smtc.smartwords.controller._
import pl.smtc.smartwords.database._

object SmartWordsApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val wordDB: WordDatabase = new WordDatabase()
    val wordController: WordController = new WordController(wordDB)
    val quizController: QuizController = new QuizController(wordDB)

    if (wordDB.loadDatabase()) {
      val apis = Router(
        "/quiz" -> CORS(quizController.getRoutes),
        "/words" -> CORS(wordController.getRoutes)
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
