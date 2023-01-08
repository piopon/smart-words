package pl.smtc.smartwords.middleware

import cats.data.ValidatedNel
import org.http4s.ParseFailure

class WordMiddleware {

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
}
