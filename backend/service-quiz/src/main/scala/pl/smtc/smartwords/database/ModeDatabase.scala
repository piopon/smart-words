package pl.smtc.smartwords.database

import io.circe._
import io.circe.parser._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.model._

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

class ModeDatabase {

  implicit val ModeDecoder: Decoder[Mode] = ModeDao.getModeDecoder

  private val quizModes: ListBuffer[Mode] = ListBuffer()
  private val quizModesFile: String = "modes.json"
  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)

  /**
   * Method used to load and populate quiz modes list with data from internal JSON file
   * @return true if at existing mode file was read correctly or if no dictionary files are present, false otherwise
   */
  def loadDatabase(): Boolean = {
    var result: Boolean = false
    val modesFile: File = new File(resourceDir.resolve(quizModesFile).toString)
    Using(new BufferedInputStream(new FileInputStream(modesFile))) { fileStream =>
      val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
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
    val freeId: Int = quizModes.map(mode => mode.id).max + 1
    val newMode: Mode = Mode(freeId, "", "", List(), deletable = true)
    quizModes += newMode
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
    newMode.id = id
    quizModes.update(idIndex, newMode)
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
    quizModes.remove(idIndex)
    true
  }
}
