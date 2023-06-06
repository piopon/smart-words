package pl.smtc.smartwords.dao

import io.circe.{Decoder, Encoder, Json}
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

  test("testGetDictionaryDecoder") {
    val decoderUnderTest: Decoder[Dictionary] = DictionaryDao.getDictionaryDecoder
    val sourceJson: Json = createDictionaryJson("puzzle", Some(111), "it")
    val decodedValue: Decoder.Result[Dictionary] = decoderUnderTest.decodeJson(sourceJson)
    assert(decodedValue.left.toOption === None)
    assert(decodedValue.toOption !== None)
    val decodedDictionary: Dictionary = decodedValue.toOption.get
    assert(decodedDictionary.file.startsWith("words-quiz-111-it@"))
    assert(decodedDictionary.game === "quiz") // currently "quiz" is hardcoded as the only supported game type
    assert(decodedDictionary.mode.nonEmpty)
    assert(decodedDictionary.mode.get === 111)
    assert(decodedDictionary.language === "it")
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
