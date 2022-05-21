package pl.smtc.smartwords.dao

import io.circe._
import io.circe.literal._
import pl.smtc.smartwords.model._

object WordDao {

  /**
   * Method used to receive word decoder (from JSON)
   * @return word object decoder
   */
  def getWordDecoder: Decoder[Word] = Decoder.instance {
    (input: HCursor) => for {
      name <- input.downField("name").as[String]
      category <- input.downField("category").as[String]
      definition <- input.downField("description").as[String]
    } yield {
      Word(name, Category.fromString(category), definition)
    }
  }

  /**
   * Method used to receive word encoder (to JSON)
   * @return word object encoder
   */
  def getWordEncoder: Encoder[Word] = Encoder.instance {
    (word: Word) => json"""{"name": ${word.name}, "category": ${word.category.toString}, "description": ${word.definition}}"""
  }
}
