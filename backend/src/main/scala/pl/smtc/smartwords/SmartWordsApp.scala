package pl.smtc.smartwords

import cats._
import cats.effect._
import cats.implicits._
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

import java.util.UUID
import scala.collection.mutable
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

  private def generateRound(): Round = {
    val word: Word = wordsDB.getWord(Random.nextInt(wordsDB.getWords.length)).get
    Round(word, generateOptions(word.definition, word.category), None)
  }

  private def generateOptions(correctDefinition: String, category: Category.Value): List[String] = {
    val incorrectOptions: List[String] = Random.shuffle(wordsDB.getWordsByCategory(category).map(_.definition))
    val options: List[String] = incorrectOptions.take(3) :+ correctDefinition
    Random.shuffle(options)
  }

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
            val correctDefinition: String = quiz.rounds(questionNo.toInt).word.definition
            val selectedDefinition: String = quiz.rounds(questionNo.toInt).options(answerNo.toInt)
            val isCorrect = correctDefinition.equals(selectedDefinition)
            quiz.rounds(questionNo.toInt).correct = Option(isCorrect)
            Ok(isCorrect.toString)
        }
      case GET -> Root / UUIDVar(quizId) / "stop" =>
        activeQuizzes.get(quizId) match {
          case None =>
            NotFound("Specified quiz does not exist")
          case Some(quiz) =>
            val okCount: Int = quiz.rounds.count(round => round.correct.exists(isCorrect => isCorrect))
            val percent: Float = okCount.toFloat / quiz.rounds.length
            activeQuizzes.remove(quizId)
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
  def adminRoutes[F[_] : Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    implicit val wordDecoder: EntityDecoder[F, Word] = jsonOf[F, Word]
    HttpRoutes.of[F] {
      case GET -> Root / "words" :? OptionalCategoryParamMatcher(maybeCategory) =>
        maybeCategory match {
          case None =>
            Ok(wordsDB.getWords.asJson)
          case Some(category) =>
            Ok(wordsDB.getWordsByCategory(category).asJson)
        }
      case request@POST -> Root / "words" =>
        for {
          newWord <- request.as[Word]
          response <- {
            if (wordsDB.addWord(newWord)) {
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
            val nameIndex = wordsDB.getWords.indexWhere((word: Word) => word.name.equals(name))
            val result = wordsDB.updateWord(nameIndex, newWord)
            if (result) {
              Ok(s"Updated word \"$name\".")
            } else {
              NotFound(s"Word \"$name\" not found in DB.")
            }
          }
        } yield response
      case DELETE -> Root / "words" / name =>
        val deleteWord = wordsDB.getWordByName(name)
        deleteWord match {
          case None => NotFound(s"Word \"$name\" not found in DB.")
          case Some(word) => {
            wordsDB.removeWord(word)
            Ok(word.asJson)
          }
        }
    }
  }

  val wordsDB: WordsDatabase = new WordsDatabase()
  val activeQuizzes: mutable.Map[UUID, Quiz] = mutable.Map()

  override def run(args: List[String]): IO[ExitCode] = {
    if (wordsDB.initDatabase()) {
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
