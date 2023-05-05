package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database.ModeDatabase
import pl.smtc.smartwords.model._

class ModeServiceTest extends AnyFunSuite {

  test("testGetSupportedSettings") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: Json = serviceUnderTest.getSupportedSettings.flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{"type": "questions", "label": "", "details": ""},
                                 {"type": "languages", "label": "", "details": ""}]"""
    assert(res === expected)
  }

  test("testDeleteQuizModeOk") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: String = serviceUnderTest.deleteQuizMode(0).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Deleted quiz mode ID: 0")
  }

  test("testDeleteQuizModeNok") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: String = serviceUnderTest.deleteQuizMode(1).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Cannot find mode with ID: 1, or mode is not deletable")
  }

  test("testDeleteQuizModeNotDeletable") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: String = serviceUnderTest.deleteQuizMode(15).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Cannot find mode with ID: 15, or mode is not deletable")
  }

  test("testUpdateQuizModeOkWhenDeletable") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val updatedMode: Mode = Mode(0, "UPDATED MODE", "Hello from unit test: UPDATE MODE", List(), deletable = true)
    val res: String = serviceUnderTest.updateQuizMode(0, updatedMode).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Updated quiz mode ID: 0")
  }

  test("testUpdateQuizModeOkWhenSettingsOk") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val settings: List[Setting] = List(Setting(Kind.questions, "updated label:", "min='10' value='15' max='20'"),
                                       Setting(Kind.languages, "languages label:", "pl"))
    val updatedMode: Mode = Mode(1, "UPDATED MODE", "Hello from unit test: UPDATE MODE", settings, deletable = true)
    val res: String = serviceUnderTest.updateQuizMode(1, updatedMode).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Updated quiz mode ID: 1")
  }

  test("testUpdateQuizModeNokWhenSettingsNok") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val updatedMode: Mode = Mode(1, "UPDATED MODE", "Hello from unit test: UPDATE MODE", List(), deletable = true)
    val res: String = serviceUnderTest.updateQuizMode(1, updatedMode).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Cannot find mode with ID: 1, or mode cannot be updated with initial settings removal")
  }

  test("testUpdateQuizModeNokWhenNotExisting") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val updatedMode: Mode = Mode(15, "UPDATED MODE", "Hello from unit test: UPDATE MODE", List(), deletable = true)
    val res: String = serviceUnderTest.updateQuizMode(15, updatedMode).flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Cannot find mode with ID: 15, or mode cannot be updated with initial settings removal")
  }

  test("testGetQuizModes") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: Json = serviceUnderTest.getQuizModes.flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json =
      json"""[ { "id" : 0, "name" : "first-mode-name", "description" : "Description for mode 1", "deletable" : true,
                 "settings" : [ { "type" : "languages", "label" : "lbl:", "details" : "en es!" },
                                { "type" : "questions", "label" : "lbl:", "details" : "value='5' min='1' max='10'" } ]
               },
               { "id" : 1, "name" : "second-mode-name", "description" : "Description for mode 2", "deletable" : false,
                 "settings" : [ { "type" : "languages", "label" : "lbl:", "details" : "en es!" },
                                { "type" : "questions", "label" : "lbl:", "details" : "value='5' min='1' max='10'" } ]
               } ]"""
    assert(res === expected)
  }

  private def createTestDatabase(): ModeDatabase = {
    val database: ModeDatabase = new ModeDatabase(databaseFile = "test-mode-service-crud.json")
    val settings: List[Setting] = List(Setting(Kind.languages, "lbl:", "en es!"),
                                       Setting(Kind.questions, "lbl:", "value='5' min='1' max='10'"))
    val firstModeId: Int = database.addMode().id
    database.updateMode(firstModeId, Mode(0, "first-mode-name", "Description for mode 1", settings, deletable = true))
    val secondModeId: Int = database.addMode().id
    database.updateMode(secondModeId, Mode(0, "second-mode-name", "Description for mode 2", settings, deletable = false))
    database
  }
}
