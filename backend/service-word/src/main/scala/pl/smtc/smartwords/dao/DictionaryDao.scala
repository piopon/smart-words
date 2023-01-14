package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object DictionaryDao {

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
