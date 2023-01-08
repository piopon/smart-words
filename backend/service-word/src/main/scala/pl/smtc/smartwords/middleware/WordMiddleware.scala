package pl.smtc.smartwords.middleware

import cats.data.ValidatedNel
import org.http4s.ParseFailure
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.utilities._

class WordMiddleware {

  private val parser: DataParser = new DataParser()

  def validateParameterMode(input: String): Option[Int] = {
    val gameMode: Option[Int] = parser.parseGameMode(input)
    if (gameMode.isEmpty) {
      throw new WordMiddlewareException(s"Invalid game mode value: $input")
    }
    gameMode
  }

  /**
   *
   * @param input
   * @throws WordMiddlewareException
   * @return
   */
  @throws(classOf[WordMiddlewareException])
  def validateParameterRandom(input: Option[ValidatedNel[ParseFailure, Boolean]]): Option[Boolean] = {
    input match {
      case None => None
      case Some(random) => random.fold(
        error => throw new WordMiddlewareException(error.head.sanitized + ": invalid 'random' parameter value."),
        value => Some(value)
      )
    }
  }

  @throws(classOf[WordMiddlewareException])
  def validateParameterSize(input: Option[ValidatedNel[ParseFailure, Int]]): Option[Int] = {
    input match {
      case None => None
      case Some(size) => size.fold(
        error => throw new WordMiddlewareException(error.head.sanitized + ": invalid 'size' parameter value."),
        value => if (value > 0) {
          Some(value)
        } else {
          throw new WordMiddlewareException("Incorrect 'size' parameter value: must be greater then 0.")
        }
      )
    }
  }

  @throws(classOf[WordMiddlewareException])
  def validateParameterCategory(input: Option[String]): Option[Category.Value] = {
    input match {
      case None => None
      case Some(category) => {
        val availableCategories: List[String] = Category.values.map(cat => cat.toString).toList
        if (availableCategories.contains(category)) {
          Some(Category.fromString(category))
        } else {
          throw new WordMiddlewareException("Invalid 'cat' parameter: value '" + category + "' is not supported.")
        }
      }
    }
  }
}
