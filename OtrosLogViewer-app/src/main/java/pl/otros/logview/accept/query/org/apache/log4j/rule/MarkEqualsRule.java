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

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.MarkerColors;
import pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Rule class implementing equality evaluation for timestamps.
 * 
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Krzysztof Otrebski
 */
public class MarkEqualsRule extends AbstractRule {

  /**
   * Serialization ID.
   */
  private static final long serialVersionUID = 1639079557187790321L;
  /**
   * Resolver.
   */
  private static final LoggingEventFieldResolver RESOLVER = LoggingEventFieldResolver.getInstance();

  /**
   * Use true/false instead of colors
   */
  private boolean useOnOffSwith = false;
  private MarkerColors markerColors;
  private boolean marked;

  /**
   * Create new instance.
   * 
   * @param value
   *          string representation of marker colors or true/false.
   */
  private MarkEqualsRule(final String value) {
    super();
    if (StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "false")) {
      marked = Boolean.valueOf(value);
      useOnOffSwith = true;
    } else {
      try {
        markerColors = MarkerColors.valueOf(value);
      } catch (Exception pe) {
        for (MarkerColors mc : MarkerColors.values()) {
          if (mc.name().equalsIgnoreCase(value)) {
            markerColors = mc;
            return;
          }
        }
        throw new IllegalArgumentException("Could not parse marker colors: " + value);
      }
    }
  }

  /**
   * Create new instance.
   * 
   * @param value
   *          string representation of marker colors or true/false.
   * @return new instance
   */
  public static Rule getRule(final String value) {
    return new MarkEqualsRule(value);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean evaluate(final LogData event, Map matches) {
    String eventMarkString = RESOLVER.getValue(LoggingEventFieldResolver.MARK_FIELD, event).toString();
    boolean result;
    if (useOnOffSwith) {
      result = marked == event.isMarked();
    } else {
      result = event.getMarkerColors() != null && event.getMarkerColors().equals(markerColors);
    }

    if (result && matches != null) {
      Set entries = (Set) matches.get(LoggingEventFieldResolver.MARK_FIELD);
      if (entries == null) {
        entries = new HashSet();
        matches.put(LoggingEventFieldResolver.MARK_FIELD, entries);
      }
      entries.add(eventMarkString);
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
    useOnOffSwith = in.readBoolean();
    markerColors = (MarkerColors) in.readObject();
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
    out.writeBoolean(useOnOffSwith);
    out.writeObject(markerColors);
  }
}
