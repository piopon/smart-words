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

  test("testGetTopEntriesReturnsTop10ByDefault") {
    val database = new LeaderboardDatabase()
    val service = new LeaderboardService(database)

    for (i <- 0 until 15) {
      val payload = LeaderboardSubmit("quiz", 1, s"user-$i", (100 - i).toFloat / 100.0f, 10, 10 - (i / 2))
      service.submitEntry(payload).unsafeRunSync()
    }

    val response = service.getTopEntries("quiz", "1", None).unsafeRunSync()
    val body = response.as[Json].unsafeRunSync()
    val items = body.asArray.getOrElse(Vector.empty)
    assert(items.length === 10)
  }
}
