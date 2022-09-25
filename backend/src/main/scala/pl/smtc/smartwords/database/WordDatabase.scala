package pl.smtc.smartwords.database

import io.circe._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.dao._

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

class WordDatabase {

  private val testWordDB: ListBuffer[Word] = ListBuffer()
  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)
  implicit val WordDecoder: Decoder[Word] = WordDao.getWordDecoder
  implicit val WordEncoder: Encoder[Word] = WordDao.getWordEncoder

  /**
   * Method used to initialize words database by loading and reading dictionary.json file
   * @return true if file was read correctly, false if error occurred
   */
  def loadDatabase(): Boolean = {
    val dictionaryExtension = "JSON"
    getDirectoryFiles(resourceDir, Some(dictionaryExtension)).foreach(file => {
      Using(new BufferedInputStream(new FileInputStream(file))) { fileStream =>
        val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
        decode[List[Word]](lines) match {
          case Right(words) =>
            words.foreach(word => {
              word.dictionary = file.getName
              testWordDB += word
            })
          case Left(fail) =>
            println(s"Invalid dictionary file ${file.getName}: ${fail.getMessage}")
            false
        }
      }
    })
    true
  }

  /**
   * Method used to save current words database into appropriate dictionary JSON files
   */
  def saveDatabase(): Unit = {
    val usedDictionaryFiles: List[String] = testWordDB.map(word => word.dictionary).distinct.toList
    usedDictionaryFiles.foreach(dictionaryFile => saveDictionary(dictionaryFile))
  }

  /**
   * Method used to save words assigned to a specific dictionary file
   * @param dictionaryFile which dictionary file words should be saved
   */
  def saveDictionary(dictionaryFile: String): Unit = {
    if (dictionaryFile.isEmpty) return
    val dictionaryWords: List[Word] = getWordsByDictionary(dictionaryFile)
    if (dictionaryWords.isEmpty) {
      Files.delete(resourceDir.resolve(dictionaryFile))
    } else {
      val content: String = dictionaryWords.asJson.toString()
      Files.write(resourceDir.resolve(dictionaryFile), content.getBytes(StandardCharsets.UTF_8))
    }
  }

  /**
   * Method used to receive the list of files with optional extension filter in input directory
   * @param directory directory in which we want to search for files
   * @param extensionFilter optional extension filter
   * @return list of all files present in input directory
   */
  def getDirectoryFiles(directory: Path, extensionFilter: Option[String] = None): List[File] = {
    val input = new File(directory.toString)
    if (input.exists && input.isDirectory) {
      val ifFile = (input: File) => input.isFile
      val ifExtension = (input: File) => extensionFilter match {
        case None => true
        case Some(filter) => input.getName.toLowerCase.endsWith(filter.toLowerCase)
      }
      input.listFiles.filter(file => ifFile.apply(file) && ifExtension.apply(file)).toList
    } else {
      List[File]()
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
   * Method used to receive all words objects from database with specified source dictionary file
   * @param dictionary a source dictionary file of words to be found
   * @return a List of all stored word objects with specified dictionary file
   */
  def getWordsByDictionary(dictionary: String): List[Word] = {
    testWordDB.toList.filter(word => word.dictionary.equals(dictionary))
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
      word.dictionary = getWord(index).get.dictionary
      testWordDB.update(index, word)
      saveDictionary(word.dictionary)
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
      val removedWord: Word = testWordDB.remove(nameIndex)
      saveDictionary(removedWord.dictionary)
      true
    } else {
      false
    }
  }
}
