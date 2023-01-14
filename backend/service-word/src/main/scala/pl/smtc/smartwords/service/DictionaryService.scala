package pl.smtc.smartwords.service

import cats.effect._
import io.circe._
import io.circe.literal._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class DictionaryService(wordDB: WordDatabase) {

  def getDictionariesInfo(): IO[Response[IO]] = {
    val availableDictionaries: Set[Dictionary] = wordDB.getWords.groupBy((word: Word) => word.dictionary).keySet
    val rawJson: Set[Json] = availableDictionaries.map(d => toJson(d))
    Ok(rawJson.asJson)
  }

  private def toJson(dictionary: Dictionary): Json = {
    json"""{
          "game": ${dictionary.game},
          "mode": ${dictionary.mode},
          "language": ${dictionary.language}
        }"""
  }
}
