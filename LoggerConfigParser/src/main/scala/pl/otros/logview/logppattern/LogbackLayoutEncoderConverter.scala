package pl.otros.logview.logppattern

import java.util.Properties

import pl.otros.logview.api.LayoutEncoderConverter

class LogbackLayoutEncoderConverter extends LayoutEncoderConverter {
  override def convert(layoutPattern: String): Properties = {
    val result = LogbackPatternConverter.convert(layoutPattern)
    result match {
      case Right(map) =>
        val prop = new Properties()
        map.foreach { kv => prop.setProperty(kv._1, kv._2) }
        prop
      case Left(error) => throw new Exception(error)
    }
  }
}
