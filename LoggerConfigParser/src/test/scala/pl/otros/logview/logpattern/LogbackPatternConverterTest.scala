package pl.otros.logview.logpattern

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import pl.otros.logview.logppattern.LogbackPatternConverter

@RunWith(classOf[JUnitRunner])
class LogbackPatternConverterTest extends WordSpecLike with Matchers {

  "LogbackPatternConverter" should {

    def testValid(logPattern: String, pattern: String, dateFormat: String = "yyyy-MM-dd HH:mm:ss,SSS") = {
      val result = LogbackPatternConverter.convert(logPattern)
      result match {
        case Right(map) =>
          map.get("pattern") shouldBe Some(pattern)
          map.get("dateFormat") shouldBe Some(dateFormat)
        case Left(error) => fail(error)
      }
    }
    def isInvalid(logPattern:String) = {
      LogbackPatternConverter.convert(logPattern).isLeft shouldBe true
    }

    "parse most basic log" in {
      val logPattern = "%level %d{HH:mm:ss.SSS} %thread %logger %msg%n"
      val pattern = "LEVEL TIMESTAMP THREAD CLASS MESSAGE"
      val dateFormat = "HH:mm:ss.SSS"
      testValid(logPattern, pattern, dateFormat)
    }

    "parse format with custom date and level formatting" in {
      val logPattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
      testValid(logPattern, "TIMESTAMP [THREAD] LEVEL CLASS - MESSAGE", "HH:mm:ss.SSS")


    }

    "parse format with default date format" in {
      val logPattern = "%d %level %logger %msg%n"
      testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE")


    }

    "parse format with ISO8601 date format" in {
      val logPattern = "%d{ISO8601} %level %logger %msg%n"
      testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE")


    }

    "parse format with exception merged with message [AFTER]" in {
      val logPattern = "%d %level %logger %msg %exception %n"
      testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE")
    }


    "parse format with exception merged with message [BEFORE]" in {
      val logPattern = "%d %level %logger %exception %msg%n"
      testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE")
    }


    "parse format with 1 MDC key" in {
      val logPattern = "%d %level %logger [%mdc{requestId}] %msg%n"
      testValid(logPattern, "TIMESTAMP LEVEL CLASS [PROP(requestId)] MESSAGE")
    }

    "parse format with 2 MDC keys as list" in {
      val logPattern = "%d %level %logger %mdc{requestId, userId} %msg%n"
      testValid(logPattern, "TIMESTAMP LEVEL CLASS requestId=PROP(requestId) userId=PROP(userId) MESSAGE")
    }

    "parse format with 2 MDC keys" in {
      val logPattern = "%d %level %logger [%mdc{requestId}] [%mdc{userId}] %msg%n"
      testValid(logPattern,"TIMESTAMP LEVEL CLASS [PROP(requestId)] [PROP(userId)] MESSAGE")
    }


    "don't parse illegal format - wrong conversion word" in {
      val logPattern = "%d %levelx %logger %msg%n"
      isInvalid(logPattern)
    }

    "don't parse illegal format - merged conversion word" in {
      val logPattern = "%d %level%logger %msg%n"
      isInvalid(logPattern)
    }


    "don't parse illegal format - message is not last keyword" in {
      val logPattern = "%d %level %msg %logger %n"
      isInvalid(logPattern)
    }


    "don't parse illegal format - MDC without specified key" in {
      val logPattern = "%d %level %logger %MDC %msg%n"
      isInvalid(logPattern)
    }

    "don't parse illegal format - No conversion word found" in {
      val logPattern = "something ..... ..."
      isInvalid(logPattern)
    }
    "don't parse illegal format - Conversion word don't separated with white char" in {
      val logPattern = "%date{yyyy-MM-dd} %-5level[%thread] %logger - %msg%n"
      isInvalid(logPattern)
    }

    "don't parse illegal format - MDC between white chars" in {
      val logPattern = "%date{yyyy-MM-dd} %X{mdcKey} %-5level [%thread] %logger - %msg%n"
      isInvalid(logPattern)
    }

  }
}
