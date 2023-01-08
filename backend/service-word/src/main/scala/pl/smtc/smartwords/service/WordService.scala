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
import pl.smtc.smartwords.utilities._

import scala.util.Random

class WordService(database: WordDatabase) {

  private val parser: DataParser = new DataParser()
  implicit val WordEncoder: Encoder[Word] = WordDao.getWordEncoder

  /**
   * Method used to receive words
   * @param mode identifier of quiz mode word to be retrieved
   * @param language language of words to be retrieved
   * @param category optional word category
   * @param size optional number of words to receive
   * @param random optional flag to determine if output should be randomized
   * @return response with words from specified category, or all words if category is None
   */
  def getWords(mode: Option[Int], language: String, category: Option[Category.Value],
               size: Option[Int], random: Option[Boolean]): IO[Response[IO]] = {
    val modeWords: List[Word] = database.getWords.filter(word => word.dictionary.mode.equals(mode))
    val languageWords: List[Word] = modeWords.filter(word => word.dictionary.language.equals(language))
    val afterCategoryFilter: List[Word] = category match {
      case None => languageWords
      case Some(categoryValue) => languageWords.filter(word => word.category.equals(categoryValue))
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
   * @param mode identifier of the quiz mode word to be added
   * @param language language of the word to be added
   * @param word new word to be added
   * @return response with new word add status (always OK but with different message)
   */
  def addWord(mode: Option[Int], language: String, word: Word): IO[Response[IO]] = {
    word.dictionary = Dictionary.create(mode, language)
    if (database.addWord(word)) {
      Ok(s"added word '${word.name}'")
    } else {
      Found(s"word '${word.name}' already defined")
    }
  }

  /**
   * Method used to update specified word
   * @param mode identifier of the quiz mode word to be updated
   * @param language of the word to be updated (many languages have words with the same spelling but different meaning)
   * @param name word name which should be updated
   * @param word new word definition
   * @return response with update status (OK or NOT FOUND if word does not exist)
   */
  def updateWord(mode: Option[Int], language: String, name: String, word: Word): IO[Response[IO]] = {
    val wordIndex = database.getWordIndex(name, mode, language)
    if (database.updateWord(wordIndex, word)) {
      Ok(s"updated word '$name'")
    } else {
      NotFound(s"word '$name' not found in DB")
    }
  }

  /**
   * Method used to delete specified word
   * @param mode identifier of the quiz mode word to be deleted
   * @param language of the word to be removed (many languages have words with the same spelling but different meaning)
   * @param name word name to be deleted
   * @return response with delete status (OK or NOT FOUND if word does not exist)
   */
  def deleteWord(mode: Option[Int], language: String, name: String): IO[Response[IO]] = {
    val wordIndex = database.getWordIndex(name, mode, language)
    if (database.removeWord(wordIndex)) {
      Ok(s"removed word '$name'")
    } else {
      NotFound(s"word '$name' not found in DB")
    }
  }
}
