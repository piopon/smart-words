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
   * @return ListBuffer with supported quiz modes
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

  def getModes: List[Mode] = quizModes.toList

  def addMode(): Int = {
    val freeId: Int = quizModes.map(mode => mode.id).max + 1
    quizModes += Mode(freeId, "", "", List())
    freeId
  }

  def updateMode(id: Int, newMode: Mode): Boolean = {
    val idIndex: Int = quizModes.indexWhere(mode => mode.id.equals(id))
    if (-1 == idIndex) {
      return false
    }
    newMode.id = id
    quizModes.update(idIndex, newMode)
    true
  }
}
