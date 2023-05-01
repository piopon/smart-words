package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.Mode

class ModeDatabaseTest extends AnyFunSuite {

  test("testLoadDatabase") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase("test-modes.json")
    databaseUnderTest.loadDatabase()
    assert(databaseUnderTest.getModes.size === 1)
    val checkedMode: Mode = databaseUnderTest.getModes.head
    assert(checkedMode.id === 99)
    assert(checkedMode.name === "UNIT test QUIZ mode 1")
    assert(checkedMode.description === "this is a JSON for unit test and checking quiz mode logic")
    assert(checkedMode.deletable === true)
  }

  test("testDeleteMode") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase()
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.deleteMode(0)
    assert(databaseUnderTest.getModes.size === 0)
  }

  test("testAddMode") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase()
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    val addedMode: Mode = databaseUnderTest.getModes.head
    assert(addedMode.id === 0)
    assert(addedMode.name.isEmpty)
    assert(addedMode.description.isEmpty)
    assert(addedMode.deletable === true)
  }

  test("testGetModes") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase()
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 2)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 3)
  }

  test("testUpdateMode") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase()
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    val updatedMode: Mode = Mode(id = 1, "test_name", "test_description", List(), deletable = false)
    databaseUnderTest.updateMode(0, updatedMode)
    val checkedMode: Mode = databaseUnderTest.getModes.head
    assert(checkedMode.id === 0)
    assert(checkedMode.name === "test_name")
    assert(checkedMode.description === "test_description")
    assert(checkedMode.deletable === false)
  }
}
