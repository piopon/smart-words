package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s._
import org.http4s.circe._
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class ModeControllerTest extends AnyFunSuite {

  test("testGetRoutesReturnsNoneForNonExistingEndpoint") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/non-existing"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

  test("testGetRoutesReturnsOkResponseWhenGetModesRequest") {
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

  test("testGetRoutesReturnsOkResponseWhenGetSupportedSettingsRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/settings"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json =
      json"""[ { "type" : "questions", "label" : "", "details" : "" },
               { "type" : "languages", "label" : "", "details" : "" } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsOkStatusEvenWhenGetSupportedSettingsRequestAndNoDatabaseInit") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/settings"
    val request: Request[IO] = Request(Method.GET, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json =
      json"""[ { "type" : "questions", "label" : "", "details" : "" },
               { "type" : "languages", "label" : "", "details" : "" } ]"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsOkStatusWhenAddingNewModesRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/"
    val request: Request[IO] = Request(Method.POST, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    val expected: Json =json"""{ "id" : 0, "name" : "", "description" : "", "deletable" : true, "settings" : [] }"""
    assert(response.get.as[Json].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsOkStatusWhenUpdatingExistingModeRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/0"
    val requestBody: Json =
      json"""{ "id" : 99, "name" : "UNIT test QUIZ mode 1",
                 "description" : "this is a JSON for unit test and checking quiz mode logic", "deletable" : false,
                 "settings" : [
                   { "type" : "questions", "label" : "UT questions:", "details" : "value='1' min='1' max='1'" }
                 ]
             }"""
    val request: Request[IO] = Request(Method.PUT, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    assert(response.get.as[String].unsafeRunSync === "Updated quiz mode ID: 0")
  }

  test("testGetRoutesReturnsNotFoundWhenUpdatingNotExistingModeRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/100"
    val requestBody: Json =
      json"""{ "id" : 100, "name" : "UNIT test QUIZ mode 1",
                 "description" : "this is a JSON for unit test and checking quiz mode logic", "deletable" : true,
                 "settings" : [
                   { "type" : "questions", "label" : "UT questions:", "details" : "value='1' min='1' max='1'" }
                 ]
             }"""
    val request: Request[IO] = Request(Method.PUT, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.NotFound)
    val expected: String = "Cannot find mode with ID: 100, or mode cannot be updated with initial settings removal"
    assert(response.get.as[String].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsNotFoundWhenUpdatingNotDeletableModeRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val mode: Mode = modeDatabase.addMode()
    mode.deletable = false
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/${mode.id}"
    val requestBody: Json =
      json"""{ "id" : 22, "name" : "UNIT test QUIZ mode 1",
                 "description" : "this is a JSON for unit test and checking quiz mode logic", "deletable" : false,
                 "settings" : [
                   { "type" : "questions", "label" : "UT questions:", "details" : "value='1' min='1' max='1'" }
                 ]
             }"""
    val request: Request[IO] = Request(Method.PUT, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.NotFound)
    val expected: String = s"Cannot find mode with ID: ${mode.id}, or mode cannot be updated with initial settings removal"
    assert(response.get.as[String].unsafeRunSync === expected)
  }

  test("testGetRoutesReturnsNothingWhenUpdatingModeWithInvalidId") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/abc"
    val requestBody: Json =
      json"""{ "id" : 0, "name" : "UNIT test QUIZ mode 1",
                 "description" : "this is a JSON for unit test and checking quiz mode logic", "deletable" : true,
                 "settings" : []
             }"""
    val request: Request[IO] = Request(Method.PUT, Uri.unsafeFromString(endpoint)).withEntity(requestBody)
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }

  test("testGetRoutesReturnsOkStatusWhenDeletingExistingModeRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val mode: Mode = modeDatabase.addMode()
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/${mode.id}"
    val request: Request[IO] = Request(Method.DELETE, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.Ok)
    assert(response.get.as[String].unsafeRunSync === s"Deleted quiz mode ID: ${mode.id}")
  }

  test("testGetRoutesReturnsNotFoundWhenDeletingNotExistingModeRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/100"
    val request: Request[IO] = Request(Method.DELETE, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.NotFound)
    assert(response.get.as[String].unsafeRunSync === s"Cannot find mode with ID: 100, or mode is not deletable")
  }

  test("testGetRoutesReturnsNotFoundWhenDeletingNotDeletableModeRequest") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val mode: Mode = modeDatabase.addMode()
    mode.deletable = false
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/${mode.id}"
    val request: Request[IO] = Request(Method.DELETE, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.nonEmpty)
    val actualStatus: Status = response.get.status
    assert(actualStatus === Status.NotFound)
    assert(response.get.as[String].unsafeRunSync === s"Cannot find mode with ID: ${mode.id}, or mode is not deletable")
  }

  test("testGetRoutesReturnsNothingWhenDeletingModeWithInvalidId") {
    val modeDatabase: ModeDatabase = new ModeDatabase("test-mode-controller-crud.json")
    assert(modeDatabase.loadDatabase())
    val controllerUnderTest: ModeController = new ModeController(modeDatabase)
    val endpoint: String = s"/abc"
    val request: Request[IO] = Request(Method.DELETE, Uri.unsafeFromString(endpoint))
    val response: Option[Response[IO]] = controllerUnderTest.getRoutes.run(request).value.unsafeRunSync()
    assert(response.isEmpty)
  }
}
