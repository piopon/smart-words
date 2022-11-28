package pl.smtc.smartwords.model

/**
 * Quiz mode type (used 'kind' due to the fact that 'type' is a reserved word in Scala)
 * Available options are: NUMBER, LANGUAGE and UNKNOWN
 */
object Kind extends Enumeration {
  type Kind = Value
  val number, language, unknown = Value
  def fromString(string: String): Value =
    values.find(_.toString.toLowerCase() == string.toLowerCase()).getOrElse(unknown)
}
