package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe.jsonDecoder
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

  test("testGetRoutesReturnsOkResponseWhenStartQuizWithoutParams") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json =
      json"""[ { "id" : 99, "name" : "UNIT test QUIZ mode 1",
                 "description" : "this is a JSON for unit test and checking quiz mode logic", "deletable" : true,
                 "settings" : [
                   { "type" : "questions", "label" : "UT questions:", "details" : "value='1' min='1' max='1'" }
                 ]
               },
               { "id" : 17, "name" : "second MODE for UNIT tests",
                 "description" : "another unit test mode", "deletable" : false,
                 "settings" : [
                   { "type" : "questions", "label" : "specify number of questions:", "details" : "value='5' min='1' max='10'" },
                   { "type" : "languages", "label" : "supported langs:", "details" : "en es!" }
                 ]
               }
             ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsOkStatusAndEmptyBodyWhenGetModesRequestAndNoDatabaseInit") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    assert(response.get.as[Json].unsafeRunSync === json"""[]""")
  }
}
