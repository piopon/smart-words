package pl.smtc.smartwords.controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class LeaderboardControllerTest extends AnyFunSuite {

  test("testPostEntryThenGetTopEntries") {
    val database = new LeaderboardDatabase()
    val controllerUnderTest = new LeaderboardController(database)

    val payload = LeaderboardSubmit("quiz", 0, "alice", 0.8f, 10, 8)
    val postRequest = Request[IO](Method.POST, Uri.unsafeFromString("/entries"))
      .withEntity(payload.asJson)
    val postResponse = controllerUnderTest.getRoutes.run(postRequest).value.unsafeRunSync()

    assert(postResponse.nonEmpty)
    assert(postResponse.get.status === Status.Created)

    val getRequest = Request[IO](Method.GET, Uri.unsafeFromString("/entries/quiz/0?limit=10"))
    val getResponse = controllerUnderTest.getRoutes.run(getRequest).value.unsafeRunSync()

    assert(getResponse.nonEmpty)
    assert(getResponse.get.status === Status.Ok)
    val body = getResponse.get.as[Json].unsafeRunSync()
    val usernames = body.asArray.getOrElse(Vector.empty)
      .flatMap(_.hcursor.downField("username").as[String].toOption)
    assert(usernames.nonEmpty)
    assert(usernames.head === "alice")
  }
}
