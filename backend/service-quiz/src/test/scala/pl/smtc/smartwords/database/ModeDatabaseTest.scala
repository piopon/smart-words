package pl.smtc.smartwords.database

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.{Kind, Mode, Setting}

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.{Path, Paths}

class ModeDatabaseTest extends AnyFunSuite with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    for {
      files <- Option(new File(Paths.get(getClass.getResource("/").toURI).toString).listFiles)
      file <- files if file.getName.endsWith(".json") && !file.getName.endsWith("test-mode-database-load.json")
    } file.delete()
  }

  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)
  private val databaseTestFile: String = "test-mode-database-crud.json"

  test("testLoadDatabase") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase("test-mode-database-load.json")
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
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseTestFile)
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.deleteMode(0)
    assert(databaseUnderTest.getModes.size === 0)
    assert(new File(resourceDir.resolve(databaseTestFile).toString).exists())
  }

  test("testAddMode") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseTestFile)
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    val addedMode: Mode = databaseUnderTest.getModes.head
    assert(addedMode.id === 0)
    assert(addedMode.name.isEmpty)
    assert(addedMode.description.isEmpty)
    assert(addedMode.deletable === true)
    assert(new File(resourceDir.resolve(databaseTestFile).toString).exists())
  }

  test("testGetModes") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseTestFile)
    assert(databaseUnderTest.getModes.size === 0)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 1)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 2)
    databaseUnderTest.addMode()
    assert(databaseUnderTest.getModes.size === 3)
    assert(new File(resourceDir.resolve(databaseTestFile).toString).exists())
  }

  test("testUpdateMode") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseTestFile)
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
    assert(new File(resourceDir.resolve(databaseTestFile).toString).exists())
  }

  test("testInitializeDataDirectoryCopiesSeedModesFile") {
    val customDataDir: Path = Files.createTempDirectory("mode-db-data-")
    val seedDir: Path = resourceDir
    val seedFileName = "test-mode-database-load.json"

    try {
      val copiedModesFile = customDataDir.resolve(seedFileName)
      assert(!Files.exists(copiedModesFile))

      val databaseUnderTest = new ModeDatabase(seedFileName, Some(customDataDir), Some(seedDir))
      assert(Files.exists(copiedModesFile))

      assert(databaseUnderTest.loadDatabase())
      assert(databaseUnderTest.getModes.size === 2)
    } finally {
      val copiedModesFile = customDataDir.resolve(seedFileName)
      if (Files.exists(copiedModesFile)) {
        Files.delete(copiedModesFile)
      }
      Files.deleteIfExists(customDataDir)
    }
  }

  test("testLoadDatabaseReturnsFalseWhenModesJsonIsInvalid") {
    val customDataDir: Path = Files.createTempDirectory("mode-db-invalid-data-")
    val emptySeedDir: Path = Files.createTempDirectory("mode-db-invalid-seed-")
    val modesFileName = "modes-invalid.json"
    val invalidModesFile = customDataDir.resolve(modesFileName)

    try {
      Files.write(invalidModesFile, "{ invalid json ]".getBytes(StandardCharsets.UTF_8))
      val databaseUnderTest = new ModeDatabase(modesFileName, Some(customDataDir), Some(emptySeedDir))
      assert(!databaseUnderTest.loadDatabase())
      assert(databaseUnderTest.getModes.isEmpty)
    } finally {
      Files.deleteIfExists(invalidModesFile)
      Files.deleteIfExists(emptySeedDir)
      Files.deleteIfExists(customDataDir)
    }
  }

  test("testUpdateModeRejectsSettingsKindChangeWhenModeIsNotDeletable") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseTestFile)
    val initialMode = Mode(
      id = 0,
      name = "initial",
      description = "initial description",
      settings = List(Setting(Kind.questions, "questions", "1-10")),
      deletable = false
    )
    val updatedMode = Mode(
      id = 1,
      name = "updated",
      description = "updated description",
      settings = List(Setting(Kind.languages, "languages", "pl,en")),
      deletable = false
    )

    databaseUnderTest.addMode()
    assert(databaseUnderTest.updateMode(0, initialMode))
    assert(!databaseUnderTest.updateMode(0, updatedMode))

    val persistedMode = databaseUnderTest.getModes.head
    assert(persistedMode.name === "initial")
    assert(persistedMode.settings.map(_.kind) === List(Kind.questions))
  }

  test("testUpdateModeReturnsFalseWhenModeIdDoesNotExist") {
    val databaseUnderTest: ModeDatabase = new ModeDatabase(databaseTestFile)
    val updatedMode = Mode(
      id = 1,
      name = "updated",
      description = "updated description",
      settings = List(Setting(Kind.questions, "questions", "1-10")),
      deletable = true
    )

    databaseUnderTest.addMode()
    assert(!databaseUnderTest.updateMode(999, updatedMode))
    assert(databaseUnderTest.getModes.size === 1)
  }

  test("testConstructorFallbacksUseBundledResourceAndDefaultCurrentDirectory") {
    val customDataDir: Path = Files.createTempDirectory("mode-db-bundled-fallback-")
    val seedFileName = "test-mode-database-load.json"
    val copiedModesFile = customDataDir.resolve(seedFileName)

    try {
      assert(!Files.exists(copiedModesFile))
      val bundledFallbackDb = new ModeDatabase(seedFileName, Some(customDataDir), None, Some(resourceDir))
      assert(Files.exists(copiedModesFile))
      assert(bundledFallbackDb.loadDatabase())

      val currentDirectoryFallbackDb = new ModeDatabase("unused-modes.json", None, None, None)
      val databaseDirField = classOf[ModeDatabase].getDeclaredField("databaseDir")
      databaseDirField.setAccessible(true)
      val resolvedDatabaseDir = databaseDirField.get(currentDirectoryFallbackDb).asInstanceOf[Path]
      assert(resolvedDatabaseDir == Paths.get("."))
    } finally {
      Files.deleteIfExists(copiedModesFile)
      Files.deleteIfExists(customDataDir)
    }
  }
}
