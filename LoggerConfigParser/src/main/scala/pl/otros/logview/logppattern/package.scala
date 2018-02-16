package pl.otros.logview

package object logppattern {


  sealed trait LoggerAst


  case class ConversionWord(word: String, args: String) extends LoggerAst

  case class LiteralAst(value: String) extends LoggerAst

  def toLog4jPatternProperties(tokens: List[Token]): Map[String, String] = {
    val str: String = tokens.map(_.pattern()).mkString("").trim

    val maybeFormat = tokens
      .filter(_.isInstanceOf[Date])
      .map(_.asInstanceOf[Date])
      .headOption

    Map(
      "type" -> "log4j",
      "pattern" -> str,
      "dateFormat" -> maybeFormat.map(_.olvDateFormat()).getOrElse(""),
      "name" -> "",
      "charset" -> "UTF-8"
    )
  }


  val logbackFormat: List[LoggerAst] => List[Token] = (list: List[LoggerAst]) => {
    val logbackList = list.map {
      case LiteralAst(literal) => StringLiteral(literal)
      case
        ConversionWord("C", _) |
        ConversionWord("class", _) |
        ConversionWord("c", _) |
        ConversionWord("lo", _) |
        ConversionWord("logger", _) => LoggerClass
      case ConversionWord("contextName", _) | ConversionWord("cx", _) => ContextName
      case ConversionWord("d", format) => Date(format)
      case ConversionWord("date", format) => Date(format)
      case ConversionWord("F", _) | ConversionWord("file", _) => File
      case ConversionWord("caller", _) => Caller
      case ConversionWord("L", _) | ConversionWord("line", _) => Line
      case ConversionWord("m", _) | ConversionWord("msg", _) | ConversionWord("message", _) => Message
      case ConversionWord("M", _) | ConversionWord("method", _) => Message
      case ConversionWord("n", _) => NewLine
      case
        ConversionWord("p", _) |
        ConversionWord("priority", _) |
        ConversionWord("l", _) |
        ConversionWord("level", _) => Level
      case
        ConversionWord("r", _) |
        ConversionWord("relative", _) => Relative

      case
        ConversionWord("t", _) |
        ConversionWord("thread", _) => Thread
      case ConversionWord("X", key) => MDC(key)
      case ConversionWord("mdc", key) => MDC(key)
      case
        ConversionWord("ex", _) |
        ConversionWord("exception", _) |
        ConversionWord("throwable", _) |
        ConversionWord("xEx", _) |
        ConversionWord("xException", _) |
        ConversionWord("xThrowable", _) => Exception
      case
        ConversionWord("nopex", _) |
        ConversionWord("nopexception", _) => Thread
      case ConversionWord("marker", _) => Marker
      case
        ConversionWord("rEx", _) |
        ConversionWord("rootException", _) => RootException
      case ConversionWord(s, _) => NotSupportedToken(s)

    }
    logbackList
  }

  def mergeSomeConversionWordBeforeMessage(tokens: List[Token]): List[Token] = {
    val mergeableBefore: Set[Token] = Set(Exception, NoException, RootException, Caller)

    def mergeWithMessage(toMerge: List[Token]): List[Token] = {
      val afterMerge = (toMerge ::: List.fill(3)(StringLiteral("ignored")))
        .sliding(4)
        .flatMap {
          case StringLiteral(_) :: token :: StringLiteral(_) :: Message :: Nil if mergeableBefore.contains(token) => None
          case token :: StringLiteral(_) :: Message :: _ :: Nil if mergeableBefore.contains(token) => None
          case head :: _ => Some(head)
          case _ => None
        }.toList

      afterMerge match {
        case `toMerge` => afterMerge
        case changed => mergeWithMessage(changed)
      }
    }

    mergeWithMessage(tokens)
  }


  def validate(tokens: List[Token]): Either[String, List[Token]] = {
    val forbiddenTokensAfterMessage = tokens
      .dropWhile(_ != Message)
      .filter(_ != Message)
      .filter {
        case NewLine => false
        case StringLiteral(_) => false
        case Exception => false
        case RootException => false
        case NoException => false
        case Caller => false
        case _ => true
      }

    val mergedTokens: List[List[Token]] = tokens
      .map(t => if (t == NewLine) StringLiteral("\n") else t)
      .sliding(2)
      .filter(_.forall(!_.isInstanceOf[StringLiteral]))
      .toList

    val allMergedWithWhitechar: Boolean = tokens
      .map(t => if (t == NewLine) StringLiteral("\n") else t)
      .filter(_.isInstanceOf[StringLiteral])
      .map(_.asInstanceOf[StringLiteral])
      .forall(l => {
        val bool = l.literal.matches(".*\\s+.*")
        println(s"$l => $bool")
        bool
      })

    if (forbiddenTokensAfterMessage.nonEmpty) {
      Left(s"Forbidden token after message: ${forbiddenTokensAfterMessage.mkString(", ")}")

    } else if (tokens.find(_.isInstanceOf[MDC]).exists(_.asInstanceOf[MDC].mdc.isEmpty)) {
      Left("MDC without specified property is not supported")

    } else if (tokens.count(_ == Message) > 1) {
      Left("Message can't be used more that once")
    } else if (tokens.exists(_.isInstanceOf[NotSupportedToken])) {
      Left(s"Not supported token used: ${tokens.filter(_.isInstanceOf[NotSupportedToken]).map(_.asInstanceOf[NotSupportedToken].token).mkString(", ")}")
    } else if (mergedTokens.nonEmpty) {
      Left(s"Token are merged: ${mergedTokens.mkString(",")}")
    } else if (tokens.forall(_.isInstanceOf[StringLiteral])) {
      Left("No conversion word found")
    } else if (!allMergedWithWhitechar) {
      Left("Conversion words merged without whitespace")
    } else {
      Right(tokens)
    }
  }

  sealed trait Token {
    def pattern(): String
  }

  case class StringLiteral(literal: String) extends Token {
    override def pattern(): String = literal
  }

  case object LoggerClass extends Token {
    override def pattern(): String = "CLASS"
  }

  case object ContextName extends Token {
    override def pattern(): String = s"PROP(contextName)"
  }

  case class Date(datePattern: String) extends Token {

    def olvDateFormat(): String = {
      datePattern match {
        case "ISO8601" | "" => "yyyy-MM-dd HH:mm:ss,SSS"
        case s => s
      }
    }

    override def pattern(): String = "TIMESTAMP"
  }

  case object File extends Token {
    override def pattern(): String = "FILE"
  }

  case object Caller extends Token {
    override def pattern(): String = ""
  }

  case object Line extends Token {
    override def pattern(): String = "LINE"
  }

  case object Message extends Token {
    override def pattern(): String = "MESSAGE"
  }

  case object Method extends Token {
    override def pattern(): String = "METHOD"
  }

  case object NewLine extends Token {
    override def pattern(): String = ""
  }

  case object Level extends Token {
    override def pattern(): String = "LEVEL"
  }

  case object Relative extends Token {
    override def pattern(): String = "RELATIVE"
  }

  case object Thread extends Token {
    override def pattern(): String = "THREAD"
  }

  case class MDC(mdc: String) extends Token {
    override def pattern(): String = {
      mdc.split(",").map(_.trim).toList match {
        case single :: Nil => s"PROP($single)"
        case elements => elements.map(e => s"$e=PROP($e)").mkString(" ")
      }
    }
  }

  case object Exception extends Token {
    override def pattern(): String = ""
  }

  case object NoException extends Token {
    override def pattern(): String = ""
  }

  case object Marker extends Token {
    override def pattern(): String = "PROP(MARKER)"
  }

  case object Replace extends Token {
    override def pattern(): String = ""
  }

  case object RootException extends Token {
    override def pattern(): String = ""
  }

  case class NotSupportedToken(token: String) extends Token {
    override def pattern(): String = ""
  }

}
