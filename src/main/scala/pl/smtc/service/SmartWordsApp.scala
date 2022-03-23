package pl.smtc.service

import cats._
import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import org.http4s.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import org.http4s.dsl.impl._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.ember.server._

object SmartWordsApp extends IOApp {

  object Category extends Enumeration {
    type Category = Value
    val verb, adverb, noun, adjective = Value
  }
  case class Word(name: String, category: Category.Value, definition: String)
  case class Round(word: Word, options: List[String], answer: String)
  case class Quiz(rounds: Map[Round, Boolean], score: Int)

  /*
   * API ENDPOINTS + APPLICATION FLOW
   * - POST: start new quiz (how many questions/answers immediately) -> OK / error (to little words)
   * - GET: questionNo (1 ... 10) -> OK + JSON with word and 4 options
   * - POST: questionNo/answerNo -> OK + JSON correct/incorrect answer
   * - GET: result -> OK + JSON with score %
   * - GET: summary -> OK + list of all questions with answers and correct definitions
   *
   * ADMIN COMMANDS
   * - GET: all words
   * - GET: all words of selected category
   * - POST: add a new word
   * - DELETE: remove a word
   * - PUT: modify a word
   */

  var helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
