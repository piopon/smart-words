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

  private val dictionaryExtension = "JSON"
  private val wordsDatabase: ListBuffer[Word] = ListBuffer()
  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)
  implicit val WordDecoder: Decoder[Word] = WordDao.getWordDecoder
  implicit val WordEncoder: Encoder[Word] = WordDao.getWordEncoder

  /**
   * Method used to initialize words database by loading and reading all dictionary JSON files
   * @return true if at least one of existing dictionary files was read correctly or if no dictionary files are present,
   *         false if all files cannot be loaded
   */
  def loadDatabase(): Boolean = {
    val dictionaryLoadStatus: ListBuffer[Boolean] = ListBuffer()
    val dictionaryFiles: List[File] = getDirectoryFiles(resourceDir, Some(dictionaryExtension))
    if (dictionaryFiles.isEmpty) {
      return true
    }
    dictionaryFiles.foreach(file => {
      Using(new BufferedInputStream(new FileInputStream(file))) { fileStream =>
        val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
        decode[List[Word]](lines) match {
          case Right(words) =>
            words.foreach(word => {
              word.dictionary = Dictionary.fromFile(file.getName)
              wordsDatabase += word
            })
            dictionaryLoadStatus.addOne(true)
          case Left(fail) =>
            println(s"Invalid dictionary file ${file.getName}: ${fail.getMessage}")
            dictionaryLoadStatus.addOne(false)
        }
      }
    })
    dictionaryLoadStatus.contains(true)
  }

  /**
   * Method used to save current words database into appropriate dictionary JSON files
   */
  def saveDatabase(): Unit = {
    val usedDictionaryFiles: List[String] = wordsDatabase.map(word => word.dictionary.file).distinct.toList
    usedDictionaryFiles.foreach(dictionaryFile => saveDictionary(dictionaryFile))
  }

  /**
   * Method used to save words assigned to a specific dictionary file
   * @param dictionaryFile which dictionary file words should be saved
   */
  private def saveDictionary(dictionaryFile: String): Unit = {
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
  private def getDirectoryFiles(directory: Path, extensionFilter: Option[String] = None): List[File] = {
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
  private def getWord(index: Integer): Option[Word] = {
    if (index >= 0 && index < wordsDatabase.length) {
      Some(wordsDatabase(index))
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
    val nameIndex = wordsDatabase.indexWhere((dbWord: Word) => dbWord.name.equals(name))
    if (nameIndex >= 0) {
      Some(wordsDatabase(nameIndex))
    } else {
      None
    }
  }

  /**
   * Method used to receive all words stored in database
   * @return a List of all stored word objects
   */
  def getWords: List[Word] = wordsDatabase.toList

  /**
   * Method used to receive words stored in database with specified language
   * @param language of the words to be received
   * @return a List of stored word objects with specified language
   */
  def getWordsByLanguage(language: String): List[Word] = {
    wordsDatabase.toList.filter(word => word.dictionary.language.equals(language))
  }

  /**
   * Method used to receive all words objects from database with specified category and language
   * @param language of the words to be received
   * @param category a category of words to be found
   * @return a List of all stored word objects with specified category and language
   */
  def getWordsByCategory(language: String, category: Category.Value): List[Word] = {
    val words: List[Word] = getWordsByLanguage(language)
    words.filter(word => word.category.equals(category))
  }

  /**
   * Method used to receive all words objects from database with specified source dictionary file
   * @param dictionary a source dictionary file of words to be found
   * @return a List of all stored word objects with specified dictionary file
   */
  private def getWordsByDictionary(dictionary: String): List[Word] = {
    wordsDatabase.toList.filter(word => word.dictionary.file.equals(dictionary))
  }

  /**
   * Method used to add new word to database
   * @param word new word to be added in database
   * @return true if word was added correctly, false otherwise (word existed in DB)
   */
  def addWord(language: String, word: Word): Boolean = {
    val nameIndex = wordsDatabase.indexWhere((dbWord: Word) => dbWord.name.equals(word.name))
    if (nameIndex < 0) {
      word.dictionary = Dictionary.generate(language)
      wordsDatabase += word
      saveDictionary(word.dictionary.file)
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
    if (index >= 0 && index < wordsDatabase.length) {
      word.dictionary = getWord(index).get.dictionary
      wordsDatabase.update(index, word)
      saveDictionary(word.dictionary.file)
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
  def removeWord(index: Integer): Boolean = {
    if (index >= 0 && index < wordsDatabase.length) {
      val removedWord: Word = wordsDatabase.remove(index)
      saveDictionary(removedWord.dictionary.file)
      true
    } else {
      false
    }
  }
}
