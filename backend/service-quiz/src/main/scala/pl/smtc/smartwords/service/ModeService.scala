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

  private val resourceDir: Path = Paths.get(getClass.getResource("/").toURI)
  private val quizModesFile = "modes.json"
  private val quizModes: ListBuffer[Mode] = initializeModes()

  def getQuizModes: IO[Response[IO]] = {
    Ok(quizModes.toList.asJson)
  }

  def createQuizMode: IO[Response[IO]] = {
    val freeId: Int = quizModes.map(mode => mode.id).max
    quizModes += Mode(freeId, "", "", List())
    Ok(s"Added new quiz mode ID: $freeId")
  }

  def updateQuizMode(id: Int, mode: Mode): IO[Response[IO]] = {
    if (quizModes.filter(mode => mode.id.equals(id)).isEmpty) {
      return BadRequest("Cannot find mode with ID: " + id)
    }
    quizModes.update(0, mode)
    Ok(s"Updated quiz mode ID: $id")
  }

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
