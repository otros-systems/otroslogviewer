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
package pl.otros.logview;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

public class LogDataBuilder {

  private LogData ld;

  public LogDataBuilder() {
    ld = new LogData();
  }

  public LogData build() {
    return ld;
  }

  public LogDataBuilder withId(int id) {
    ld.setId(id);
    return this;
  }

  public LogDataBuilder withMessage(String message) {
    ld.setMessage(message);
    return this;
  }

  public LogDataBuilder withClass(String clazz) {
    ld.setClazz(clazz);
    return this;
  }

  public LogDataBuilder withDate(Date date) {
    ld.setDate(date);
    return this;
  }

  public LogDataBuilder withLevel(Level level) {
    ld.setLevel(level);
    return this;
  }

  public LogDataBuilder withLoggerName(String loggerName) {
    ld.setLoggerName(loggerName);
    return this;
  }

  public LogDataBuilder withMarked(boolean marked) {
    ld.setMarked(marked);
    return this;
  }

  public LogDataBuilder withMarkerColors(MarkerColors markerColors) {
    ld.setMarkerColors(markerColors);
    return this;
  }

  public LogDataBuilder withMessageId(String messageId) {
    ld.setMessageId(messageId);
    return this;
  }

  public LogDataBuilder withMethod(String method) {
    ld.setMethod(method);
    return this;
  }

  public LogDataBuilder withNote(Note note) {
    ld.setNote(note);
    return this;
  }

  public LogDataBuilder withProperties(Map<String, String> properties) {
    ld.setProperties(properties);
    return this;
  }

  public LogDataBuilder withThread(String thread) {
    ld.setThread(thread);
    return this;
  }

  public LogDataBuilder withLogSource(String logSource) {
    ld.setLogSource(logSource);
    return this;
  }

}
