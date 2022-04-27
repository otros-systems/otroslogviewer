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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import pl.otros.logview.TestUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import static org.testng.AssertJUnit.assertEquals;

public class StackTraceFinderTest {

  private final StackTraceFinder finder = new StackTraceFinder();

  @Test
  public void testFindStackTraces() throws IOException {
    // given
    String stacktraceFile = TestUtils.getResourceFile("stacktrace/stacktrace.txt");
    AssertJUnit.assertFalse("The test expected file 'stacktrace/stacktrace.txt' in unix format.", stacktraceFile.contains("\r"));

    // when
    SortedSet<SubText> findStackTraces = finder.findStackTraces(stacktraceFile);

    // then
    assertEquals(1, findStackTraces.size());

    SubText subtext = findStackTraces.iterator().next();
    assertEquals(9, subtext.getStart());
    assertEquals(235, subtext.getEnd());
  }

  @Test
  public void testFindInOnlyStackTrace() throws IOException {
    // given
    String stacktraceFile = TestUtils.getResourceFile("stacktrace/stacktTraceOnly.txt");
    AssertJUnit.assertFalse("The test expected file 'stacktrace/stacktTraceOnly.txt' in unix format.", stacktraceFile.contains("\r"));

    // when
    SortedSet<SubText> findStackTraces = finder.findStackTraces(stacktraceFile);

    // then
    assertEquals(1, findStackTraces.size());
    SubText subtext = findStackTraces.iterator().next();
    assertEquals(0, subtext.getStart());
    assertEquals(229, subtext.getEnd());

  }

  @Test
  public void testFindForEmptyPackage() throws IOException {
    // given
    String stacktraceFile = TestUtils.getResourceFile("stacktrace/stacktTraceWtihEmptyPackage.txt");
    AssertJUnit.assertFalse("The test expected file 'stacktrace/stacktTraceWtihEmptyPackage.txt' in unix format.", stacktraceFile.contains("\r"));

    // when
    SortedSet<SubText> findStackTraces = finder.findStackTraces(stacktraceFile);

    // then
    assertEquals(1, findStackTraces.size());
    SubText subtext = findStackTraces.iterator().next();
    assertEquals(0, subtext.getStart());
    assertEquals(209, subtext.getEnd());
  }

  @Test
  public void testFind2StackTraces() throws IOException {
    // given
    String stacktraceFile = TestUtils.getResourceFile("stacktrace/stacktrace2.txt");
    AssertJUnit.assertFalse("The test expected file 'stacktrace/stacktrace2.txt' in unix format.", stacktraceFile.contains("\r"));

    // when
    SortedSet<SubText> findStackTraces = finder.findStackTraces(stacktraceFile);

    // then
    assertEquals(2, findStackTraces.size());
    Iterator<SubText> iterator = findStackTraces.iterator();
    SubText subtext = iterator.next();
    assertEquals(43, subtext.getStart());
    assertEquals(3102, subtext.getEnd());
    subtext = iterator.next();
    assertEquals(3137, subtext.getStart());
    assertEquals(3388, subtext.getEnd());
  }
}
