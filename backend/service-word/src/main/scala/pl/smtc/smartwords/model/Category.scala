package pl.smtc.smartwords.model

/**
 * Word category type.
 * Available options are: VERB, NOUN, ADJECTIVE, LATIN, PERSON and UNKNOWN
 */
object Category extends Enumeration {
  type Category = Value
  val verb, noun, adjective, latin, person, unknown = Value
  def fromString(string: String): Value =
    values.find(_.toString.toLowerCase() == string.toLowerCase()).getOrElse(unknown)
}
