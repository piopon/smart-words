package pl.smtc.smartwords.service

import cats.effect._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import io.circe.literal._
import org.http4s._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class WordService(wordDB: WordDatabase) {

  implicit val WordEncoder: Encoder[Word] = Encoder.instance {
    (word: Word) => json"""{"name": ${word.name}, "category": ${word.category.toString}, "description": ${word.definition}}"""
  }

  def addWord(word: Word): IO[Response[IO]] = {
    if (wordDB.addWord(word)) {
      Ok(s"Added new word \"${word.name}\".")
    } else {
      Ok(s"Word \"${word.name}\" already defined.")
    }
  }

  def updateWord(name: String, word: Word): IO[Response[IO]] = {
    val nameIndex = wordDB.getWords.indexWhere((word: Word) => word.name.equals(name))
    if (wordDB.updateWord(nameIndex, word)) {
      Ok(s"Updated word \"$name\".")
    } else {
      NotFound(s"Word \"$name\" not found in DB.")
    }
  }

  def deleteWord(name: String): IO[Response[IO]] = {
    wordDB.getWordByName(name) match {
      case None => NotFound(s"Word \"$name\" not found in DB.")
      case Some(word) =>
        wordDB.removeWord(word)
        Ok(word.asJson)
    }
  }
}
