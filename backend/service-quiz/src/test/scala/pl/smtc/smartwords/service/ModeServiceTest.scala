package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database.ModeDatabase

class ModeServiceTest extends AnyFunSuite {

  test("testGetSupportedSettings") {
    val serviceUnderTest: ModeService = new ModeService(createTestDatabase())
    val res: Json = serviceUnderTest.getSupportedSettings.flatMap(_.as[Json]).unsafeRunSync()
    val expected: Json = json"""[{"type": "questions", "label": "", "details": ""},
                                 {"type": "languages", "label": "", "details": ""}]"""
    assert(res === expected)
  }

  private def createTestDatabase(): ModeDatabase = {
    val database: ModeDatabase = new ModeDatabase(databaseFile = "test-modes.json")
    if (!database.loadDatabase()) {
      throw new InstantiationException("Cannot load test modes database...")
    }
    database
  }
}
