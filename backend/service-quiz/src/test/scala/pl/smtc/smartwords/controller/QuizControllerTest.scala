package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s._
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.client._
import pl.smtc.smartwords.database._

import java.util.UUID

class QuizControllerTest extends AnyFunSuite {

  private val uuidRegex: String = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/non-existing"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

  test("testGetRoutesReturnsOkResponseWhenStartQuizWithoutParams") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/start"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody.matches(uuidRegex))
  }

  test("testGetRoutesReturnsOkResponseWhenStartQuizWithParams") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/start?size=13&mode=3&lang=pl"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody.matches(uuidRegex))
  }

  test("testGetRoutesReturnsServiceUnavailableWhenWordServiceIsNotAlive") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest(alive = false)
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/start?size=13&mode=3&lang=pl"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.ServiceUnavailable)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody === "Cannot start quiz. Service: WORD - not available.")
  }

  test("testGetRoutesReturnsBadRequestWhenWordServiceCannotGiveRandomWord") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest(wordFail = true)
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/start?size=13&mode=3&lang=pl"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody === "Cannot start quiz: Invalid input parameter(s) - getRandomWord error!")
  }

  test("testGetRoutesReturnsBadRequestWhenWordServiceCannotGiveCategoryWords") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest(categoryFail = true)
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/start?size=13&mode=3&lang=pl"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody === "Cannot start quiz: Invalid input parameter(s) - getWordsByCategory error!")
  }

  test("testGetRoutesReturnsStatusOkWhenGettingCorrectQuestionNo") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val quizUuid: String = controllerUnderTest.getRoutes.run(Request(Method.POST, Uri.unsafeFromString(s"/start")))
                                                        .value.unsafeRunSync()
                                                        .get.as[String].unsafeRunSync
    val endpoint: String = s"/${UUID.fromString(quizUuid)}/question/1"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val actualBody: Json = response.get.as[Json].unsafeRunSync
    assert(actualBody.hcursor.downField("word").as[String] match {
      case Right(s) => s.startsWith("word-pl")
      case Left(_) => false
    })
  }

  test("testGetRoutesReturnsBadRequestWhenGettingQuestionForNotExistingQuiz") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val endpoint: String = s"/${UUID.randomUUID().toString}/question/1"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.NotFound)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody === "Specified quiz does not exist")
  }
}
