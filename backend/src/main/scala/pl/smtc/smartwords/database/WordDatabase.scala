package pl.smtc.smartwords.database

import io.circe._
import io.circe.parser._
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.utilities._

import scala.collection.mutable.ListBuffer
import scala.io.Source

class WordDatabase {

  private val testWordDB: ListBuffer[Word] = ListBuffer()
  implicit val WordDecoder: Decoder[Word] = WordDao.getWordDecoder

  /**
   * Method used to initialize words database by reading dictionary.json file
   * @return true if file was read correctly, false if error occurred
   */
  def initDatabase(): Boolean = {
    val fileStream = getClass.getResourceAsStream("/dictionary.json")
    val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
    decode[List[Word]](lines) match {
      case Left(fail) =>
        println(s"Invalid dictionary file. ${fail.getMessage}")
        false
      case Right(words) =>
        words.foreach(word => testWordDB += word)
        true
    }
  }

  /**
   * Method used to receive a single word object from database with specified index
   * @param index a index of word to be received
   * @return non empty if word was present (index in bounds), None otherwise
   */
  def getWord(index: Integer): Option[Word] = {
    if (index >= 0 && index < testWordDB.length) {
      Some(testWordDB(index))
    } else {
      None
    }
  }

  /**
   * Method used to receive a single word object from database with specified name
   * @param name a name of a word to be received
   * @return non empty if word was present (name existing), None otherwise
   */
  def getWordByName(name: String): Option[Word] = {
    val nameIndex = testWordDB.indexWhere((dbWord: Word) => dbWord.name.equals(name))
    if (nameIndex >= 0) {
      Some(testWordDB(nameIndex))
    } else {
      None
    }
  }

  /**
   * Method used to receive all words stored in database
   * @return a List of all stored word objects
   */
  def getWords: List[Word] = testWordDB.toList

  /**
   * Method used to receive all words objects from database with specified category
   * @param category a category of words to be found
   * @return a List of all stored word objects with specified category
   */
  def getWordsByCategory(category: Category.Value): List[Word] = {
    testWordDB.toList.filter(word => word.category.equals(category))
  }

  /**
   * Method used to add new word to database
   * @param word new word to be added in database
   * @return true if word was added correctly, false otherwise (word existed in DB)
   */
  def addWord(word: Word): Boolean = {
    val nameIndex = testWordDB.indexWhere((dbWord: Word) => dbWord.name.equals(word.name))
    if (nameIndex < 0) {
      testWordDB += word
      true
    } else {
      false
    }
  }

  /**
   * Method used to update a word in database at a selected position
   * @param index position in DB of a word that needs to be updated
   * @param word new word values
   * @return true if word was updated correctly, false otherwise (word does not exist in DB)
   */
  def updateWord(index: Integer, word: Word): Boolean = {
    if (index >= 0 && index < testWordDB.length) {
      testWordDB.update(index, word)
      true
    } else {
      false
    }
  }

  /**
   * Method used to remove word from database
   * @param word word to be removed from database
   * @return true if word was removed correctly, false otherwise (word does not exist in DB)
   */
  def removeWord(word: Word): Boolean = {
    val nameIndex = testWordDB.indexWhere((dbWord: Word) => dbWord.name.equals(word.name))
    if (nameIndex >= 0) {
      testWordDB.remove(nameIndex)
      true
    } else {
      false
    }
  }
}
