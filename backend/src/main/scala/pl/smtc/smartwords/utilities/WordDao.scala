package pl.smtc.smartwords.utilities

import io.circe._
import pl.smtc.smartwords.model._

object WordDao {
  def getWordDecoder: Decoder[Word] = Decoder.instance {
    (input: HCursor) => for {
      name <- input.downField("name").as[String]
      category <- input.downField("category").as[String]
      definition <- input.downField("description").as[String]
    } yield {
      Word(name, Category.fromString(category), definition)
    }
  }
}
