package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.Mode

import java.io.File
import java.nio.file.{Path, Paths}

class ModeDatabaseTest extends AnyFunSuite {

  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)

  test("testLoadDatabase") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase("test-modes.json")
    databaseUnderTest.loadDatabase()
    assert(databaseUnderTest.getModes.size === 2)
    val firstMode: Mode = databaseUnderTest.getModes.head
    assert(firstMode.id === 99)
    assert(firstMode.name === "UNIT test QUIZ mode 1")
    assert(firstMode.description === "this is a JSON for unit test and checking quiz mode logic")
    assert(firstMode.deletable === true)
    val lastMode: Mode = databaseUnderTest.getModes.last
    assert(lastMode.id === 17)
    assert(lastMode.name === "second MODE for UNIT tests")
    assert(lastMode.description === "another unit test mode")
    assert(lastMode.deletable === false)
  }

  test("testDeleteMode") {
    val databaseFile: String = "test-delete.json"
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseFile)
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.deleteMode(0)
    assert(databaseUnderTest.getModes.size === 0)
    assert(new File(resourceDir.resolve(databaseFile).toString).exists())
  }

  test("testAddMode") {
    val databaseFile: String = "test-add.json"
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseFile)
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    val addedMode: Mode = databaseUnderTest.getModes.head
    assert(addedMode.id === 0)
    assert(addedMode.name.isEmpty)
    assert(addedMode.description.isEmpty)
    assert(addedMode.deletable === true)
    assert(new File(resourceDir.resolve(databaseFile).toString).exists())
  }

  test("testGetModes") {
    val databaseFile: String = "test-get.json"
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseFile)
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 2)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 3)
    assert(new File(resourceDir.resolve(databaseFile).toString).exists())
  }

  test("testUpdateMode") {
    val databaseFile: String = "test-update.json"
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseFile)
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
    assert(new File(resourceDir.resolve(databaseFile).toString).exists())
  }
}
