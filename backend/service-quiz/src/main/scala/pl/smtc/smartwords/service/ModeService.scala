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

class ModeService(database: ModeDatabase) {

  implicit val ModeEncoder: Encoder[Mode] = ModeDao.getModeEncoder
  implicit val SettingEncoder: Encoder[Setting] = SettingDao.getSettingEncoder

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
    val newMode: Mode = database.addMode()
    Ok(newMode.asJson)
  }

  /**
   * Method used to update quiz mode with specified ID
   * @param id identifier of the quiz mode
   * @param mode new quiz mode value
   * @return response with update status (OK or NOT FOUND if mode does not exist)
   */
  def updateQuizMode(id: Int, mode: Mode): IO[Response[IO]] = {
    if (database.updateMode(id, mode)) {
      return Ok(s"Updated quiz mode ID: $id")
    }
    NotFound(s"Cannot find mode with ID: $id, or mode cannot be updated with initial settings removal")
  }

  /**
   * Method used to delete quiz mode with specified ID
   * @param id identifier of the quiz mode
   * @return response with update status (OK or NOT FOUND if mode does not exist/is not deletable)
   */
  def deleteQuizMode(id: Int): IO[Response[IO]] = {
    if (database.deleteMode(id)) {
      return Ok(s"Deleted quiz mode ID: $id")
    }
    NotFound(s"Cannot find mode with ID: $id, or mode is not deletable")
  }
}
