package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database.ModeDatabase
import pl.smtc.smartwords.model.{Kind, Mode, Setting}

class ModeServiceTest extends AnyFunSuite {

  test("testGetSupportedSettings") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: Json = serviceUnderTest.getSupportedSettings.flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{"type": "questions", "label": "", "details": ""},
                                 {"type": "languages", "label": "", "details": ""}]"""
    assert(res === expected)
  }

  private def createTestDatabase(): ModeDatabase = {
    val database: ModeDatabase = new ModeDatabase(databaseFile = "test-mode-service-crud.json")
    val settings: List[Setting] = List(Setting(Kind.languages, "languages label:", "en es!"),
                                       Setting(Kind.questions, "questions label:", "value='5' min='1' max='10'"))
    val firstModeId: Int = database.addMode().id
    database.updateMode(firstModeId, Mode(0, "first-mode-name", "Description for mode 1", settings, deletable = true))
    val secondModeId: Int = database.addMode().id
    database.updateMode(secondModeId, Mode(0, "first-mode-name", "Description for mode 1", settings, deletable = false))
    database
  }
}
