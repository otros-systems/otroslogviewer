package pl.otros.logview.logppattern

import scala.util.parsing.combinator.RegexParsers

class LogbackPatternConverter {

  //https://logback.qos.ch/manual/layouts.html#ClassicPatternLayout
  def convert(pattern: String): Either[String, Map[String, String]] = {

    //1 parse format to abstract
    val r = LogPatternParser(pattern)
      //2 convert to logback definition
      .map(logbackFormat)
      //2a. convert "EXCEPTION MESSAGE" to "MESSAGE"
      .map(mergeSomeConversionWordBeforeMessage)
      //3 validation
      .flatMap(validate)
      //4 convert to Log4jPattern
      .map(toLog4jPatternProperties)
    r
  }

}

object LogPatternParser extends RegexParsers {
  def apply(pattern: String): Either[String, List[LoggerAst]] = {
    parse(tokens(), pattern) match {
      case Success(result, _) => Right(result)
      case Error(msg, _) => Left(msg)
      case Failure(msg, _) => Left(msg)
    }
  }

  override def skipWhitespace: Boolean = false

  private def conversionWord: Parser[ConversionWord] = {
    def formatModifier: Parser[String] = "((-)?[\\d]+)?\\.?((-)?[\\d]+)?".r

    "%" ~ formatModifier ~ """[a-zA-Z]+""".r ^^ {
      case _ ~ _ ~ w => ConversionWord(w, "")
    }
  }

  private def conversionWordArgs: Parser[String] ="""\{[^}]+\}""".r ^^ (r => r.substring(1, r.length - 1))

  private def conversionWorkWithArgs: Parser[LoggerAst] = conversionWord ~ conversionWordArgs ^^ { case w ~ a => ConversionWord(w.word, a) }

  private def literalString: Parser[LoggerAst] = "[^%]+".r ^^ (x => LiteralAst(x))

  def tokens(): Parser[List[LoggerAst]] = {
    phrase(rep1(conversionWorkWithArgs | conversionWord | literalString))
  }
}




