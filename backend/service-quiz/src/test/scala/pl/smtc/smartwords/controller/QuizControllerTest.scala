package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.{Method, Request, Response, Uri}
import org.scalatest.funsuite.AnyFunSuite

class QuizControllerTest extends AnyFunSuite {

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val controllerUnderTest: QuizController = new QuizController()
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString("/non-existing"))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }
}
