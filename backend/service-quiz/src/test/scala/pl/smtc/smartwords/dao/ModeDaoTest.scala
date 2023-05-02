package pl.smtc.smartwords.dao

import io.circe.{Decoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.Mode

class ModeDaoTest extends AnyFunSuite {

  test("testGetModeDecoder") {
    val decoderUnderTest: Decoder[Mode] = ModeDao.getModeDecoder
    val sourceJson = createModeJson(id = 0, name = "test-mode", description = "test-mode-description")
    val decodedValue = decoderUnderTest.decodeJson(sourceJson)
    assert(decodedValue.left.toOption === None)
  }

  private def createModeJson(id: Int, name: String, description: String): Json = {
    Json.obj(
      ("id", Json.fromInt(id)),
      ("name",  Json.fromString(name)),
      ("description",  Json.fromString(description)),
      ("deletable",  Json.fromBoolean(true)),
      ("settings", Json.arr(createSettingJson()))
    )
  }

  private def createSettingJson(): Json = {
    Json.obj(
      ("type", Json.fromString("test-setting")),
      ("label", Json.fromString("test-setting-label")),
      ("details", Json.fromString("test-setting-details"))
    )
  }

}
