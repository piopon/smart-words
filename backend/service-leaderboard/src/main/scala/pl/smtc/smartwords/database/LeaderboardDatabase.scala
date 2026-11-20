package pl.smtc.smartwords.database

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import pl.smtc.smartwords.model._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

class LeaderboardDatabase(dataDirectory: Option[Path] = sys.env.get("LEADERBOARD_DATA_DIR").map(Paths.get(_)),
                          leaderboardFileName: String = sys.env.getOrElse("LEADERBOARD_DATA_FILE", "leaderboard.json")) {

  private val entries = ListBuffer[LeaderboardEntry]()
  private val databaseDir: Path = dataDirectory.getOrElse(Paths.get("."))
  private val databaseFile: Path = databaseDir.resolve(leaderboardFileName)

  initializeDataDirectory()

  /**
   * Method used to initialize leaderboard database by loading persisted JSON file.
   * @return true if data was loaded correctly or file does not exist, false if file cannot be parsed
   */
  def loadDatabase(): Boolean = synchronized {
    if (!Files.exists(databaseFile)) {
      return true
    }

    val loaded = Using.resource(Source.fromFile(databaseFile.toFile, StandardCharsets.UTF_8.name())) { source =>
      decode[List[LeaderboardEntry]](source.mkString)
    }

    loaded match {
      case Right(values) =>
        entries.clear()
        entries.addAll(values)
        true
      case Left(error) =>
        println(s"Invalid leaderboard file ${databaseFile.getFileName}: ${error.getMessage}")
        false
    }
  }

  /**
   * Add one leaderboard attempt to full history.
    *
    * @param entry new leaderboard entry to persist
    * @return saved leaderboard entry
   */
  def addEntry(entry: LeaderboardEntry): LeaderboardEntry = synchronized {
    entries += entry
    saveDatabase()
    entry
  }

  /**
   * Return sorted top entries for selected game and mode.
    *
    * @param game game identifier
    * @param mode mode identifier
    * @param limit max amount of entries to return
    * @return top entries sorted by score, question count, and timestamp
   */
  def getTopEntries(game: String, mode: Int, limit: Int): List[LeaderboardEntry] = synchronized {
    entries
      .filter(entry => entry.game == game && entry.mode == mode)
      .sortWith((left, right) => {
        if (left.scorePercent != right.scorePercent) {
          left.scorePercent > right.scorePercent
        } else if (left.totalQuestions != right.totalQuestions) {
          left.totalQuestions > right.totalQuestions
        } else {
          left.timestamp < right.timestamp
        }
      })
      .take(limit)
      .toList
  }

  /**
   * Method used to save current leaderboard history into a JSON file.
   */
  def saveDatabase(): Unit = synchronized {
    val content = entries.toList.asJson.toString()
    Files.write(databaseFile, content.getBytes(StandardCharsets.UTF_8))
  }

  /**
   * Create custom data directory if configured.
   */
  private def initializeDataDirectory(): Unit = {
    Files.createDirectories(databaseDir)
  }
}
