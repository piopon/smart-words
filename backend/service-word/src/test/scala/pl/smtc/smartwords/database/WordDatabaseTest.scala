package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite

class WordDatabaseTest extends AnyFunSuite {

  test("testLoadDatabase") {
    val databaseUnderTest: WordDatabase = new WordDatabase()
    assert(databaseUnderTest.loadDatabase())
  }
}
