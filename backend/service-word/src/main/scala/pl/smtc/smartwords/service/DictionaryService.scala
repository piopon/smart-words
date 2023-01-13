package pl.smtc.smartwords.service

import cats.effect._
import io.circe._
import io.circe.literal._
import org.http4s._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class DictionaryService(wordDB: WordDatabase) {

  def getDictionariesInfo() = ???

  def toJson(dictionary: Dictionary): Json = {
    json"""{
          "game": ${dictionary.game},
          "mode": ${dictionary.mode},
          "language": ${dictionary.language}
        }"""
  }
}
