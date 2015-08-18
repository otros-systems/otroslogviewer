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
package pl.otros.logview.gui.message;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoapFinder {

  static Pattern p1 = Pattern.compile("xmlns:(.+?)=\"http://schemas\\.xmlsoap\\.org/soap/envelope/\"", Pattern.MULTILINE);
  // static Pattern p2 = Pattern.compile("xmlns:(.+?)=\"http://www\\.w3\\.org/2003/05/soap-envelope\"", Pattern.MULTILINE);
  static Pattern p2 = Pattern.compile("xmlns:(.+?)=\"http://www\\.w3\\.org/\\d+/\\d+/soap-envelope\"", Pattern.MULTILINE);
  // http://www.w3.org/2002/06/soap-envelope
  static Pattern[] patterns = { p1, p2 };

  public String findSoapTag(String string) {
    String result = null;

    for (Pattern pattern : patterns) {
      Matcher m = pattern.matcher(string);
      boolean found = m.find();
      if (found) {
        result = m.group(1);
        break;
      }
    }
    return result;
  }

  public SortedSet<SubText> findSoaps(String text) {
    SortedSet<SubText> set = new TreeSet<>();
    String tag = findSoapTag(text);
    if (tag == null) {
      return set;
    }
    String soapStartTag = "<" + tag + ":Envelope";
    String soapEndTag = "</" + tag + ":Envelope>";
    int end = 0;
    int start = 0;
    String s = text;
    Pattern startPatern = Pattern.compile("((<\\?xml .*?\\?>[\\s\n]*)?" + soapStartTag + ")");
    Matcher startMatcher = startPatern.matcher(s);
    while (startMatcher.find()) {
      start = startMatcher.start(1);
      end = s.indexOf(soapEndTag, start);
      if (start < 0 || end < 0) {
        break;
      }
      SubText subText = new SubText(start, end + soapEndTag.length());
      set.add(subText);
    }
    // while (true) {
    // start = s.indexOf(soapStartTag, end);
    // end = s.indexOf(soapEndTag, start);
    // if (start < 0 || end < 0) {
    // break;
    // }
    // Pattern p = Pattern.compile(".*(<\\?xml .*?\\?>)[\\s\n]*" + soapStartTag, Pattern.CASE_INSENSITIVE);
    // String sub = text.substring(0, start + soapStartTag.length());
    // Matcher matcher = p.matcher(sub);
    // if (matcher.find()) {
    // start = matcher.start(1);
    // }
    // SubText subText = new SubText(start, end + soapEndTag.length());
    // set.add(subText);
    // }

    return set;
  }

}
