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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import pl.otros.logview.LogData;

public class RegexMatcherTest {

  private LogData ld1;
  private LogData ld2;

  @BeforeMethod
public void setup() {
    ld1 = new LogData();
    ld1.setMessage("Ala ma kota, a kot ma ale 12");

    ld2 = new LogData();
    ld2.setMessage("Ala ma kota a kot ma ale");

  }

  @Test
  public void testMatches() {
    RegexMatcher matcher = new RegexMatcher(".*ma[\\s\\w]+,.*\\d");
    assertTrue(matcher.matches(ld1));
    assertFalse(matcher.matches(ld2));
  }

}
