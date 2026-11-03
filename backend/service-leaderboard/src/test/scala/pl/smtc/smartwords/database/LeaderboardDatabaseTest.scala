package pl.smtc.smartwords.database

import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.model.LeaderboardEntry

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

class LeaderboardDatabaseTest extends AnyFunSuite {

  test("testAddEntryPersistsDataAndCanBeReloaded") {
    val customDir: Path = Files.createTempDirectory("leaderboard-db-save-")
    val fileName = "leaderboard.json"
    try {
      val database = new LeaderboardDatabase(Some(customDir), fileName)
      assert(database.loadDatabase())

      database.addEntry(LeaderboardEntry("quiz", 0, "alice", 0.8f, 10, 8, 1000L))
      database.addEntry(LeaderboardEntry("quiz", 0, "bob", 0.9f, 10, 9, 900L))

      val reloaded = new LeaderboardDatabase(Some(customDir), fileName)
      assert(reloaded.loadDatabase())
      val top = reloaded.getTopEntries("quiz", 0, 10)

      assert(top.length === 2)
      assert(top.head.username === "bob")
      assert(top(1).username === "alice")
    } finally {
      Files.deleteIfExists(customDir.resolve(fileName))
      Files.deleteIfExists(customDir)
    }
  }
}
