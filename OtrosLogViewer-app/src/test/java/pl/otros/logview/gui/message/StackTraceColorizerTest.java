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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class StackTraceColorizerTest {

  @Test
  public void testColorizingNeeded() throws IOException {
    String string = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("stacktrace/stacktrace.txt"));
    StackTraceColorizer colorizer = new StackTraceColorizer();
    boolean colorizingNeeded = colorizer.colorizingNeeded(string);
    Assert.assertTrue(colorizingNeeded);
  }
}
