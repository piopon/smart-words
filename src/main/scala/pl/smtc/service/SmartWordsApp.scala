package pl.smtc.service

import cats._
import cats.effect._
import cats.implicits._
import com.comcast.ip4s._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.literal._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import org.http4s.dsl.impl._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.ember.server._

object SmartWordsApp extends IOApp {

  /**
   * Word category type.
   * Available options are: VERB, ADVERB, NOUN and ADJECTIVE
   */
  object Category extends Enumeration {
    type Category = Value
    val verb, adverb, noun, adjective = Value
  }

  /**
   * Model class representing a single word
   * @param name smart word title/name
   * @param category word category
   * @param definition word correct definition
   */
  case class Word(name: String, category: Category.Value, definition: String)

  /**
   * Model class representing a single round of a quiz
   * @param word selected word which a user has to figure out
   * @param options the list of possible answers (will be matched from word category)
   * @param answer the selected answer (A, B, C, D, etc.)
   */
  case class Round(word: Word, options: List[String], answer: String)

  /**
   * Model class representing a complete quiz containing several rounds (smart words questions)
   * @param rounds a collection of rounds/questions with an indicator if already answered
   * @param score current correct answers counter
   */
  case class Quiz(rounds: Map[Round, Boolean], score: Int)

  var helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"1234")
      .withHttpApp(helloWorldService)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
