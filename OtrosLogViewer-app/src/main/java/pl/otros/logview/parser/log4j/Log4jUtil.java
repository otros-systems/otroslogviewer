/*
 Copyright 2011 Krzysztof Otrebski
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package pl.otros.logview.parser.log4j;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.RenamedLevel;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.model.LogData;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log4jUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(Log4jUtil.class.getName());
  private static final Map<String, String> IMMUTABLE_EMPTY_MAP = new ImmutableMap.Builder<String, String>().build();
  public static final String CONVERSION_PATTERN = "conversionPattern";

  public static LogData translateLog4j(LoggingEvent event) {
    LogData ld = new LogData();
    ld.setDate(new Date(event.getTimeStamp()));
    StringBuilder sb = new StringBuilder();
    sb.append(event.getMessage());
    if (event.getThrowableInformation() != null) {
      String[] throwableStrRep = event.getThrowableInformation().getThrowableStrRep();
      for (String string : throwableStrRep) {
        sb.append('\n');
        sb.append(string);
      }
    }
    ld.setMessage(sb.toString().trim());

    ld.setLevel(parseLevel(event.getLevel().toString()));
    ld.setClazz(event.getLocationInformation().getClassName());
    ld.setMethod(event.getLocationInformation().getMethodName());
    ld.setFile(event.getLocationInformation().getFileName());
    ld.setLine(event.getLocationInformation().getLineNumber());
    ld.setNDC(event.getNDC());
    ld.setThread(event.getThreadName());
    ld.setLoggerName(event.getLoggerName());

    ld.setProperties(IMMUTABLE_EMPTY_MAP);
    Map properties = event.getProperties();
    if (properties != null) {
      Map<String, String> props = new HashMap<>(properties.size());
      for (Object key : properties.keySet()) {
        String value = Optional
          .ofNullable(properties.get(key))
          .map(Object::toString)
          .orElse("");
        if (StringUtils.isNotBlank(value)) {
          props.put(key.toString(), value);
        }
      }
      if (props.size() > 0) {
        ld.setProperties(props);
      }
    }

    return ld;
  }

  public static Level parseLevel(String s) {
    if (s.equalsIgnoreCase("INFO")) {
      return Level.INFO;
    } else if (s.equalsIgnoreCase("ERROR")) {
      return RenamedLevel.ERROR;
    } else if (s.equalsIgnoreCase("FATAL")) {
      return RenamedLevel.FATAL;
    } else if (s.equalsIgnoreCase("WARN")) {
      return RenamedLevel.WARN;
    } else if (s.equalsIgnoreCase("DEBUG")) {
      return RenamedLevel.DEBUG;
    } else if (s.equalsIgnoreCase("TRACE")) {
      return RenamedLevel.TRACE;
    }
    LOGGER.error("Level \"" + s + "\" not parsed!");
    return null;
  }

  public static void parsePattern(Properties p) throws InitializationException {
    String conversionPattern = p.getProperty(CONVERSION_PATTERN);
    if (conversionPattern == null) {
      throw new InitializationException("Log " + CONVERSION_PATTERN + " is not set.");
    }

    Matcher dateFormatMatcher = Pattern.compile("%-?\\d*(?:\\.\\d+)?d(?:\\{([^}]+)})?").matcher(conversionPattern);
    String dateFormat = "yyyy-MM-dd HH:mm:ss,SSS"; //ISO8601
    if (dateFormatMatcher.find() && dateFormatMatcher.groupCount() >= 1) {
      if (dateFormatMatcher.group(1).equals("ABSOLUTE")) {
        dateFormat = "HH:mm:ss,SSS";
      } else {
        dateFormat = dateFormatMatcher.group(1);
      }
    }
    p.setProperty("dateFormat", dateFormat);

    String parserPattern = p.getProperty(CONVERSION_PATTERN, "");

    Map<Character, String> conversionCharacters = new HashMap<>();
    conversionCharacters.put('C', "CLASS");
    conversionCharacters.put('c', "CLASS");
    conversionCharacters.put('d', "TIMESTAMP");
    conversionCharacters.put('F', "FILE");
//        conversionCharacters.put('l', "");//Location ?
    conversionCharacters.put('L', "LINE");
    conversionCharacters.put('m', "MESSAGE");
    conversionCharacters.put('M', "METHOD");
    conversionCharacters.put('n', ""); // New Line
    conversionCharacters.put('p', "LEVEL");
//        conversionCharacters.put('r', "");//Milliseconds
    conversionCharacters.put('t', "THREAD");
    conversionCharacters.put('x', "NDC");

    for (Map.Entry<Character, String> conversion : conversionCharacters.entrySet()) {
      parserPattern = parserPattern.replaceAll("%-?\\d*(?:\\.\\d+)?" + conversion.getKey() + "(?:\\{([^}]+)})?", conversion.getValue());
    }
    p.setProperty("pattern", parserPattern);
  }
}
