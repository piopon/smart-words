package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import org.http4s.Status
import org.scalatest.funsuite.AnyFunSuite

class HealthServiceTest extends AnyFunSuite {

  test("testCheckHealth") {
    val serviceUnderTest: HealthService = new HealthService()
    val res = serviceUnderTest.checkHealth().unsafeRunSync()
    assert(res.status === Status.Ok)
  }
}
