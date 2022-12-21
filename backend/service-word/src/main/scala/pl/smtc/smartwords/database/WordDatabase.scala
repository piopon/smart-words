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
   * Method used to receive all words stored in database
   * @return a List of all stored word objects
   */
  def getWords: List[Word] = wordsDatabase.toList

  /**
   * Method used to retrieve an index of the word with specified name and language<br>
   * <b>NOTE:</b> it's required to match not only the name but the language also since in different languages there are words
   * which are spelled the same but have different meaning, like:
   * <ul>
   *   <li>pupil = student in English</li>
   *   <li>pupil = favorite / pet in Polish</li>
   * </ul>
   * @param name of the word to be found
   * @param language of the word to be found
   * @return database index of the word which name and language matches parameters, or -1 if no such word was found
   */
  def getWordIndex(name: String, mode: String, language: String): Int = {
    wordsDatabase.indexWhere((word: Word) => word.name.equals(name) &&
                                             word.dictionary.mode.equals(mode) &&
                                             word.dictionary.language.equals(language))
  }

  /**
   * Method used to add new word to database
   * @param word new word to be added in database
   * @return true if word was added correctly, false otherwise (word existed in DB)
   */
  def addWord(word: Word): Boolean = {
    val wordIndex = getWordIndex(word.name, word.dictionary.mode, word.dictionary.language)
    if (wordIndex < 0) {
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
   * @param index position in DB of a word that needs to be removed
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
   * @param extensionFilter optional extension filter (if not used then no extension will be used)
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
   * Method used to receive all words objects from database with specified source dictionary file
   * @param dictionary a source dictionary file of words to be found
   * @return a List of all stored word objects with specified dictionary file
   */
  private def getWordsByDictionary(dictionary: String): List[Word] = {
    wordsDatabase.toList.filter(word => word.dictionary.file.equals(dictionary))
  }
}
