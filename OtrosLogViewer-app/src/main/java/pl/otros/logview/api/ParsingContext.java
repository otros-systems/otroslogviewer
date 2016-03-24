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
package pl.otros.logview.api;

import java.text.DateFormat;
import java.util.HashMap;

public class ParsingContext {

  private StringBuilder unmatchedLog;
  private long lastParsed = 0;
  private int generatedId = 0;
  private volatile boolean parsingInProgress = true;
  private String name;
  private String logSource;
  private HashMap<String, Object> customConextProperties;
  private DateFormat dateFormat;

  public ParsingContext() {
    this("?");
  }

  public ParsingContext(String name) {
    this(name, null);
  }

  public ParsingContext(String name, String logSource) {
    this.name = name;
    this.logSource = logSource;
    unmatchedLog = new StringBuilder();
    customConextProperties = new HashMap<>();
  }

  public HashMap<String, Object> getCustomConextProperties() {
    return customConextProperties;
  }

  public void setCustomConextProperties(HashMap<String, Object> customConextProperties) {
    this.customConextProperties = customConextProperties;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isParsingInProgress() {
    return parsingInProgress;
  }

  public void setParsingInProgress(boolean parsingInProgress) {
    this.parsingInProgress = parsingInProgress;
  }

  public int getGeneratedId() {
    return generatedId;
  }

  public int getGeneratedIdAndIncrease() {
    return generatedId++;
  }

  public void setGeneratedId(int generatedId) {
    this.generatedId = generatedId;
  }

  public long getLastParsed() {
    return lastParsed;
  }

  public void setLastParsed(long lastParsed) {
    this.lastParsed = lastParsed;
  }

  public StringBuilder getUnmatchedLog() {
    return unmatchedLog;
  }

  public void setUnmatchedLog(StringBuilder unmatchedLog) {
    this.unmatchedLog = unmatchedLog;
  }

  public String getLogSource() {
    return logSource;
  }

  public void setLogSource(String logSource) {
    this.logSource = logSource;
  }

  public DateFormat getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }
}
