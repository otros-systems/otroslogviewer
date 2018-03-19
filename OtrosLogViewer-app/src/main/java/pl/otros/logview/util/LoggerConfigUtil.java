package pl.otros.logview.util;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.LayoutEncoderConverter;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.logppattern.LogbackLayoutEncoderConverter;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LoggerConfigUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfigUtil.class.getName());

  @NotNull
  public static Set<String> extractLayoutPatterns(String content) {
    final LayoutEncoderConverter logbackLayoutEncoderConverter = new LogbackLayoutEncoderConverter();

    Set<String> result = new HashSet<>();
    final Matcher logbackMatcher = Pattern.compile("<pattern>\\s*(.*?)\\s*</pattern>", Pattern.MULTILINE).matcher(content);
    while (logbackMatcher.find()) {
      result.add(logbackMatcher.group(1));
    }

    final List<String> barePatterns = Splitter
      .onPattern("\r?\n")
      .trimResults()
      .omitEmptyStrings()
      .splitToList(content)
      .stream()
      .filter(line -> !line.contains("value=\"")) //log4j.xml
      .filter(line -> !line.contains("ConversionPattern=\"")) //log4j.properties
      .filter(line -> !line.contains("<pattern>\"")) //logback.xml
      .filter(line -> !line.contains("<Pattern>\"")) //log4j2.xml
      .filter(line -> !line.contains("<PatternLayout>\"")) //log4j2.xml
      .filter(line -> {
        try {
          logbackLayoutEncoderConverter.convert(line);
          return true;
        } catch (Exception e) {
          return false;
        }
      }).collect(Collectors.toList());
    result.addAll(barePatterns);


    final Matcher log4jMatcher = Pattern.compile("<param\\s*name=\"ConversionPattern\"\\s*value=\"(.*?)\".*", Pattern.MULTILINE).matcher(content);
    while (log4jMatcher.find()) {
      result.add(log4jMatcher.group(1));
    }

    final Matcher log4j2Matcher = Pattern.compile("<Pattern>\\s*(.*?)\\s*</Pattern>", Pattern.MULTILINE).matcher(content);
    while (log4j2Matcher.find()) {
      result.add(log4j2Matcher.group(1));
    }

    final Matcher log4jMatcher2 = Pattern.compile("<PatternLayout.*?pattern=\"(.*?)\".*", Pattern.MULTILINE).matcher(content);
    while (log4jMatcher2.find()) {
      result.add(log4jMatcher2.group(1));
    }

    try {
      final Properties properties = new Properties();
      properties.load(new StringReader(content));
      List<String> patterns = properties
        .<String>keySet()
        .stream()
        .map(Object::toString)
        .filter(key -> key.endsWith("ConversionPattern"))
        .map(properties::getProperty)
        .collect(Collectors.toList());
      result.addAll(patterns);
    } catch (IOException e1) {
      LOGGER.warn("Can't read content as properties: " + e1.getMessage());
    }

    return result;
  }


  public static void addLog4jParser(OtrosApplication otrosApplication, String baseFileName, String name, Properties properties) {
    final Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    final LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(logParser);
    try (OutputStream out = new FileOutputStream(new File(AllPluginables.USER_LOG_IMPORTERS, baseFileName + ".pattern"))) {
      properties.setProperty("name", name);
      logImporterUsingParser.init(properties);
      otrosApplication.getAllPluginables().getLogImportersContainer().addElement(logImporterUsingParser);
      properties.store(out, "Imported log pattern");
    } catch (InitializationException | IOException e1) {
      //Ignore it
      LOGGER.error("Problem with saving Log4jPatternMultilineLogParser configuration: ", e1);
    }
  }
}
