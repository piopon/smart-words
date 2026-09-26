package pl.smtc.smartwords.service

import org.scalatest.funsuite.AnyFunSuite

class QuizServiceExceptionTest extends AnyFunSuite {

  test("testQuizServiceExceptionConstructors") {
    val cause = new IllegalArgumentException("root-cause")

    val messageAndCauseException = new QuizServiceException("custom message", cause)
    assert(messageAndCauseException.getMessage === "custom message")
    assert(messageAndCauseException.getCause eq cause)

    val causeOnlyException = new QuizServiceException(cause)
    assert(causeOnlyException.getMessage.contains("root-cause"))
    assert(causeOnlyException.getCause eq cause)

    val nullCauseException = new QuizServiceException(null: Throwable)
    assert(nullCauseException.getMessage == null)

    val emptyException = new QuizServiceException()
    assert(emptyException.getMessage == null)
  }
}
