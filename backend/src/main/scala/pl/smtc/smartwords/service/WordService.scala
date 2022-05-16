package pl.smtc.smartwords.service

import cats.effect._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.utilities._

class WordService(wordDB: WordDatabase) {

  implicit val WordEncoder: Encoder[Word] = WordDao.getWordEncoder

  def getWords(maybeCategory: Option[Category.Value]): IO[Response[IO]] = {
    maybeCategory match {
      case None =>
        Ok(wordDB.getWords.asJson)
      case Some(category) =>
        Ok(wordDB.getWordsByCategory(category).asJson)
    }
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
