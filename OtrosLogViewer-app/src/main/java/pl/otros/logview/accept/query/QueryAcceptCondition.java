/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package pl.otros.logview.accept.query;

import pl.otros.logview.LogData;
import pl.otros.logview.accept.AcceptCondition;
import pl.otros.logview.accept.query.org.apache.log4j.rule.ExpressionRule;
import pl.otros.logview.accept.query.org.apache.log4j.rule.Rule;
import pl.otros.logview.accept.query.org.apache.log4j.rule.RuleException;

import java.util.HashMap;

public class QueryAcceptCondition implements AcceptCondition {

  private static final String DESCRIPTION = "Query - desc";
  private static final String NAME = "Query";
  private final String query;
  private final Rule rule;

  public QueryAcceptCondition(String query) throws RuleException {
    super();
    this.query = query;
    try {
      rule = ExpressionRule.getRule(query, false);
    } catch (Exception e) {
      throw new RuleException(e);
    }
  }

  @Override
  public boolean accept(LogData data) {
    HashMap<Object, Object> matches = new HashMap<>();
    boolean evaluate = rule.evaluate(data, matches);
    return evaluate;
  }

  @Override
  public String getName() {
    return NAME + ": " + query;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

}
