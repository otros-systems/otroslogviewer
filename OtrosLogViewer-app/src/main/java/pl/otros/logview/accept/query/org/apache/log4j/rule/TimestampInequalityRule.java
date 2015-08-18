/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.accept.query.org.apache.log4j.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.LogData;
import pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Rule class implementing inequality evaluation for timestamps.
 * 
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Krzysztof Otrebski
 */
public class TimestampInequalityRule extends AbstractRule {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimestampEqualsRule.class.getName());

  /**
   * Serialization ID.
   */
  static final long serialVersionUID = -4642641663914789241L;
  /**
   * Resolver.
   */
  private static final LoggingEventFieldResolver RESOLVER = LoggingEventFieldResolver.getInstance();
  /**
   * Date format.
   */
  private static final SimpleDateFormat[] DATE_FORMATS = {//
  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),//
      new SimpleDateFormat("yyyy-MM-dd HH:mm"),//
      new SimpleDateFormat("yyyy-MM-dd HH"),//
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),//
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"),//
      new SimpleDateFormat("yyyy-MM-dd'T'HH"),//
      new SimpleDateFormat("yyyy-MM-dd"),//

  };
  /**
   * Time format.
   */
  private static final SimpleDateFormat[] TIME_FORMATS = {//
  new SimpleDateFormat("HH:mm:ss"),//
      new SimpleDateFormat("HH:mm") //
  };

  /**
   * Inequality symbol.
   */
  private transient String inequalitySymbol;
  /**
   * Timestamp.
   */
  private long timeStamp;

  /**
   * Create new instance.
   * 
   * @param inequalitySymbol
   *          inequality symbol.
   * @param value
   *          string representation of date.
   */
  private TimestampInequalityRule(final String inequalitySymbol, final String value) {
    super();
    this.inequalitySymbol = inequalitySymbol;
    boolean dateFormatFound = false;
    for (SimpleDateFormat df : DATE_FORMATS) {
      try {
        Date parse = df.parse(value);
        timeStamp = parse.getTime();
        dateFormatFound = true;
        LOGGER.debug(String.format("Date format for %s detected: %s", value, df.toPattern()));
        break;
      } catch (ParseException pe) {
        // check next log format
      }
    }
    if (!dateFormatFound) {
      for (SimpleDateFormat df : TIME_FORMATS) {
        try {
          Date parse = df.parse(value);
          GregorianCalendar calendar = new GregorianCalendar();
          calendar.setTime(parse);

          GregorianCalendar todayCal = new GregorianCalendar();
          todayCal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
          todayCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
          todayCal.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
          todayCal.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
          timeStamp = todayCal.getTimeInMillis();
          LOGGER.debug(String.format("Date format for %s detected: %s", value, df.toPattern()));
          dateFormatFound = true;
          break;
        } catch (ParseException pe) {
          // check next log format
        }
      }
    }

    if (!dateFormatFound) {
      long count = -1;
      long unit = 0;
      if (value.matches("-\\d+m(in)?") || value.matches("-\\d+minutes?")) {
        Matcher matcher = Pattern.compile("-(\\d+)m(in)?").matcher(value);
        if (matcher.find()) {
          count = Integer.parseInt(matcher.group(1));
          unit = 60 * 1000;
          LOGGER.debug(String.format("Date format is -%d minutes", count));
        }
      } else if (value.matches("-\\d+h(ours?)?")) {
        Matcher matcher = Pattern.compile("-(\\d+)h(ours?)?").matcher(value);
        if (matcher.find()) {
          count = Integer.parseInt(matcher.group(1));
          unit = 60 * 60 * 1000;
          LOGGER.debug(String.format("Date format is -%d hours", count));
        }
      } else if (value.matches("-\\d+d(ays?)?")) {
        Matcher matcher = Pattern.compile("-(\\d+)d(ays?)?").matcher(value);
        if (matcher.find()) {
          count = Integer.parseInt(matcher.group(1));
          unit = 24 * 60 * 60 * 1000;
          LOGGER.debug(String.format("Date format is -%d days", count));
        }
      }
      if (count > 0) {
        timeStamp = System.currentTimeMillis() - (count * unit);
        dateFormatFound = true;
      }

    }

    if (!dateFormatFound) {
      LOGGER.debug(String.format("Date format for %s is not found", value));
      throw new IllegalArgumentException("Could not parse date: " + value);
    }
  }

  /**
   * Create new instance.
   * 
   * @param inequalitySymbol
   *          inequality symbol
   * @param value
   *          string representation of date
   * @return new instance
   */
  public static Rule getRule(final String inequalitySymbol, final String value) {
    return new TimestampInequalityRule(inequalitySymbol, value);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean evaluate(final LogData event, Map matches) {
    String eventTimeStampString = RESOLVER.getValue(LoggingEventFieldResolver.TIMESTAMP_FIELD, event).toString();
    // long eventTimeStamp = Long.parseLong(
    // eventTimeStampString) / 1000 * 1000;
    long eventTimeStamp = event.getDate().getTime();
    boolean result = false;
    long first = eventTimeStamp;
    long second = timeStamp;
    if ("<".equals(inequalitySymbol)) {
      result = first < second;
    } else if (">".equals(inequalitySymbol)) {
      result = first > second;
    } else if ("<=".equals(inequalitySymbol)) {
      result = first <= second;
    } else if (">=".equals(inequalitySymbol)) {
      result = first >= second;
    }
    if (result && matches != null) {
      Set entries = (Set) matches.get(LoggingEventFieldResolver.TIMESTAMP_FIELD);
      if (entries == null) {
        entries = new HashSet();
        matches.put(LoggingEventFieldResolver.TIMESTAMP_FIELD, entries);
      }
      entries.add(eventTimeStampString);
    }
    return result;
  }

  /**
   * Deserialize the state of the object.
   * 
   * @param in
   *          object input stream
   * @throws IOException
   *           if IO error during deserialization
   * @throws ClassNotFoundException
   *           if class not found
   */
  private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    inequalitySymbol = (String) in.readObject();
    timeStamp = in.readLong();
  }

  /**
   * Serialize the state of the object.
   * 
   * @param out
   *          object output stream
   * @throws IOException
   *           if IO error during serialization
   */
  private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(inequalitySymbol);
    out.writeLong(timeStamp);
  }
}
