package pl.smtc.smartwords.service

import cats.effect.unsafe.implicits.global
import io.circe.Json
import org.http4s.circe.jsonDecoder
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.database._
import pl.smtc.smartwords.model._

class LeaderboardServiceTest extends AnyFunSuite {

  test("testSubmitAndGetTopEntriesSortedByScoreAndQuestions") {
    val database = new LeaderboardDatabase()
    val service = new LeaderboardService(database)

    val low = LeaderboardSubmit("quiz", 0, "alice", 0.75f, 10, 7)
    val highSmall = LeaderboardSubmit("quiz", 0, "bob", 0.90f, 5, 4)
    val highLarge = LeaderboardSubmit("quiz", 0, "charlie", 0.90f, 10, 9)

    service.submitEntry(low).unsafeRunSync()
    service.submitEntry(highSmall).unsafeRunSync()
    service.submitEntry(highLarge).unsafeRunSync()

    val response = service.getTopEntries("quiz", "0", Some(10)).unsafeRunSync()
    val body = response.as[Json].unsafeRunSync()
    val usernames = body.asArray.getOrElse(Vector.empty)
      .flatMap(_.hcursor.downField("username").as[String].toOption)

    assert(usernames.length === 3)
    assert(usernames(0) === "charlie")
    assert(usernames(1) === "bob")
    assert(usernames(2) === "alice")
  }
}
