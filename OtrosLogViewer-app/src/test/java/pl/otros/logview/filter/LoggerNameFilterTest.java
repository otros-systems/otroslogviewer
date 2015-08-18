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
package pl.otros.logview.filter;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;

public class LoggerNameFilterTest {

  private final LoggerNameFilter filter = new LoggerNameFilter();

  @BeforeMethod
public void before() {
    filter.setEnable(true);

  }

  @Test
  public void testAcceptSensetiveAccept() {
    // given
    LogData logData = new LogDataBuilder().withLoggerName("loggerA").build();
    filter.setIgnoreCase(false);
    filter.setFilteringText("loggerA");
    filter.performPreFiltering();

    // when
    boolean accept = filter.accept(logData, 0);

    // then
    assertTrue(accept);
  }

  @Test
  public void testAcceptInSensetiveAccept() {
    // given
    LogData logData = new LogDataBuilder().withLoggerName("loggera").build();
    filter.setIgnoreCase(true);
    filter.setFilteringText("loggerA");
    filter.performPreFiltering();

    // when
    boolean accept = filter.accept(logData, 0);

    // then
    assertTrue(accept);
  }

  @Test
  public void testAcceptSensetiveReject1() {
    // given
    LogData logData = new LogDataBuilder().withLoggerName("loggerA").build();
    filter.setIgnoreCase(false);
    filter.setFilteringText("loggerB");
    filter.performPreFiltering();

    // when
    boolean accept = filter.accept(logData, 0);

    // then
    assertFalse(accept);
  }

  @Test
  public void testAcceptSensetiveReject2() {
    // given
    LogData logData = new LogDataBuilder().withLoggerName("loggerA").build();
    filter.setIgnoreCase(false);
    filter.setFilteringText("loggera");
    filter.performPreFiltering();

    // when
    boolean accept = filter.accept(logData, 0);

    // then
    assertFalse(accept);
  }

  @Test
  public void testAcceptInSensetiveReject() {
    // given
    LogData logData = new LogDataBuilder().withLoggerName("loggera").build();
    filter.setIgnoreCase(true);
    filter.setFilteringText("loggerb");
    filter.performPreFiltering();

    // when
    boolean accept = filter.accept(logData, 0);

    // then
    assertFalse(accept);
  }

  @Test
  public void testAcceptEmptyCondition() {
    // given
    LogData logData = new LogDataBuilder().withLoggerName("loggera").build();
    filter.setIgnoreCase(true);
    filter.setFilteringText("");
    filter.performPreFiltering();

    // when
    boolean accept = filter.accept(logData, 0);

    // then
    assertTrue(accept);
  }
}
