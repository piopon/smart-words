package pl.smtc.smartwords.service

import cats.effect._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.dao._

class WordService(wordDB: WordDatabase) {

  implicit val WordEncoder: Encoder[Word] = WordDao.getWordEncoder

  /**
   * Method used to receive words
   * @param maybeCategory optional word category
   * @param maybeSize optional number of words to receive
   * @return response with words from specified category, or all words if category is None
   */
  def getWords(maybeCategory: Option[Category.Value], maybeSize: Option[Int]): IO[Response[IO]] = {
    maybeCategory match {
      case None =>
        maybeSize match {
          case None => Ok(wordDB.getWords.asJson)
          case Some(size) => Ok(wordDB.getWords.take(size).asJson)
        }
      case Some(category) =>
        maybeSize match {
          case None => Ok(wordDB.getWordsByCategory(category).asJson)
          case Some(size) => Ok(wordDB.getWordsByCategory(category).take(size).asJson)
        }
    }
  }

  /**
   * Method used to add new word
   * @param word new word to be added
   * @return response with new word add status (always OK but with different message)
   */
  def addWord(word: Word): IO[Response[IO]] = {
    if (wordDB.addWord(word)) {
      Ok(s"Added new word \"${word.name}\".")
    } else {
      Ok(s"Word \"${word.name}\" already defined.")
    }
  }

  /**
   * Method used to update specified word
   * @param name word name which should be updated
   * @param word new word definition
   * @return response with update status (OK or NOT FOUND if word does not exist)
   */
  def updateWord(name: String, word: Word): IO[Response[IO]] = {
    val nameIndex = wordDB.getWords.indexWhere((word: Word) => word.name.equals(name))
    if (wordDB.updateWord(nameIndex, word)) {
      Ok(s"Updated word \"$name\".")
    } else {
      NotFound(s"Word \"$name\" not found in DB.")
    }
  }

  /**
   * Method used to delete specified word
   * @param name word name to be deleted
   * @return response with delete status (OK or NOT FOUND if word does not exist)
   */
  def deleteWord(name: String): IO[Response[IO]] = {
    wordDB.getWordByName(name) match {
      case None => NotFound(s"Word \"$name\" not found in DB.")
      case Some(word) =>
        wordDB.removeWord(word)
        Ok(word.asJson)
    }
  }
}
