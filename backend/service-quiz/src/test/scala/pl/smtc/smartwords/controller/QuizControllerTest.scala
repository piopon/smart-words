package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s._
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.client._
import pl.smtc.smartwords.database._

class QuizControllerTest extends AnyFunSuite {

  private val uuidRegex: String = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/non-existing"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

  test("testGetRoutesReturnsCorrectResponseOnEndpointStartWithoutParams") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/start"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    assert(response.get.status === Status.Ok)
    assert(response.get.as[String].unsafeRunSync.matches(uuidRegex))
  }

  test("testGetRoutesReturnsCorrectResponseOnEndpointStartWithParams") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/start?size=13&mode=3&lang=pl"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    assert(response.get.status === Status.Ok)
    assert(response.get.as[String].unsafeRunSync.matches(uuidRegex))
  }

  test("testGetRoutesReturnsServiceUnavailableWhenWordServiceIsNotAlive") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest(alive = false)
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/start?size=13&mode=3&lang=pl"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    assert(response.get.status === Status.ServiceUnavailable)
    assert(response.get.as[String].unsafeRunSync === "Cannot start quiz. Service: WORD - not available.")
  }

  test("testGetRoutesReturnsBadRequestWhenWordServiceCannotGiveRandomWord") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest(wordFail = true)
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/start?size=13&mode=3&lang=pl"))
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
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/start?size=13&mode=3&lang=pl"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.BadRequest)
    val actualBody: String = response.get.as[String].unsafeRunSync
    assert(actualBody === "Cannot start quiz: Invalid input parameter(s) - getWordsByCategory error!")
  }
}
