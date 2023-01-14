package pl.smtc.smartwords.service

import cats.effect._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._

class DictionaryService(wordDB: WordDatabase) {

  def getDictionaries: IO[Response[IO]] = {
    Ok(wordDB.getWords.groupBy(_.dictionary).keySet.map(_.toJson).asJson)
  }
}
