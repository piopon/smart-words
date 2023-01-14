package pl.smtc.smartwords.service

import cats.effect._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class DictionaryService(wordDB: WordDatabase) {

  implicit val DictionaryEncoder: Encoder[Dictionary] = DictionaryDao.getDictionaryEncoder

  def getDictionaries: IO[Response[IO]] = {
    Ok(wordDB.getWords.groupBy(_.dictionary).keySet.asJson)
  }
}
