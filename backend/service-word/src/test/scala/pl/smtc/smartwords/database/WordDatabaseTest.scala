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
    val testWord: Word = Word("test", Category.verb, List("description"), testDict)
    databaseUnderTest.addWord(testWord)
    databaseUnderTest.saveDatabase()
    assert(databaseTestFile.exists())
    // cleanup after checking test result
    databaseTestFile.delete()
  }
}
