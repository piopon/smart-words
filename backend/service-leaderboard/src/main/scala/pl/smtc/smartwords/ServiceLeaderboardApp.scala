package pl.smtc.smartwords

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.middleware._
import org.http4s.ember.server._
import pl.smtc.smartwords.config._
import pl.smtc.smartwords.controller._
import pl.smtc.smartwords.database._

import scala.concurrent.duration.DurationInt

object ServiceLeaderboardApp extends IOApp {

  private lazy val appConfig: LeaderboardAppConfig = LeaderboardAppConfig.load()

  override def run(args: List[String]): IO[ExitCode] = {
    val serverHost: Host = Host.fromString(appConfig.service.host).getOrElse(ipv4"0.0.0.0")
    val serverPort: Port = Port.fromInt(appConfig.service.port).getOrElse(port"3333")
    val leaderboardDatabase = new LeaderboardDatabase()
    val leaderboardController = new LeaderboardController(leaderboardDatabase)

    val config = CORSConfig(
      anyOrigin = appConfig.cors.anyOrigin,
      allowCredentials = appConfig.cors.allowCredentials,
      maxAge = appConfig.cors.maxAgeSeconds,
      anyMethod = appConfig.cors.anyMethod
    )
    val apis = Router(
      "/leaderboard" -> CORS(leaderboardController.getRoutes, config)
    ).orNotFound

    for {
      server <- EmberServerBuilder.default[IO]
        .withHost(serverHost)
        .withPort(serverPort)
        .withHttpApp(apis)
        .withIdleTimeout(appConfig.service.idleTimeoutMinutes.minutes)
        .withErrorHandler { case err => IO(err.printStackTrace()).as(Response(status = Status.InternalServerError)) }
        .build
    } yield server
  }.use(server => {
    val serverAddress = server.address.getAddress.getHostAddress
    val serverPort = server.address.getPort
    IO.delay(println(s"Service: ${appConfig.service.name}\n" +
      s"- state: started\n" +
      s"- address: IPv6=$serverAddress, port=$serverPort")) >> IO.never.as(ExitCode.Success)
  })
}
