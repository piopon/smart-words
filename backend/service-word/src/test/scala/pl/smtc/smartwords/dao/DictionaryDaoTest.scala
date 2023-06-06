package pl.smtc.smartwords.dao

import io.circe.{Encoder, Json}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model._

class DictionaryDaoTest extends AnyFunSuite {

  test("testGetDictionaryEncoder") {
    val encoderUnderTest: Encoder[Dictionary] = DictionaryDao.getDictionaryEncoder
    val sourceDictionary: Dictionary = Dictionary("file", "game", Some(1), "lang")
    val encodedValue: Json = encoderUnderTest.apply(sourceDictionary)
    val expectedValue: Json = createDictionaryJson("game", Some(1), "lang")
    assert(encodedValue === expectedValue)
  }

  /**
   * Method used to create dictionary JSON object with hardcoded file, game type, optional ID, and language
   * @param game name of the game for which the dictionary is created
   * @param mode optional game mode for which the dictionary is created
   * @param language source language of the created dictionary
   * @return JSON object representing word
   */
  private def createDictionaryJson(game: String, mode: Option[Int], language: String): Json = {
    Json.obj(
      ("game",  Json.fromString(game)),
      ("mode",  mode match {
        case Some(value) => Json.fromInt(value)
        case None => Json.Null
      }),
      ("language",  Json.fromString(language))
    )
  }
}
