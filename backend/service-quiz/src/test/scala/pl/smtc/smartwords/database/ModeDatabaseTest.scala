package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite

class ModeDatabaseTest extends AnyFunSuite {

  test("testDeleteMode") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase()
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.deleteMode(0)
    assert(databaseUnderTest.getModes.size === 0)
  }
}
