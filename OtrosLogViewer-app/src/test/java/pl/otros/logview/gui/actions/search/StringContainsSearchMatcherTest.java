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
package pl.otros.logview.gui.actions.search;

import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;

import static org.junit.Assert.assertEquals;

public class StringContainsSearchMatcherTest {

  private LogData logData;
  private LogData logDataUpperCase;
  private StringContainsSearchMatcher matcher = new StringContainsSearchMatcher("ala");

  @Before
  public void setup() {
    logData = new LogData();
    logData.setMessage("ala ma kota");

    logDataUpperCase = new LogData();
    logDataUpperCase.setMessage("Ala ma kota");
  }

  @Test
  public void testMatches() {
    assertEquals(true, matcher.matches(logData));
  }

  @Test
  public void testMatchesIgnoreCase() {
    assertEquals(true, matcher.matches(logDataUpperCase));
  }
}
