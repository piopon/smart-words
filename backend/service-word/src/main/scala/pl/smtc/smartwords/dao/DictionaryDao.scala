package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object DictionaryDao {

  def getDictionaryEncoder: Encoder[Dictionary] = Encoder.instance {
    (dict: Dictionary) => json"""{"game": ${dict.game},
                                  "mode": ${dict.mode},
                                  "language": ${dict.language}}"""
  }

}
