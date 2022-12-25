package pl.smtc.smartwords.utilities

class DataParser {

  /**
   * Method used to convert string game mode to an integer identifier
   * @param mode of the quiz in the String format
   * @return integer identifier representing quiz mode, or empty if input cannot be parsed
   */
  def parseGameMode(mode: String): Option[Int] = {
    try {
      Some(mode.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }
}
