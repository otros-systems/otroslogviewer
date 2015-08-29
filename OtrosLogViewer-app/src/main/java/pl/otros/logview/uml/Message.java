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
package pl.otros.logview.uml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

  private final MessageType type;
  private String message = "";
  private final String values = "";
  private static final Pattern getExitValuesPattern = Pattern.compile("Exit, return value = (.*)");

  public Message(String m) {
    m = m.toLowerCase();
    if (m.startsWith("entry") || m.startsWith("enter")) {
      type = MessageType.TYPE_ENTRY;
      message = m;

    } else if (m.startsWith("exit") || m.startsWith("return")) {
      type = MessageType.TYPE_EXIT;
      // Exit, return value = false
      Matcher match = getExitValuesPattern.matcher(m);
      if (match.find() && match.groupCount() == 1) {
        message = match.group(1);
      }

    } else {
      type = MessageType.TYPE_LOG;
      message = m;
    }
    if (message.length() > 90) {
      message = message.substring(0, 90);
    }

  }

  public static class MessageType {

    public static final MessageType TYPE_ENTRY = new MessageType("ENTRY");
    public static final MessageType TYPE_EXIT = new MessageType("EXIT");
    public static final MessageType TYPE_LOG = new MessageType("LOG");

    private String type = "";

    private MessageType(String type) {
      this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MessageType) {
        MessageType mt = (MessageType) obj;
        return mt.type.equalsIgnoreCase(type);
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return type.hashCode();
    }

    public String toString() {
      return type;
    }

  }

  public MessageType getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public String toString() {
    return type + ": " + message;
  }
}
