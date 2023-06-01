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

  test("testGetAvailableModes") {
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
}
