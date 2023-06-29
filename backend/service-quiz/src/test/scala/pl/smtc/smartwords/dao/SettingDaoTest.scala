package pl.smtc.smartwords.dao

import io.circe._
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model._

class SettingDaoTest extends AnyFunSuite {

  test("testGetSettingDecoder") {
    val decoderUnderTest: Decoder[Setting] = SettingDao.getSettingDecoder
    val sourceJson: Json = createSettingJson()
    val decodedValue: Decoder.Result[Setting] = decoderUnderTest.decodeJson(sourceJson)
    assert(decodedValue.left.toOption === None)
    assert(decodedValue.toOption !== None)
    val decodedSetting: Setting = decodedValue.toOption.get
    assert(decodedSetting.kind === Kind.languages)
    assert(decodedSetting.label === "test-setting-label")
    assert(decodedSetting.details === "test-setting-details")
  }

  test("testGetSettingEncoder") {
    val encoderUnderTest: Encoder[Setting] = SettingDao.getSettingEncoder
    val sourceSetting: Setting = Setting(Kind.languages, "test-setting-label", "test-setting-details")
    val encodedValue: Json = encoderUnderTest.apply(sourceSetting)
    val expectedValue: Json = createSettingJson()
    assert(encodedValue === expectedValue)
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
