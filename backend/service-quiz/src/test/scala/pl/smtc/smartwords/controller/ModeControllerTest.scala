package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s._
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._

class ModeControllerTest extends AnyFunSuite {

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/non-existing"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

}
