package pl.smtc.smartwords.dao

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.{Kind, Setting}

class SettingDaoTest extends AnyFunSuite {

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
