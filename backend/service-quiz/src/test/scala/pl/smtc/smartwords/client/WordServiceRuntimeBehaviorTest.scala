package pl.smtc.smartwords.client

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import org.scalatest.funsuite.AnyFunSuite
import pl.smtc.smartwords.config.QuizWordServiceConfig

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class WordServiceRuntimeBehaviorTest extends AnyFunSuite {

  test("testIsAliveReturnsTrueWhenHealthResponseIsOk") {
    withServer("OK", "[]") { port =>
      val serviceUnderTest = new WordService(Some(createConfig(port)))
      assert(serviceUnderTest.isAlive)
    }
  }

  test("testIsAliveReturnsFalseWhenHealthResponseIsNotOk") {
    withServer("DOWN", "[]") { port =>
      val serviceUnderTest = new WordService(Some(createConfig(port)))
      assert(!serviceUnderTest.isAlive)
    }
  }

  test("testGetRandomWordReturnsSingleWordWhenEndpointReturnsData") {
    val singleWordJson = """[{"name":"alpha","category":"verb","description":["d1","d2"]}]"""
    withServer("OK", singleWordJson) { port =>
      val serviceUnderTest = new WordService(Some(createConfig(port)))
      val result = serviceUnderTest.getRandomWord(0, "pl")
      assert(result.name == "alpha")
      assert(result.category == "verb")
      assert(result.description == List("d1", "d2"))
    }
  }

  test("testGetRandomWordThrowsWhenEndpointReturnsEmptyList") {
    withServer("OK", "[]") { port =>
      val serviceUnderTest = new WordService(Some(createConfig(port)))
      assertThrows[WordServiceException] {
        serviceUnderTest.getRandomWord(0, "pl")
      }
    }
  }

  test("testGetWordsByCategoryReturnsListWhenEndpointReturnsData") {
    val wordsJson =
      """[{"name":"alpha","category":"verb","description":["d1"]},{"name":"beta","category":"verb","description":["d2"]}]"""
    withServer("OK", wordsJson) { port =>
      val serviceUnderTest = new WordService(Some(createConfig(port)))
      val result = serviceUnderTest.getWordsByCategory(0, "pl", "verb")
      assert(result.size == 2)
      assert(result.map(_.name) == List("alpha", "beta"))
    }
  }

  test("testGetWordsByCategoryThrowsWhenEndpointReturnsEmptyList") {
    withServer("OK", "[]") { port =>
      val serviceUnderTest = new WordService(Some(createConfig(port)))
      assertThrows[WordServiceException] {
        serviceUnderTest.getWordsByCategory(0, "pl", "verb")
      }
    }
  }

  private def createConfig(port: Int): QuizWordServiceConfig = {
    QuizWordServiceConfig(
      name = "WORD",
      host = "127.0.0.1",
      port = port,
      baseUrl = "",
      requestTimeoutSeconds = 1.0
    )
  }

  private def withServer(healthBody: String, wordsBody: String)(testBody: Int => Unit): Unit = {
    val server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
    server.createContext("/", (exchange: HttpExchange) => {
      val path = exchange.getRequestURI.getPath
      val responseBody = if (path == "/health") healthBody else if (path.startsWith("/words/")) wordsBody else "[]"
      writeResponse(exchange, responseBody)
    })
    server.start()
    val port = server.getAddress.getPort

    try {
      testBody(port)
    } finally {
      server.stop(0)
    }
  }

  private def writeResponse(exchange: HttpExchange, body: String): Unit = {
    val bytes = body.getBytes(StandardCharsets.UTF_8)
    exchange.sendResponseHeaders(200, bytes.length)
    val outputStream = exchange.getResponseBody
    try {
      outputStream.write(bytes)
    } finally {
      outputStream.close()
    }
  }
}
