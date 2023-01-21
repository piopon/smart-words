package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object DictionaryDao {

  /**
   * Method used to receive dictionary decoder (from JSON)
   * @return dictionary object decoder
   */
  def getWordDecoder: Decoder[Dictionary] = Decoder.instance {
    (input: HCursor) => for {
      game <- input.downField("game").as[String]
      mode <- input.downField("mode").as[Int]
      language <- input.downField("language").as[String]
    } yield {
      Dictionary.create(Some(mode), language)
    }
  }

  /**
   * Method used to receive dictionary encoder (to JSON)
   * @return dictionary object encoder
   */
  def getDictionaryEncoder: Encoder[Dictionary] = Encoder.instance {
    (dict: Dictionary) => json"""{"game": ${dict.game},
                                  "mode": ${dict.mode},
                                  "language": ${dict.language}}"""
  }

}
