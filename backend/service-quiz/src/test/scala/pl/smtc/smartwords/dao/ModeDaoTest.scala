package pl.smtc.smartwords.dao

import io.circe.{Decoder, DecodingFailure, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.Mode

class ModeDaoTest extends AnyFunSuite {

  test("testGetModeDecoder") {
    val decoderUnderTest: Decoder[Mode] = ModeDao.getModeDecoder
    val sourceJson: Json = createModeJson(id = "0", name = "test-mode", description = "test-mode-description")
    val decodedValue: Decoder.Result[Mode] = decoderUnderTest.decodeJson(sourceJson)
    assert(decodedValue.left.toOption === None)
    assert(decodedValue.toOption !== None)
    val decodedMode: Mode = decodedValue.toOption.get
    assert(decodedMode.id === 0)
    assert(decodedMode.name === "test-mode")
    assert(decodedMode.description === "test-mode-description")
  }

  test("testGetModeDecoderFails") {
    val decoderUnderTest: Decoder[Mode] = ModeDao.getModeDecoder
    val sourceJson: Json = createModeJson(id = "aaa", "test-mode", "test-mode-description")
    val decodedValue: Decoder.Result[Mode] = decoderUnderTest.decodeJson(sourceJson)
    assert(decodedValue.left.toOption !== None)
    val decodeErr: DecodingFailure = decodedValue.left.toOption.get
    assert(decodeErr.toString() === "DecodingFailure at .id: Int")
    assert(decodedValue.toOption === None)
  }

  private def createModeJson(id: String, name: String, description: String): Json = {
    Json.obj(
      ("id", Json.fromString(id)),
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
