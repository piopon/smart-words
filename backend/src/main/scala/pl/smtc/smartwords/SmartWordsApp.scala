package pl.smtc.smartwords

import cats._
import cats.effect._
import com.comcast.ip4s._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import io.circe.literal._
import org.http4s._
import org.http4s.dsl._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.ember.server._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.service._

import scala.util.Random

object SmartWordsApp extends IOApp {

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

  implicit val RoundEncoder: Encoder[Round] = Encoder.instance {
    (round: Round) => json"""{"word": ${round.word.name}, "options": ${round.options}}"""
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
  def quizRoutes: HttpRoutes[IO] = {
    val service: QuizService = new QuizService(quizDB, wordDB)
    val dsl = Http4sDsl[IO]
    import dsl._
    HttpRoutes.of[IO] {
      case POST -> Root / "start" :? OptionalQuizStartParamMatcher(maybeSize) =>
        service.startQuiz(maybeSize)
      case GET -> Root / UUIDVar(quizId) / "question" / questionNo =>
        service.getQuizQuestionNo(quizId, questionNo)
      case POST -> Root / UUIDVar(quizId) / "question" / questionNo / answerNo =>
        service.postQuizQuestionNo(quizId, questionNo, answerNo)
      case GET -> Root / UUIDVar(quizId) / "stop" =>
        quizDB.getQuiz(quizId) match {
          case None =>
            NotFound("Specified quiz does not exist")
          case Some(quiz) =>
            val okCount: Int = quiz.rounds.count(round => round.correct.exists(isCorrect => isCorrect))
            val percent: Float = okCount.toFloat / quiz.rounds.length
            quizDB.removeQuiz(quizId)
            Ok(percent.toString)
        }
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
  def adminRoutes: HttpRoutes[IO] = {
    val service: WordService = new WordService(wordDB)
    val dsl = Http4sDsl[IO]
    import dsl._
    implicit val wordDecoder: EntityDecoder[IO, Word] = jsonOf[IO, Word]
    HttpRoutes.of[IO] {
      case GET -> Root / "words" :? OptionalCategoryParamMatcher(maybeCategory) =>
        service.getWords(maybeCategory)
      case request@POST -> Root / "words" =>
        for {
          newWord <- request.as[Word]
          response <- service.addWord(newWord)
        } yield response
      case request@PUT -> Root / "words" / name =>
        for {
          newWord <- request.as[Word]
          response <- service.updateWord(name, newWord)
        } yield response
      case DELETE -> Root / "words" / name => service.deleteWord(name)
    }
  }

  val wordDB: WordDatabase = new WordDatabase()
  val quizDB: QuizDatabase = new QuizDatabase()

  override def run(args: List[String]): IO[ExitCode] = {
    if (wordDB.initDatabase()) {
      val apis = Router(
        "/quiz" -> SmartWordsApp.quizRoutes,
        "/admin" -> SmartWordsApp.adminRoutes
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
