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

import pl.otros.logview.api.LogData;
import pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver;

import java.util.*;
import java.util.logging.Level;

/**
 * A Rule class implementing inequality evaluation for Levels (log4j and util.logging) using the toInt method.
 * 
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Krzysztof Otrebski
 */
public class LevelInequalityRule {

  /**
   * Level list.
   */
  private static List<String> levelList;

  static {
    populateLevels();
  }

  /**
   * Create new instance.
   */
  private LevelInequalityRule() {
    super();
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
   * @param inequalitySymbol
   *          inequality symbol.
   * @param value
   *          Symbolic name of comparison level.
   * @return instance of AbstractRule.
   */
  public static Rule getRule(final String inequalitySymbol, final String value) {

    Level thisLevel;

    // if valid util.logging levels are used against events
    // with log4j levels, the
    // DEBUG level is used and an illegalargumentexception won't be generated

    // an illegalargumentexception is only generated
    // if the user types a level name
    // that doesn't exist as either a log4j or util.logging level name
    if (levelList.contains(value.toUpperCase())) {
      thisLevel = Level.parse(value.toUpperCase());
    } else {
      throw new IllegalArgumentException("Invalid level inequality rule - " + value + " is not a supported level");
    }

    if ("<".equals(inequalitySymbol)) {
      return new LessThanRule(thisLevel);
    }
    if (">".equals(inequalitySymbol)) {
      return new GreaterThanRule(thisLevel);
    }
    if ("<=".equals(inequalitySymbol)) {
      return new LessThanEqualsRule(thisLevel);
    }
    if (">=".equals(inequalitySymbol)) {
      return new GreaterThanEqualsRule(thisLevel);
    }

    return null;
  }

  /**
   * Rule returning true if event level less than specified level.
   */
  @SuppressWarnings({ "rawtypes", "unchecked", "serial" })
  private static final class LessThanRule extends AbstractRule {

    /**
     * Comparison level.
     */
    private final int newLevelInt;

    /**
     * Create new instance.
     * 
     * @param level
     *          comparison level.
     */
    public LessThanRule(final Level level) {
      super();
      newLevelInt = level.intValue();
    }

    /**
     * {@inheritDoc}
     */
    public boolean evaluate(final LogData event, Map matches) {
      Level eventLevel = event.getLevel();
      boolean result = (eventLevel.intValue() < newLevelInt);
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
  }

  /**
   * Rule returning true if event level greater than specified level.
   */
  @SuppressWarnings("serial")
  private static final class GreaterThanRule extends AbstractRule {

    /**
     * Comparison level.
     */
    private final int newLevelInt;

    /**
     * Create new instance.
     * 
     * @param level
     *          comparison level.
     */
    public GreaterThanRule(final Level level) {
      super();
      newLevelInt = level.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean evaluate(final LogData event, Map matches) {
      Level eventLevel = event.getLevel();
      boolean result = (eventLevel.intValue() > newLevelInt);
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
  }

  /**
   * Rule returning true if event level greater than or equal to specified level.
   */

  @SuppressWarnings("serial")
  private static final class GreaterThanEqualsRule extends AbstractRule {

    /**
     * Comparison level.
     */
    private final int newLevelInt;

    /**
     * Create new instance.
     * 
     * @param level
     *          comparison level.
     */
    public GreaterThanEqualsRule(final Level level) {
      super();
      newLevelInt = level.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean evaluate(final LogData event, Map matches) {
      Level eventLevel = event.getLevel();
      boolean result = eventLevel.intValue() >= newLevelInt;
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
  }

  /**
   * Rule returning true if event level less than or equal to specified level.
   */

  @SuppressWarnings("serial")
  private static final class LessThanEqualsRule extends AbstractRule {

    /**
     * Comparison level.
     */
    private final int newLevelInt;

    /**
     * Create new instance.
     * 
     * @param level
     *          comparison level.
     */
    public LessThanEqualsRule(final Level level) {
      super();
      newLevelInt = level.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean evaluate(final LogData event, Map matches) {
      Level eventLevel = event.getLevel();
      boolean result = eventLevel.intValue() <= newLevelInt;
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
  }
}
