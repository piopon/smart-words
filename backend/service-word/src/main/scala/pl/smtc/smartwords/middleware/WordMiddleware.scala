package pl.smtc.smartwords.middleware

import cats.data.ValidatedNel
import org.http4s.ParseFailure
import pl.smtc.smartwords.model._
import pl.smtc.smartwords.utilities._

class WordMiddleware {

  private val parser: DataParser = new DataParser()

  /**
   * Method used to validate "mode" parameter (if it's an integer and if its present in stored words dictionary)
   * @param input value containing mode identifier which should be validated
   * @param availableModes list of modes which are currently available in words from database
   * @throws WordMiddlewareException when validation fails (input is not an integer, or a not supported integer value)
   * @return integer value representing the mode setting
   */
  @throws(classOf[WordMiddlewareException])
  def validateParameterMode(input: String, availableModes: Option[List[Int]]): Option[Int] = {
    parser.parseGameMode(input) match {
      case None => throw new WordMiddlewareException(s"Invalid 'mode' parameter: value '$input' cannot be parsed.")
      case Some(mode) =>
        if (availableModes.isEmpty || availableModes.get.contains(mode)) {
          Some(mode)
        } else {
          throw new WordMiddlewareException(s"Invalid 'mode' parameter: value '$input' is not supported.")
        }
    }
  }

  /**
   * Method used to validate "language" parameter (if it's a part of used/available languages in database)
   * @param input value containing language value which should be validated
   * @param availableLanguages list of languages which are currently available in words from database
   * @throws WordMiddlewareException when validation fails (input is not supported language value)
   * @return string value representing the language setting
   */
  @throws(classOf[WordMiddlewareException])
  def validateParameterLanguage(input: String, availableLanguages: List[String]): String = {
    if (!availableLanguages.contains(input)) {
      throw new WordMiddlewareException(s"Invalid 'language' parameter: value '$input' is not supported.")
    }
    input
  }

  /**
   * Method used to validate "random" parameter (if it's an boolean)
   * @param input value containing randomness value in form of "true" or "false" (or a parse failure information)
   * @throws WordMiddlewareException when validation fails (input cannot be parsed to boolean)
   * @return boolean value representing the random setting
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

  /**
   * Method used to validate "size" parameter (if it's an integer and if its bigger than zero)
   * @param input value containing size value which should be validated (or a parse failure information)
   * @throws WordMiddlewareException when validation fails (input is not an integer, or is smaller or equal to 0)
   * @return integer value representing the size setting
   */
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

  /**
   * Method used to validate "cat" parameter (if it's a value present in category enumerable)
   * @param input value containing category value which should be validated
   * @throws WordMiddlewareException when validation fails (input is not a valid enum value)
   * @return category enumerable value representing the category setting
   */
  @throws(classOf[WordMiddlewareException])
  def validateParameterCategory(input: Option[String]): Option[Category.Value] = {
    input match {
      case None => None
      case Some(category) =>
        val availableCategories: List[String] = Category.values.map(cat => cat.toString).toList
        if (availableCategories.contains(category)) {
          Some(Category.fromString(category))
        } else {
          throw new WordMiddlewareException("Invalid 'cat' parameter: value '" + category + "' is not supported.")
        }
    }
  }
}
