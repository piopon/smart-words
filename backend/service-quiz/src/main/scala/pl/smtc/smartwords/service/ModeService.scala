package pl.smtc.smartwords.service

import cats.effect._
import io.circe._
import io.circe.syntax._
import io.circe.parser._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.model._

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

class ModeService {

  implicit val ModeDecoder: Decoder[Mode] = ModeDao.getModeDecoder
  implicit val ModeEncoder: Encoder[Mode] = ModeDao.getModeEncoder
  implicit val SettingEncoder: Encoder[Setting] = SettingDao.getSettingEncoder

  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)
  private val quizModesFile = "modes.json"
  private val quizModes: ListBuffer[Mode] = initializeModes()

  /**
   * Method used to receive the list of current quiz modes
   * @return response with list of quiz modes
   */
  def getQuizModes: IO[Response[IO]] = {
    Ok(quizModes.toList.asJson)
  }

  /**
   * Method used to receive the list of all supported settings
   * @return response with a list of supported settings
   */
  def getSupportedSettings: IO[Response[IO]] = {
    val supportedSettings: List[Setting] = Kind.values.toList
      .filter(kind => kind != Kind.unknown)
      .map(kind => Setting(kind, "", ""))
    Ok(supportedSettings.asJson)
  }

  /**
   * Method used to create new empty quiz mode
   * @return confirmation response with ID of newly created quiz mode
   */
  def createQuizMode: IO[Response[IO]] = {
    val freeId: Int = quizModes.map(mode => mode.id).max
    quizModes += Mode(freeId, "", "", List())
    Ok(s"Added new quiz mode ID: $freeId")
  }

  /**
   * Method used to update quiz mode with specified ID
   * @param id identifier of the quiz mode
   * @param mode new quiz mode value
   * @return response with update status (OK or NOT FOUND if word does not exist)
   */
  def updateQuizMode(id: Int, mode: Mode): IO[Response[IO]] = {
    val idPosition: Int = quizModes.indexWhere(mode => mode.id.equals(id))
    if (-1 == idPosition) {
      return NotFound("Cannot find mode with ID: " + id)
    }
    quizModes.update(idPosition, mode)
    Ok(s"Updated quiz mode ID: $id")
  }

  /**
   * Method used to initialize quiz modes list with data from internal JSON file
   * @return ListBuffer with supported quiz modes
   */
  private def initializeModes(): ListBuffer[Mode] = {
    val foundModes: ListBuffer[Mode] = new ListBuffer()
    val modesFile = new File(resourceDir.resolve(quizModesFile).toString)
    Using(new BufferedInputStream(new FileInputStream(modesFile))) { fileStream =>
      val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
      decode[List[Mode]](lines) match {
        case Right(modes) => modes.foreach(mode => foundModes += mode)
        case Left(fail) => println(s"Invalid modes file ${modesFile.getName}: ${fail.getMessage}")
      }
    }
    foundModes
  }
}
