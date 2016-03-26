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

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver.DATE_FIELD;
import static pl.otros.logview.accept.query.org.apache.log4j.spi.LoggingEventFieldResolver.TIMESTAMP_FIELD;

/**
 * A Rule class implementing inequality evaluation. expects to be able to convert two values to longs. If a specific inequality evaluation class has been
 * provided for the event field, the appropriate rule is returned. (For example, if the expression is Level &lt DEBUG, a LevelInequalityRule is returned).
 * 
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Krzysztof Otrebski
 */
public class InequalityRule extends AbstractRule {

  /**
   * Serialization ID.
   */
   private static final long serialVersionUID = -5592986598528885122L;
  /**
   * field RESOLVER.
   */
  private static final LoggingEventFieldResolver RESOLVER = LoggingEventFieldResolver.getInstance();
  /**
   * Field name.
   */
  private final String field;
  /**
   * Comparison value.
   */
  private final String value;
  /**
   * Inequality symbol.
   */
  private final String inequalitySymbol;

  /**
   * Create new instance.
   * 
   * @param inequalitySymbol
   *          inequality symbol.
   * @param field
   *          field
   * @param value
   *          comparison value.
   */
  private InequalityRule(final String inequalitySymbol, final String field, final String value) {
    super();
    this.inequalitySymbol = inequalitySymbol;
    if (!RESOLVER.isField(field)) {
      throw new IllegalArgumentException("Invalid " + inequalitySymbol + " rule - " + field + " is not a supported field");
    }

    this.field = field;
    this.value = value;
  }

  /**
   * Create new instance from top two elements on stack.
   * 
   * @param inequalitySymbol
   *          inequality symbol.
   * @param stack
   *          stack.
   * @return rule.
   */
  public static Rule getRule(final String inequalitySymbol, final Stack<Object> stack) {
    if (stack.size() < 2) {
      throw new IllegalArgumentException("Invalid " + inequalitySymbol + " rule - expected two parameters but received " + stack.size());
    }

    String p2 = stack.pop().toString();
    String p1 = stack.pop().toString();
    return getRule(inequalitySymbol, p1, p2);
  }

  /**
   * Create new instance from top two elements on stack.
   * 
   * @param inequalitySymbol
   *          inequality symbol.
   * @param field
   *          field.
   * @param value
   *          comparison value.
   * @return rule.
   */
  public static Rule getRule(final String inequalitySymbol, final String field, final String value) {
    if (field.equalsIgnoreCase(LoggingEventFieldResolver.LEVEL_FIELD)) {
      // push the value back on the stack and
      // allow the level-specific rule pop values
      return LevelInequalityRule.getRule(inequalitySymbol, value);
    } else if (field.equalsIgnoreCase(TIMESTAMP_FIELD) || field.equalsIgnoreCase(DATE_FIELD)) {
      return TimestampInequalityRule.getRule(inequalitySymbol, value);
    } else {
      return new InequalityRule(inequalitySymbol, field, value);
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean evaluate(final LogData event, Map matches) {
    long first;
    try {
      first = Long.parseLong(RESOLVER.getValue(field, event).toString());
    } catch (NumberFormatException nfe) {
      return false;
    }

    long second;

    try {
      second = Long.parseLong(value);
    } catch (NumberFormatException nfe) {
      return false;
    }

    boolean result = false;

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
      Set entries = (Set) matches.get(field.toUpperCase());
      if (entries == null) {
        entries = new HashSet();
        matches.put(field.toUpperCase(), entries);
      }
      entries.add(String.valueOf(first));
    }
    return result;
  }
}
