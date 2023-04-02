package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object SettingDao {

  /**
   * Method used to receive mode setting decoder (from JSON)
   * @return setting object decoder
   */
  def getSettingDecoder: Decoder[Setting] = Decoder.instance {
    (input: HCursor) => for {
      kind <- input.downField("type").as[String]
      label <- input.downField("label").as[String]
      details <- input.downField("details").as[String]
    } yield {
      Setting(Kind.fromString(kind), label, details)
    }
  }

  /**
   * Method used to receive mode setting encoder (to JSON)
   * @return setting object encoder
   */
  def getSettingEncoder: Encoder[Setting] = Encoder.instance {
    (setting: Setting) => json"""{"type": ${setting.kind.toString},
                                  "label": ${setting.label},
                                  "details": ${setting.details}}"""
  }
}
