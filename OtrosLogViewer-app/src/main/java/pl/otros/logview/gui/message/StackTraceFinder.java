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

import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackTraceFinder {

  private static final Pattern exceptionLine = Pattern.compile("(\\s*at\\s+([\\w\\d\\.]*\\.)?([\\w\\d\\$]+)\\.([\\d\\w<>]+)\\(([\\d\\w\\.\\u0020:]+)\\))");

  public SortedSet<SubText> findStackTraces(String text) {
    SortedSet<SubText> set = new TreeSet<SubText>();
    LinkedList<Integer> newLineIndexes = new LinkedList<Integer>();
    newLineIndexes.add(0);
    int newLineIndex = -1;
    while ((newLineIndex = StringUtils.indexOf(text, '\n', newLineIndex + 1)) > -1) {
      newLineIndexes.addLast(newLineIndex);
    }
    if (newLineIndexes.getLast() < text.length()) {
      newLineIndexes.addLast(text.length());
    }

    int startLine;
    int endLine;
    boolean found = false;
    int startedLineException = -1;
    for (int i = 0; i < newLineIndexes.size() - 1; i++) {
      startLine = newLineIndexes.get(i);
      endLine = newLineIndexes.get(i + 1);
      String line = text.substring(startLine, endLine);
      Matcher matcher = exceptionLine.matcher(line);
      boolean f = matcher.find();
      if (f && !found) {
        startedLineException = i - 1;
      } else if (!f && found) {
        // exception end
        int start = newLineIndexes.get(startedLineException);
        int end = newLineIndexes.get(i);
        SubText subText = new SubText(start, end);
        set.add(subText);
      }
      found = f;
    }
    // Add stacktrace if string with end of stacktrace
    if (found) {
      int start = newLineIndexes.get(startedLineException);
      int end = newLineIndexes.getLast();
      SubText subText = new SubText(start, end);
      set.add(subText);
    }
    // for (SubText subText : set) {
    // System.out.printf("[%d -> %d] \n\"%s\"", subText.start, subText.end, text.substring(subText.start, subText.end));
    // }
    return set;
  }
}
