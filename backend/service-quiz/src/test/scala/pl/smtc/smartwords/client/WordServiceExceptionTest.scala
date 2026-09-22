package pl.smtc.smartwords.client

import org.scalatest.funsuite.AnyFunSuite

class WordServiceExceptionTest extends AnyFunSuite {

  test("testWordServiceExceptionConstructors") {
    val cause = new IllegalArgumentException("root-cause")

    val messageAndCauseException = new WordServiceException("custom message", cause)
    assert(messageAndCauseException.getMessage === "custom message")
    assert(messageAndCauseException.getCause eq cause)

    val causeOnlyException = new WordServiceException(cause)
    assert(causeOnlyException.getMessage.contains("root-cause"))
    assert(causeOnlyException.getCause eq cause)

    val nullCauseException = new WordServiceException(null: Throwable)
    assert(nullCauseException.getMessage == null)

    val emptyException = new WordServiceException()
    assert(emptyException.getMessage == null)
  }
}
