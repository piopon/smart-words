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
}
