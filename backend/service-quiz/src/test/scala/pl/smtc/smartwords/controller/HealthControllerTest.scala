package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.{Method, Request, Response, Status, Uri}
import org.scalatest.funsuite.AnyFunSuite

class HealthControllerTest extends AnyFunSuite {

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val controllerUnderTest: HealthController = new HealthController()
    val endpoint: String = s"/non-existing"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }
}
