package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite

class HealthServiceTest extends AnyFunSuite {

  test("testCheckHealth") {
    val serviceUnderTest: HealthService = new HealthService()
    val res: String = serviceUnderTest.checkHealth().flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Service: WORD - status: OK")
  }
}
