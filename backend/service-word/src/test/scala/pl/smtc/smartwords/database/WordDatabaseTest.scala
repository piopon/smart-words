package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model._

import java.io.File
import java.nio.file._

class WordDatabaseTest extends AnyFunSuite {

  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)

  test("testLoadDatabaseWorksCorrectlyWhenDictionaryFilesAreCorrect") {
    val databaseUnderTest: WordDatabase = new WordDatabase()
    assert(databaseUnderTest.loadDatabase())
  }

  test("testSaveDatabaseCreatesNewFileWhenDatabaseIsNotEmpty") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    val testDict: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(1), "pl")
    databaseUnderTest.addWord(Word("test", Category.verb, List("description"), testDict))
    databaseUnderTest.saveDatabase()
    assert(databaseTestFile.exists())
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testSaveDatabaseDoesNotCreateNewFileWhenDatabaseIsEmpty") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    databaseUnderTest.saveDatabase()
    assert(!databaseTestFile.exists())
    // cleanup after checking test result (if somehow the test file will be created...)
    databaseTestFile.delete()
  }

  test("testGetAvailableLanguages") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    val dictionaryPl: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(1), "pl")
    val dictionaryFr: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(1), "fr")
    databaseUnderTest.addWord(Word("word_1", Category.verb, List("description-1"), dictionaryPl))
    databaseUnderTest.addWord(Word("word_2", Category.person, List("description-2"), dictionaryFr))
    databaseUnderTest.addWord(Word("word_3", Category.latin, List("description-3"), dictionaryPl))
    databaseUnderTest.addWord(Word("word_4", Category.verb, List("description-4"), dictionaryPl))
    assert(databaseUnderTest.getAvailableLanguages === List("pl", "fr"))
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testGetAvailableModesReturnsCorrectResultWhenDatabaseIsNotEmpty") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    val dictionaryMode99: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(99), "pl")
    val dictionaryMode11: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(11), "pl")
    val dictionaryMode29: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(29), "fr")
    databaseUnderTest.addWord(Word("word_1", Category.verb, List("description-1"), dictionaryMode11))
    databaseUnderTest.addWord(Word("word_2", Category.person, List("description-2"), dictionaryMode29))
    databaseUnderTest.addWord(Word("word_3", Category.latin, List("description-3"), dictionaryMode29))
    databaseUnderTest.addWord(Word("word_4", Category.verb, List("description-4"), dictionaryMode99))
    databaseUnderTest.addWord(Word("word_5", Category.verb, List("description-4"), dictionaryMode11))
    assert(databaseUnderTest.getAvailableModes.nonEmpty)
    assert(databaseUnderTest.getAvailableModes.get === List(11, 29, 99))
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testGetAvailableModesReturnsEmptyWhenDatabaseIsEmpty") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    assert(databaseUnderTest.getAvailableModes.isEmpty)
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testGetWordsReturnsCorrectNumberOfWordsAfterAddingWords") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    val dictionary: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(99), "pl")
    val word1: Word = Word("word_1", Category.verb, List("description-1"), dictionary)
    val word2: Word = Word("word_2", Category.person, List("description-2"), dictionary)
    val word3: Word = Word("word_3", Category.latin, List("description-3"), dictionary)
    databaseUnderTest.addWord(word1)
    databaseUnderTest.addWord(word2)
    databaseUnderTest.addWord(word3)
    assert(databaseUnderTest.getWords.size === 3)
    assert(databaseUnderTest.getWords.contains(word1))
    assert(databaseUnderTest.getWords.contains(word2))
    assert(databaseUnderTest.getWords.contains(word3))
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testGetWordsReturnsEmptyListWhenNoWordsWereAdded") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    assert(databaseUnderTest.getWords.isEmpty)
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testGetWordIndex") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    val dictionary: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(99), "pl")
    val word1: Word = Word("word_1", Category.verb, List("description-1"), dictionary)
    val word2: Word = Word("word_2", Category.person, List("description-2"), dictionary)
    val word3: Word = Word("word_3", Category.latin, List("description-3"), dictionary)
    databaseUnderTest.addWord(word1)
    databaseUnderTest.addWord(word2)
    databaseUnderTest.addWord(word3)
    assert(databaseUnderTest.getWordIndex(word1.name, word1.dictionary.mode, word1.dictionary.language) === 0)
    assert(databaseUnderTest.getWordIndex(word2.name, word2.dictionary.mode, word2.dictionary.language) === 1)
    assert(databaseUnderTest.getWordIndex(word3.name, word3.dictionary.mode, word3.dictionary.language) === 2)
    // cleanup after checking test result
    databaseTestFile.delete()
  }

  test("testAddWord") {
    val databaseTestFile: File = new File(resourceDir.resolve("test-db.json").toString)
    val databaseUnderTest: WordDatabase = new WordDatabase()
    assert(databaseUnderTest.getWords.isEmpty)
    val dictionary: Dictionary = Dictionary(databaseTestFile.getName, "quiz", Some(99), "pl")
    val word1: Word = Word("word_1", Category.verb, List("description-1"), dictionary)
    databaseUnderTest.addWord(word1)
    assert(databaseUnderTest.getWords.size === 1)
    val word2: Word = Word("word_2", Category.verb, List("description-2"), dictionary)
    databaseUnderTest.addWord(word2)
    assert(databaseUnderTest.getWords.size === 2)
    val word3: Word = Word("word_3", Category.verb, List("description-3"), dictionary)
    databaseUnderTest.addWord(word3)
    assert(databaseUnderTest.getWords.size === 3)
    // cleanup after checking test result
    databaseTestFile.delete()
  }
}
