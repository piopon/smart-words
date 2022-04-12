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
import io.circe.parser._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.ember.server._

import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

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

  private def findWordsByCategory(category: Category.Value): List[Word] = {
    testWordDB.toList.filter(word => word.category.equals(category))
  }

  /**
   * Model class representing a single round of a quiz
   * @param word selected word which a user has to figure out
   * @param options the list of possible answers (will be matched from word category)
   * @param correct true if got correct answer, false otherwise. If no answer yet then None
   */
  case class Round(word: Word, options: List[String], correct: Option[Boolean])

  private def generateRound(): Round = {
    val word: Word = testWordDB(Random.nextInt(testWordDB.length))
    Round(word, generateOptions(word.definition, word.category), None)
  }

  private def generateOptions(correctDefinition: String, category: Category.Value): List[String] = {
    val incorrectOptions: List[String] = Random.shuffle(findWordsByCategory(category).map(_.definition))
    val options: List[String] = incorrectOptions.take(3) :+ correctDefinition
    Random.shuffle(options)
  }

  /**
   * Model class representing a complete quiz containing several rounds (smart words questions)
   * @param rounds a collection of rounds/questions
   * @param score current correct answers counter
   */
  case class Quiz(rounds: List[Round], score: Int)

  private def generateQuiz(size: Int): Quiz = {
    Quiz(List.fill(size)(generateRound()), 0)
  }

  object OptionalQuizStartParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("size")

  /**
   * Routes (request -> response) for quiz endpoints/resources
   * <ul>
   *  <li>Start a new quiz: <u>POST</u> /quiz/start?size=10 -> RET: OK 200 + {id} / ERR 500</li>
   *  <li>Receive specific question: <u>GET</u> /quiz/{id}/question/{no} -> RET: OK 200 + Round JSON / ERR 404</li>
   *  <li>Send question answer: <u>POST</u> /quiz/{id}/question/{no}/{answerNo} -> RET: OK 200 / ERR 404</li>
   *  <li>End quiz and get result: <u>GET</u> /quiz/{id}/stop -> RET: OK 200 / ERR 404</li>
   * </ul>
   */
  def quizRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case POST -> Root / "start" :? OptionalQuizStartParamMatcher(maybeSize) =>
        val newUuid: UUID = UUID.randomUUID()
        val size: Int = maybeSize match {
          case None => 10
          case Some(size) => size
        }
        activeQuizzes.put(newUuid, generateQuiz(size))
        Ok(newUuid.toString)
      case GET -> Root / UUIDVar(quizId) / "question" / questionNo =>
        activeQuizzes.get(quizId) match {
          case None =>
            NotFound("Specified quiz does not exist")
          case Some(quiz) =>
            Ok(quiz.rounds(questionNo.toInt).asJson)
        }
      case POST -> Root / UUIDVar(quizId) / "question" / questionNo / answerNo =>
        activeQuizzes.get(quizId) match {
          case None =>
            NotFound("Specified quiz does not exist")
          case Some(quiz) =>
            Ok("quiz ok.")
        }
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
            Ok(findWordsByCategory(category).asJson)
        }
      case request@POST -> Root / "words" =>
        for {
          newWord <- request.as[Word]
          response <- {
            val nameIndex = testWordDB.indexWhere((word: Word) => word.name.equals(newWord.name))
            if (nameIndex == -1) {
              testWordDB += newWord
              Ok(s"Added new word \"${newWord.name}\".")
            } else {
              Ok(s"Word \"${newWord.name}\" already defined.")
            }
          }
        } yield response
      case request@PUT -> Root / "words" / name =>
        for {
          newWord <- request.as[Word]
          response <- {
            val nameIndex = testWordDB.indexWhere((word: Word) => word.name.equals(name))
            if (nameIndex >= 0) {
              testWordDB.update(nameIndex, newWord)
              Ok(s"Updated word \"$name\".")
            } else {
              NotFound(s"Word \"$name\" not found in DB.")
            }
          }
        } yield response
      case DELETE -> Root / "words" / name =>
        val nameIndex = testWordDB.indexWhere((word: Word) => word.name.equals(name))
        if (nameIndex >= 0) {
          val removed = testWordDB.remove(nameIndex)
          Ok(removed.asJson)
        } else {
          NotFound(s"Word \"$name\" not found in DB.")
        }
    }
  }

  def initDatabase(): Boolean = {
    val fileStream = getClass.getResourceAsStream("/dictionary.json")
    val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
    decode[List[Word]](lines) match {
      case Left(fail) =>
        println(s"Invalid dictionary file. ${fail.getMessage}")
        false
      case Right(words) =>
        words.foreach(word => testWordDB += word)
        true
    }
  }

  val testWordDB: ListBuffer[Word] = ListBuffer()
  val activeQuizzes: mutable.Map[UUID, Quiz] = mutable.Map()

  override def run(args: List[String]): IO[ExitCode] = {
    if (initDatabase()) {
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
    } else {
      IO.canceled.as(ExitCode.Error)
    }
  }
}
