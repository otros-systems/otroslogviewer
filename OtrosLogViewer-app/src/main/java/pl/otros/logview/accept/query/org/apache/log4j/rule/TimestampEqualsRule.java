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
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A Rule class implementing equality evaluation for timestamps.
 * 
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Krzysztof Otrebski
 */
public class TimestampEqualsRule extends AbstractRule {

  private static final long SECOND = 1000;
  private static final long MINUTE = 60 * SECOND;
  private static final long HOUR = 60 * MINUTE;
  private static final long DAY = 24 * HOUR;

  private static final Logger LOGGER = LoggerFactory.getLogger(TimestampEqualsRule.class.getName());
  /**
   * Serialization ID.
   */
  private static final long serialVersionUID = 1639079557187790321L;
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

  private static final long[] DATE_DURATIONS = {//
  SECOND,//
      MINUTE,//
      HOUR,//
      SECOND,//
      MINUTE,//
      HOUR,//
      DAY,//
  };
  /**
   * Time format.
   */
  private static final SimpleDateFormat[] TIME_FORMATS = {//
  new SimpleDateFormat("HH:mm:ss"),//
      new SimpleDateFormat("HH:mm"), //
  };

  private static final long[] TIME_DURATIONS = {//
  SECOND,//
      MINUTE,//
  };

  /**
   * time stamp.
   */
  private long timeStamp;
  private long duration;

  /**
   * Create new instance.
   * 
   * @param value
   *          string representation of date.
   */
  private TimestampEqualsRule(final String value) {
    super();
    boolean dateFormatFound = false;
    for (int i = 0; i < DATE_FORMATS.length; i++) {
      SimpleDateFormat df = DATE_FORMATS[i];
      duration = DATE_DURATIONS[i];
      try {
        Date parse = df.parse(value);
        timeStamp = parse.getTime();
        dateFormatFound = true;
        LOGGER.debug(String.format("Date format for %s detected: %s with duration %dms", value, df.toPattern(), duration));
        break;
      } catch (ParseException pe) {
        // check next log format
      }
    }
    if (!dateFormatFound) {
      for (int i = 0; i < TIME_FORMATS.length; i++) {
        SimpleDateFormat df = TIME_FORMATS[i];
        duration = TIME_DURATIONS[i];
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
          LOGGER.debug(String.format("Date format for %s detected: %s with duration %dms", value, df.toPattern(), duration));
          dateFormatFound = true;
          break;
        } catch (ParseException pe) {
          // check next log format
        }
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
   * @param value
   *          string representation of date.
   * @return new instance
   */
  public static Rule getRule(final String value) {
    return new TimestampEqualsRule(value);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean evaluate(final LogData event, Map matches) {
    String eventTimeStampString = RESOLVER.getValue(LoggingEventFieldResolver.TIMESTAMP_FIELD, event).toString();
    long eventTimeStamp = Long.parseLong(eventTimeStampString) / 1000 * 1000;
    boolean result = (eventTimeStamp >= timeStamp && eventTimeStamp < timeStamp + duration);
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
   *           if class not found during deserialization
   */
  private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    timeStamp = in.readLong();
    duration = in.readLong();
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
    out.writeLong(timeStamp);
    out.writeLong(duration);
  }
}
