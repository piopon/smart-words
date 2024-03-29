package pl.smtc.smartwords.database

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.model._

import java.io._
import java.nio.charset.StandardCharsets
import java.nio.file._
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

class ModeDatabase(databaseFile: String = "modes.json") {

  implicit val ModeDecoder: Decoder[Mode] = ModeDao.getModeDecoder
  implicit val ModeEncoder: Encoder[Mode] = ModeDao.getModeEncoder

  private val quizModes: ListBuffer[Mode] = ListBuffer()
  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)

  /**
   * Method used to load and populate quiz modes list with data from internal JSON file
   * @return true if at existing mode file was read correctly or if no dictionary files are present, false otherwise
   */
  def loadDatabase(): Boolean = {
    var result: Boolean = false
    val modesFile: File = new File(resourceDir.resolve(databaseFile).toString)
    Using(new BufferedInputStream(new FileInputStream(modesFile))) { fileStream =>
      val lines = Source.fromInputStream(fileStream).getLines().mkString.stripMargin
      decode[List[Mode]](lines) match {
        case Right(modes) =>
          modes.foreach(mode => quizModes += mode)
          result = true
        case Left(fail) =>
          println(s"Invalid modes file ${modesFile.getName}: ${fail.getMessage}")
          result = false
      }
    }
    result
  }

  /**
   * Method used to retrieve all quiz modes
   * @return a list of currently available quiz modes
   */
  def getModes: List[Mode] = quizModes.toList

  /**
   * Method used to add a new empty quiz mode
   * @return a quiz mode object with predefined ID and an empty value
   */
  def addMode(): Mode = {
    val freeId: Int = if(quizModes.isEmpty) 0 else quizModes.map(mode => mode.id).max + 1
    val newMode: Mode = Mode(freeId, "", "", List(), deletable = true)
    quizModes += newMode
    saveDatabase()
    newMode
  }

  /**
   * Method used to update the mode from specified ID with new values
   * @param id identifier of the mode which should be updated
   * @param newMode new values for the mode
   * @return true if mode was updated successfully, false otherwise
   */
  def updateMode(id: Int, newMode: Mode): Boolean = {
    val idIndex: Int = quizModes.indexWhere(mode => mode.id.equals(id))
    if (-1 == idIndex) {
      return false
    }
    val oldMode: Mode = quizModes.apply(idIndex)
    if (!oldMode.deletable && !modesHaveTheSameSettings(oldMode, newMode)) {
      return false
    }
    newMode.id = id
    quizModes.update(idIndex, newMode)
    saveDatabase()
    true
  }

  /**
   * Method used to remove a mode with specified ID
   * @param id identifier of the mode which should be deleted
   * @return true if mode was deleted successfully, false otherwise
   */
  def deleteMode(id: Int): Boolean = {
    val idIndex: Int = quizModes.indexWhere(mode => mode.id.equals(id))
    if (-1 == idIndex) {
      return false
    }
    if (!quizModes.apply(idIndex).deletable) {
      return false
    }
    quizModes.remove(idIndex)
    saveDatabase()
    true
  }

  /**
   * Method used to save current quiz mode database into JSON file
   */
  private def saveDatabase(): Unit = {
    val content: String = quizModes.asJson.toString()
    Files.write(resourceDir.resolve(databaseFile), content.getBytes(StandardCharsets.UTF_8))
  }

  /**
   * Method used to check if two modes have the same setting types (setting order does not matter)
   * @param mode1 first mode which settings should be checked
   * @param mode2 second mode which settings should be checked
   * @return true if modes have the same settings types (order and label/values insensitive), false otherwise
   */
  private def modesHaveTheSameSettings(mode1: Mode, mode2: Mode): Boolean = {
    if (mode1.settings.size != mode2.settings.size) {
      return false
    }
    mode1.settings.foreach(setting => {
      if (!mode2.settings.map(_.kind).contains(setting.kind)) {
        return false
      }
    })
    true
  }
}
