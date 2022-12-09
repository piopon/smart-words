package pl.smtc.smartwords.client

class WordServiceException(msg: String) extends Exception(msg) {
  def this(msg: String, cause: Throwable) = {
    this(msg)
    initCause(cause)
  }

  def this(cause: Throwable) = {
    this(Option(cause).map(_.toString).orNull)
    initCause(cause)
  }

  def this() = {
    this(null: String)
  }
}