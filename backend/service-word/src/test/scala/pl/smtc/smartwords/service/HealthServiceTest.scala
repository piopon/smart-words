package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite

class HealthServiceTest extends AnyFunSuite {

  test("testCheckHealth") {
    val serviceUnderTest: HealthService = new HealthService()
    val res: String = serviceUnderTest.checkHealth().flatMap(_.as[String]).unsafeRunSync()
    assert(res === "Service: WORD - status: OK")
  }

  test("testCheckHealthReturnsInternalServerErrorWhenStatusIsNotOk") {
    val serviceUnderTest: HealthService = new HealthService(statusSupplier = () => "DOWN")
    val res = serviceUnderTest.checkHealth().unsafeRunSync()
    val body = res.as[String].unsafeRunSync()
    assert(res.status === org.http4s.Status.InternalServerError)
    assert(body === "Service: WORD - status: DOWN")
  }
}
