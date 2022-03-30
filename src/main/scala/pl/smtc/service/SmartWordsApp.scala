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
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.ember.server._

import scala.collection.mutable.ListBuffer

object SmartWordsApp extends IOApp {

  /**
   * Word category type.
   * Available options are: VERB, ADVERB, NOUN and ADJECTIVE
   */
  object Category extends Enumeration {
    type Category = Value
    val verb, adverb, noun, adjective, unknown = Value
    def fromString(string: String): Value =
      values.find(_.toString.toLowerCase() == string.toLowerCase()).getOrElse(unknown)
  }

  /**
   * Model class representing a single word
   * @param name smart word title/name
   * @param category word category
   * @param definition word correct definition
   */
  case class Word(name: String, category: Category.Value, definition: String)
  val testWordDB: ListBuffer[Word] = ListBuffer(
    Word("test", Category.verb, "definition-test"),
    Word("hello", Category.noun, "definition-hello"),
    Word("doing", Category.adjective, "definition-hello"))
  implicit val WordEncoder: Encoder[Word] = Encoder.instance {
    (word: Word) => json"""{"name": ${word.name}, "category": ${word.category.toString}, "description": ${word.definition}}"""
  }
  implicit val WordDecoder: Decoder[Word] = Decoder.instance {
    (input: HCursor) => for {
      name <- input.downField("name").as[String]
      category <- input.downField("category").as[String]
      definition <- input.downField("description").as[String]
    } yield {
      Word(name, Category.fromString(category), definition)
    }
  }

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

  /**
   * Routes (request -> response) for quiz endpoints/resources
   * <ul>
   *  <li>Start a new quiz: <u>POST</u> /quiz/start?size=10 -> RET: OK 200 + {id} / ERR 500</li>
   *  <li>Receive specific question: <u>GET</u> /quiz/{id}/question?no=1 -> RET: OK 200 + Round JSON / ERR 404</li>
   *  <li>Send question answer: <u>POST</u> /quiz/{id}/question?no=1&answer=A -> RET: OK 200 / ERR 404</li>
   *  <li>End quiz and get result: <u>GET</u> /quiz/{id}/stop -> RET: OK 200 / ERR 404</li>
   *  <li>Get answer details: <u>GET</u> /quiz/{id}/summary -> RET: OK 200 + Quiz JSON / ERR 404</li>
   * </ul>
   */
  def quizRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name => Ok(s"Hello, $name.")
    }
  }

  implicit val categoryParamDecoder: QueryParamDecoder[Category.Value] =
    QueryParamDecoder[String].map(categoryStr => Category.fromString(categoryStr))
  object OptionalCategoryParamMatcher extends OptionalQueryParamDecoderMatcher[Category.Value]("cat")

  /**
   * Routes (request -> response) for admin endpoints/resources
   * <ul>
   *  <li>Receive all words: <u>GET</u> /admin/words -> RET: OK 200 + ALL WORDS JSON / ERR 500</li>
   *  <li>Receive category-specific words: <u>GET</u> /admin/words?cat=adj -> RET: OK 200 + Word JSON / ERR 500</li>
   *  <li>Add a new word: <u>POST</u> /admin/words + Word JSON -> RET: OK 200 / ERR 500</li>
   *  <li>Delete word: <u>DELETE</u> /admin/words/{name} -> RET: OK 200 / ERR 404</li>
   *  <li>Update word: <u>PUT</u> /admin/words/{name} + Word JSON -> RET: OK 200 + Word JSON / ERR 404</li>
   * </ul>
   */
  def adminRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val wordDecoder: EntityDecoder[F, Word] = jsonOf[F, Word]
    HttpRoutes.of[F] {
      case GET -> Root / "words" :? OptionalCategoryParamMatcher(maybeCategory) =>
        maybeCategory match {
          case None =>
            Ok(testWordDB.toList.asJson)
          case Some(category) =>
            Ok(testWordDB.toList.filter(word => word.category.equals(category)).asJson)
        }
      case request@POST -> Root / "words" =>
        for {
          newWord <- request.as[Word]
          response <- Ok()
        } yield {
          testWordDB += newWord
          response
        }
      case request@PUT -> Root / "words" / name =>
        for {
          newWord <- request.as[Word]
        } yield {
          val nameIndex = testWordDB.indexWhere((word: Word) => word.name.equals(name))
          testWordDB += newWord
          Ok(newWord.asJson)
        }
      case DELETE -> Root / "words" / name =>
        val nameIndex = testWordDB.indexWhere((word: Word) => word.name.equals(name))
        if (nameIndex >= 0) {
          val removed = testWordDB.remove(nameIndex)
          Ok(removed.asJson)
        } else {
          NotFound(s"Word: ${name} not found.")
        }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val apis = Router(
      "/quiz" -> SmartWordsApp.quizRoutes[IO],
      "/admin" -> SmartWordsApp.adminRoutes[IO]
    ).orNotFound

    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"1234")
      .withHttpApp(apis)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
