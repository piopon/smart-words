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

import scala.util.Random

class WordService(wordDB: WordDatabase) {

  implicit val WordEncoder: Encoder[Word] = WordDao.getWordEncoder

  /**
   * Method used to receive words
   * @param language optional language of words to be retrieved
   * @param category optional word category
   * @param size optional number of words to receive
   * @param random optional flag to determine if output should be randomized
   * @return response with words from specified category, or all words if category is None
   */
  def getWords(language: Option[String], category: Option[Category.Value],
               size: Option[Int], random: Option[Boolean]): IO[Response[IO]] = {
    val afterCategoryFilter: List[Word] = category match {
      case None => wordDB.getWords(language)
      case Some(categoryValue) => wordDB.getWordsByCategory(language, categoryValue)
    }
    val afterRandomFilter: List[Word] = random match {
      case None => afterCategoryFilter
      case Some(randomValue) => if (randomValue) Random.shuffle(afterCategoryFilter) else afterCategoryFilter
    }
    val afterSizeFilter: List[Word] = size match {
      case None => afterRandomFilter
      case Some(sizeValue) => afterRandomFilter.take(sizeValue)
    }
    Ok(afterSizeFilter.asJson)
  }

  /**
   * Method used to add new word
   * @param word new word to be added
   * @return response with new word add status (always OK but with different message)
   */
  def addWord(word: Word): IO[Response[IO]] = {
    if (wordDB.addWord(word)) {
      Ok(s"added word '${word.name}'")
    } else {
      Found(s"word '${word.name}' already defined")
    }
  }

  /**
   * Method used to update specified word
   * @param name word name which should be updated
   * @param word new word definition
   * @return response with update status (OK or NOT FOUND if word does not exist)
   */
  def updateWord(name: String, word: Word): IO[Response[IO]] = {
    val nameIndex = wordDB.getWords(None).indexWhere((word: Word) => word.name.equals(name))
    if (wordDB.updateWord(nameIndex, word)) {
      Ok(s"updated word '$name'")
    } else {
      NotFound(s"word '$name' not found in DB")
    }
  }

  /**
   * Method used to delete specified word
   * @param name word name to be deleted
   * @return response with delete status (OK or NOT FOUND if word does not exist)
   */
  def deleteWord(name: String): IO[Response[IO]] = {
    wordDB.getWordByName(name) match {
      case None => NotFound(s"word '$name' not found in DB")
      case Some(word) =>
        wordDB.removeWord(word)
        Ok(s"removed word '$name'")
    }
  }
}
