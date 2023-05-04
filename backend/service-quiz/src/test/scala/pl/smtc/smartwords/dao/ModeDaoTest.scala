package pl.smtc.smartwords.dao

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.{Kind, Mode, Setting}

class ModeDaoTest extends AnyFunSuite {

  test("testGetModeDecoder") {
    val decoderUnderTest: Decoder[Mode] = ModeDao.getModeDecoder
    val sourceJson: Json = createModeJson(Right(0), name = "test-mode", description = "test-mode-description")
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
    val sourceJson: Json = createModeJson(Left("aaa"), "test-mode", "test-mode-description")
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
    val expectedValue: Json = createModeJson(Right(73), "diff-mode", "mode-desc")
    assert(encodedValue === expectedValue)
  }

  /**
   * Method used to create quiz mode JSON object with hardcoded settings and deletable flag, and customizable name,
   * description, and ID which could be either String (to match content of client request) or Int (to match inner logic)
   * @param id identifier of the mode which could be one of: String (as client requests) or Int (as internal logic)
   * @param name mode name used for easier modes distinguish
   * @param description mode description for providing more context about the mode
   * @return JSON object representing quiz mode
   */
  private def createModeJson(id: Either[String, Int], name: String, description: String): Json = {
    Json.obj(
      ("id", id match {
        case Left(s) => Json.fromString(s)
        case Right(i) => Json.fromInt(i)
      }),
      ("name",  Json.fromString(name)),
      ("description",  Json.fromString(description)),
      ("deletable",  Json.fromBoolean(true)),
      ("settings", Json.arr(createSettingJson()))
    )
  }

  /**
   * Method used to create mode settings JSON object with hardcoded type, label, and details
   * @return JSON object representing mode setting
   */
  private def createSettingJson(): Json = {
    Json.obj(
      ("type", Json.fromString("languages")),
      ("label", Json.fromString("test-setting-label")),
      ("details", Json.fromString("test-setting-details"))
    )
  }

}
