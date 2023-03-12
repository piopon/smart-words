package pl.smtc.smartwords.service

import cats.effect._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import pl.smtc.smartwords.dao._
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

import scala.collection.mutable.ListBuffer

class ModeService(database: ModeDatabase) {

  implicit val ModeEncoder: Encoder[Mode] = ModeDao.getModeEncoder
  implicit val SettingEncoder: Encoder[Setting] = SettingDao.getSettingEncoder

  private val quizModes: ListBuffer[Mode] = database.loadDatabase()

  /**
   * Method used to receive the list of current quiz modes
   * @return response with list of quiz modes
   */
  def getQuizModes: IO[Response[IO]] = {
    Ok(database.getModes.asJson)
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
    val newId: Int = database.addMode()
    Ok(s"Added new quiz mode ID: $newId")
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
    mode.id = id
    quizModes.update(idPosition, mode)
    Ok(s"Updated quiz mode ID: $id")
  }
}
