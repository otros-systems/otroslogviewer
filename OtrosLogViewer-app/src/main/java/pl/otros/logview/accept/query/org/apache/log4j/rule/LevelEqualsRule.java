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

import pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver;
import pl.otros.logview.api.model.LogData;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * A Rule class implementing equals against two levels.
 *
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Krzysztof Otrebski
 */
public class LevelEqualsRule extends AbstractRule {

  /**
   * Serialization ID.
   */
  private static final long serialVersionUID = -3638386582899583994L;

  /**
   * Level.
   */
  private transient Level level;

  /**
   * List of levels.
   */
  private static List<String> levelList = new LinkedList<>();

  static {
    populateLevels();
  }

  /**
   * Create new instance.
   *
   * @param level level.
   */
  private LevelEqualsRule(final Level level) {
    super();
    this.level = level;
  }

  /**
   * Populate list of levels.
   */
  private static void populateLevels() {
    levelList = new LinkedList<>();

    levelList.add(Level.SEVERE.toString());
    levelList.add(Level.WARNING.toString());
    levelList.add(Level.INFO.toString());
    levelList.add(Level.CONFIG.toString());
    levelList.add(Level.FINE.toString());
    levelList.add(Level.FINER.toString());
    levelList.add(Level.FINEST.toString());
  }

  /**
   * Create new rule.
   *
   * @param value name of level.
   * @return instance of LevelEqualsRule.
   */
  public static Rule getRule(final String value) {
    Level thisLevel;
    if (levelList.contains(value.toUpperCase())) {
      thisLevel = Level.parse(value.toUpperCase());
    } else {
      thisLevel = Level.FINEST;
    }

    return new LevelEqualsRule(thisLevel);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public boolean evaluate(final LogData event, Map matches) {
    // both util.logging and log4j contain 'info' - use the int values instead of equality
    // info level set to the same value for both levels
    Level eventLevel = event.getLevel();
    boolean result = (level.intValue() == eventLevel.intValue());
    if (result && matches != null) {
      Set entries = (Set) matches.get(LoggingEventFieldResolver.LEVEL_FIELD);
      if (entries == null) {
        entries = new HashSet();
        matches.put(LoggingEventFieldResolver.LEVEL_FIELD, entries);
      }
      entries.add(eventLevel);
    }
    return result;
  }

  /**
   * Deserialize the state of the object.
   *
   * @param in object input stream.
   * @throws IOException if error in reading stream for deserialization.
   */
  private void readObject(final java.io.ObjectInputStream in) throws IOException {
    populateLevels();
    int levelInt = in.readInt();
    if (Level.INFO.intValue() == levelInt) {
      level = Level.INFO;
    } else if (Level.CONFIG.intValue() == levelInt) {
      level = Level.CONFIG;
    } else if (Level.FINE.intValue() == levelInt) {
      level = Level.FINE;
    } else if (Level.FINER.intValue() == levelInt) {
      level = Level.FINER;
    } else if (Level.FINEST.intValue() == levelInt) {
      level = Level.FINEST;
    } else if (Level.SEVERE.intValue() == levelInt) {
      level = Level.SEVERE;
    } else if (Level.WARNING.intValue() == levelInt) {
      level = Level.WARNING;
    } else {
      level = Level.FINEST;
    }
  }

  /**
   * Serialize the state of the object.
   *
   * @param out object output stream.
   * @throws IOException if error in writing stream during serialization.
   */
  private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
    out.writeInt(level.intValue());
  }
}
