package pl.smtc.smartwords.dao

import io.circe.{Decoder, Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model._

class WordDaoTest extends AnyFunSuite {

  test("testGetWordEncoder") {
    val encoderUnderTest: Encoder[Word] = WordDao.getWordEncoder
    val sourceDictionary: Dictionary = Dictionary("file", "game", Some(1), "lang")
    val sourceWord: Word = Word("word", Category.verb, List("desc"), sourceDictionary)
    val encodedValue: Json = encoderUnderTest.apply(sourceWord)
    val expectedValue: Json = createWordJson("word", "verb", List("desc"))
    assert(encodedValue === expectedValue)
  }

  /**
   * Method used to create word JSON object with hardcoded name, category, and definition
   * @param name word name
   * @param category word category
   * @param definitions word definitions
   * @return JSON object representing word
   */
  private def createWordJson(name: String, category: String, definitions: List[String]): Json = {
    Json.obj(
      ("name",  Json.fromString(name)),
      ("category",  Json.fromString(category)),
      ("description",  Json.fromValues(definitions.map(desc => Json.fromString(desc))))
    )
  }
}
