package pl.otros.logview.util;

import org.testng.annotations.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggerConfigUtilTest {

  @Test
  public void testExtractLayoutPatternsLog4jXml() {
    //Given
    String content = "<appender name=\"console\" class=\"org.apache.log4j.ConsoleAppender\">\n" +
      "\t\t<param name=\"Target\" value=\"System.out\" />\n" +
      "\t\t<layout class=\"org.apache.log4j.PatternLayout\">\n" +
      "\t\t\t<param name=\"ConversionPattern\" value=\"LOG4j11  %d %-5p: %c - %m%n\" />\n" +
      "\t\t</layout>\n" +
      "\t</appender>\n" +
      "\t<appender name=\"file\" class=\"org.apache.log4j.FileAppender\">\n" +
      "\t\t<param name=\"File\" value=\"blog-log.txt\" />\n" +
      "\t\t<param name=\"Append\" value=\"true\" />\n" +
      "\t\t<layout class=\"org.apache.log4j.PatternLayout\">\n" +
      "\t\t\t<param \n" +
      "  name=\"ConversionPattern\" \n" +
      "  value=\"LOG4j12 %d %-5p: %c - %m%n\" />\n" +
      "\t\t</layout>";

    //when
    final Set<String> strings = LoggerConfigUtil.extractLayoutPatterns(content);
    //then
    assertThat(strings).containsOnly("LOG4j11  %d %-5p: %c - %m%n", "LOG4j12 %d %-5p: %c - %m%n");
  }

  @Test
  public void testExtractLayoutPatternsLog4jProperties() {
    //Given
    String content = "log4j.appender.file.MaxBackupIndex=10\n" +
      "log4j.appender.file.layout=org.apache.log4j.PatternLayout\n" +
      "log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n\n" +
      "\n" +
      "# Direct log messages to stdout\n" +
      "log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n" +
      "log4j.appender.stdout.Target=System.out\n" +
      "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout\n" +
      "log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n";

    //when
    final Set<String> strings = LoggerConfigUtil.extractLayoutPatterns(content);
    //then
    assertThat(strings).containsOnly("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
  }

  @Test
  public void testExtractLayoutPatternsLogbackXml() {
    //Given
    String content = "<configuration>\n" +
      "\n" +
      "      <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n" +
      "            <maxFileSize>35 MB</maxFileSize>\n" +
      "        </triggeringPolicy>\n" +
      "        <encoder>\n" +
      "            <pattern>%-5level %date{dd MMM yyyy;HH:mm:ss.SSS} [%thread] %logger %msg%n</pattern>\n" +
      "        </encoder>\n" +
      "    </appender>\n" +
      "\n" +
      "      <triggeringPolicy class=\"ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy\">\n" +
      "            <maxFileSize>35 MB</maxFileSize>\n" +
      "        </triggeringPolicy>\n" +
      "        <encoder>\n" +
      "            <pattern>\n" +
      "                 %level %date{dd MMM yyyy;HH:mm:ss.SSS} %thread %file %logger %msg%n\n" +
      "          </pattern>\n" +
      "        </encoder>\n" +
      "</configuration>\n";

    //when
    final Set<String> strings = LoggerConfigUtil.extractLayoutPatterns(content);
    //then
    assertThat(strings).containsOnly(
      "%-5level %date{dd MMM yyyy;HH:mm:ss.SSS} [%thread] %logger %msg%n",
      "%level %date{dd MMM yyyy;HH:mm:ss.SSS} %thread %file %logger %msg%n");
  }

  @Test
  public void testExtractLayoutPatternsJustPatterns() {
    //Given
    String content = "%-5level %date{dd MMM yyyy;HH:mm:ss.SSS} [%thread] %logger %msg%n\n" +
      "                 %level %date{dd MMM yyyy;HH:mm:ss.SSS} %thread %file %logger %msg%n\n" +
      "some trash";

    //when
    final Set<String> strings = LoggerConfigUtil.extractLayoutPatterns(content);
    //then
    assertThat(strings).containsOnly(
      "%-5level %date{dd MMM yyyy;HH:mm:ss.SSS} [%thread] %logger %msg%n",
      "%level %date{dd MMM yyyy;HH:mm:ss.SSS} %thread %file %logger %msg%n");
  }

}