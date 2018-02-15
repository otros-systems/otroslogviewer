package pl.otros.logview.logpattern

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import pl.otros.logview.logppattern.LogbackPatternConverter

@RunWith(classOf[JUnitRunner])
class LogbackPatternConverterTest extends WordSpecLike with Matchers {

  "LogbackPatternConverter" should {

    "parse most basic log" in {
      val logPattern = "%level %d{HH:mm:ss.SSS} %thread %logger %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("LEVEL TIMESTAMP THREAD CLASS MESSAGE")
      map.get("dateFormat") shouldBe Some("HH:mm:ss.SSS")
    }

    "parse format with custom date and level formatting" in {
      val logPattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP [THREAD] LEVEL CLASS - MESSAGE")
      map.get("dateFormat") shouldBe Some("HH:mm:ss.SSS")
    }

    "parse format with default date format" in {
      val logPattern = "%d %level %logger %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS MESSAGE")
      map.get("dateFormat") shouldBe Some("yyyy-MM-dd HH:mm:ss,SSS")
    }

    "parse format with ISO8601 date format" in {
      val logPattern = "%d{ISO8601} %level %logger %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS MESSAGE")
      map.get("dateFormat") shouldBe Some("yyyy-MM-dd HH:mm:ss,SSS")
    }

    "parse format with exception merged with message [AFTER]" in {
      val logPattern = "%d %level %logger %msg %exception %n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS MESSAGE")
    }


    "parse format with exception merged with message [BEFORE]" in {
      val logPattern = "%d %level %logger %exception %msg%n"
      val result = LogbackPatternConverter.convert(logPattern)

      result match {
        case Right(map) => map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS MESSAGE")
        case Left(error) => fail(error)
      }

    }


    "parse format with 1 MDC key" in {
      val logPattern = "%d %level %logger %mdc{requestId} %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS PROP(requestId) MESSAGE")
    }

    "parse format with 2 MDC keys as list" in {
      val logPattern = "%d %level %logger %mdc{requestId, userId} %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS requestId=PROP(requestId) userId=PROP(userId) MESSAGE")
    }

    "parse format with 2 MDC keys" in {
      val logPattern = "%d %level %logger %mdc{requestId} %mdc{userId} %msg%n"
      val map = LogbackPatternConverter.convert(logPattern).getOrElse(Map.empty[String, String])

      map.get("pattern") shouldBe Some("TIMESTAMP LEVEL CLASS PROP(requestId) PROP(userId) MESSAGE")
    }


    "don't parse illegal format - wrong conversion word" in {
      val logPattern = "%d %levelx %logger %msg%n"
      val result = LogbackPatternConverter.convert(logPattern)

      result.isLeft shouldBe true
    }

    "don't parse illegal format - merged conversion word" in {
      val logPattern = "%d %level%logger %msg%n"
      val result = LogbackPatternConverter.convert(logPattern)

      result.isLeft shouldBe true
    }


    "don't parse illegal format - message is not last keyword" in {
      val logPattern = "%d %level %msg %logger %n"
      val result = LogbackPatternConverter.convert(logPattern)

      result.isLeft shouldBe true
    }


    "don't parse illegal format - MDC without specified key" in {
      val logPattern = "%d %level %logger %MDC %msg%n"
      val result = LogbackPatternConverter.convert(logPattern)

      result.isLeft shouldBe true
    }

  }
}
