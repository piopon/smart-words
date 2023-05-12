package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.{Method, Request, Response, Uri}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.client.WordServiceTest
import pl.smtc.smartwords.database.QuizDatabase

class QuizControllerTest extends AnyFunSuite {

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val quizDatabase: QuizDatabase = new QuizDatabase()
    val wordService: WordServiceTest = new WordServiceTest
    val controllerUnderTest: QuizController = new QuizController(quizDatabase, wordService)
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/non-existing"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }
}
