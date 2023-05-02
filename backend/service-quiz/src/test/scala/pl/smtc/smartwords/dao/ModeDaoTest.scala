package pl.smtc.smartwords.dao

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.{Kind, Mode, Setting}

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

  test("testGetModeEncoder") {
    val encoderUnderTest: Encoder[Mode] = ModeDao.getModeEncoder
    val sourceSetting: Setting = Setting(Kind.languages, "test-setting-label", "test-setting-details")
    val sourceMode: Mode = Mode(73, "diff-mode", "mode-desc", List(sourceSetting), deletable = true)
    val encodedValue: Json = encoderUnderTest.apply(sourceMode)
    val expectedValue: Json = createModeJson("73", "diff-mode", "mode-desc")
    assert(encodedValue === expectedValue)
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
