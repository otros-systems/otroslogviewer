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
package pl.otros.logview.parser.log4j;

import org.testng.annotations.Test;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;
import java.util.Properties;
import java.util.logging.Level;
import pl.otros.logview.importer.InitializationException;

public class Log4jUtilTest {

  @Test
  public void testParseLevel() {
    assertEquals(Level.FINEST, Log4jUtil.parseLevel("TRACE"));
    assertEquals(Level.FINE, Log4jUtil.parseLevel("DEBUG"));
    assertEquals(Level.INFO, Log4jUtil.parseLevel("INFO"));
    assertEquals(Level.WARNING, Log4jUtil.parseLevel("WARN"));
    assertEquals(Level.SEVERE, Log4jUtil.parseLevel("ERROR"));
    assertEquals(Level.SEVERE, Log4jUtil.parseLevel("FATAL"));
  }

  @Test
  public void testParsePattern() {
    Properties p = new Properties();
    p.setProperty(Log4jUtil.CONVERSION_PATTERN, "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%t] %c{2}: %m%n");

    try {
      Log4jUtil.parsePattern(p);
    } catch (InitializationException ex) {
      fail(ex.getMessage());
    }

    assertEquals("yyyy-MM-dd HH:mm:ss.SSS", p.getProperty("dateFormat"));
    assertEquals("TIMESTAMP LEVEL [THREAD] CLASS: MESSAGE", p.getProperty("pattern"));
  }
}
