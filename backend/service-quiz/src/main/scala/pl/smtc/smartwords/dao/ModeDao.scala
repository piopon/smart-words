package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object ModeDao {

  implicit val SettingDecoder: Decoder[Setting] = SettingDao.getSettingDecoder
  implicit val SettingEncoder: Encoder[Setting] = SettingDao.getSettingEncoder

  /**
   * Method used to receive quiz mode decoder (from JSON)
   * @return quiz mode object decoder
   */
  def getModeDecoder: Decoder[Mode] = Decoder.instance {
    (input: HCursor) => for {
      name <- input.downField("name").as[String]
      description <- input.downField("description").as[String]
      settings <- input.downField("settings").as[List[Setting]]
    } yield {
      Mode(name, description, settings)
    }
  }

  /**
   * Method used to receive quiz mode encoder (to JSON)
   * @return quiz mode object encoder
   */
  def getModeEncoder: Encoder[Mode] = Encoder.instance {
    (mode: Mode) => json"""{"name": ${mode.name},
                            "description": ${mode.description},
                            "settings": ${mode.settings}}"""
  }
}
