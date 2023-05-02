package pl.smtc.smartwords.dao

import io.circe.{Decoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.Mode

class ModeDaoTest extends AnyFunSuite {

  private def createTestMode(id: Int, name: String, description: String): Json = {
    Json.obj(
      ("id", Json.fromInt(id)),
      ("name",  Json.fromString(name)),
      ("description",  Json.fromString(description)),
      ("deletable",  Json.fromBoolean(true)),
      ("settings", Json.arr(createTestSetting()))
    )
  }

  private def createTestSetting(): Json = {
    Json.obj(
      ("type", Json.fromString("test-setting")),
      ("label", Json.fromString("test-setting-label")),
      ("details", Json.fromString("test-setting-details"))
    )
  }

}
