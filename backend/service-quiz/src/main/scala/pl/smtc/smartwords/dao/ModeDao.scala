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
      id <- input.downField("id").as[Int]
      name <- input.downField("name").as[String]
      description <- input.downField("description").as[String]
      settings <- input.downField("settings").as[List[Setting]]
      deletable <- input.downField("deletable").as[Boolean]
    } yield {
      Mode(id, name, description, settings, deletable)
    }
  }

  /**
   * Method used to receive quiz mode encoder (to JSON)
   * @return quiz mode object encoder
   */
  def getModeEncoder: Encoder[Mode] = Encoder.instance {
    (mode: Mode) => json"""{"id": ${mode.id},
                            "name": ${mode.name},
                            "description": ${mode.description},
                            "deletable": ${mode.deletable},
                            "settings": ${mode.settings}}"""
  }
}
