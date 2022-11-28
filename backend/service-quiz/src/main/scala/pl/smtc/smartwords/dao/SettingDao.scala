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
      label <- input.downField("label").as[String]
      kind <- input.downField("type").as[String]
      details <- input.downField("details").as[String]
    } yield {
      Setting(label, Kind.fromString(kind), details)
    }
  }

  /**
   * Method used to receive mode setting encoder (to JSON)
   * @return setting object encoder
   */
  def getSettingEncoder: Encoder[Setting] = Encoder.instance {
    (setting: Setting) => json"""{"label": ${setting.label},
                                  "type": ${setting.kind.toString},
                                  "details": ${setting.details}}"""
  }
}
