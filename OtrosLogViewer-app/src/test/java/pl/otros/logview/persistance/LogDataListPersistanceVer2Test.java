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
package pl.otros.logview.persistance;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class LogDataListPersistanceVer2Test {

  LogDataListPersistanceVer2 ver2 = new LogDataListPersistanceVer2();

  @Test
  public void testLogMessagesWithCarriageReturn() throws IOException {
    List<LogData> list = new ArrayList<LogData>();
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1\r\nLine2").withThread("T1").build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message2\r\nLine2").withThread("T1").build();
    list.add(ld1);
    list.add(ld2);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    assertEquals(2, loadLogsList.size());
    assertEquals(ld1.getMessage(), loadLogsList.get(0).getMessage());
    assertEquals(ld2.getMessage(), loadLogsList.get(1).getMessage());
    assertEquals(ld1, loadLogsList.get(0));
    assertEquals(ld2, loadLogsList.get(1));
  }

  @Test
  public void testLogMessagesWithEmptyLastParams() throws IOException {
    List<LogData> list = new ArrayList<LogData>();
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1").build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message2").build();
    list.add(ld1);
    list.add(ld2);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    assertEquals(2, loadLogsList.size());
    assertEquals(ld1.getMessage(), loadLogsList.get(0).getMessage());
    assertEquals(ld2.getMessage(), loadLogsList.get(1).getMessage());
    assertEquals(ld1, loadLogsList.get(0));
    assertEquals(ld2, loadLogsList.get(1));
  }

  @Test
  public void testLogDataWithProperties() throws IOException {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("key1", "value1");
    properties.put("key2", "value2");
    LogData logData = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1").withProperties(properties).build();

    List<LogData> list = new ArrayList<LogData>();
    list.add(logData);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    assertEquals(1, loadLogsList.size());
    Map<String, String> propertiesRead = loadLogsList.get(0).getProperties();
    assertNotNull(propertiesRead);
    assertEquals(2, propertiesRead.size());
    assertEquals("value1", propertiesRead.get("key1"));
    assertEquals("value2", propertiesRead.get("key2"));
  }

  @Test
  public void testLogDataWithoutProperties() throws IOException {
    LogData logData = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1").build();

    List<LogData> list = new ArrayList<LogData>();
    list.add(logData);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    assertEquals(1, loadLogsList.size());
    Map<String, String> propertiesRead = loadLogsList.get(0).getProperties();
    assertNull(propertiesRead);
  }

  @Test
  public void testLogDataWithoutPropertiesColumn() throws IOException {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("persistance/logs_without_properties_column.txt");
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    assertEquals(40, loadLogsList.size());
    Map<String, String> propertiesRead = loadLogsList.get(0).getProperties();
    assertNull(propertiesRead);
  }

  @Test
  public void testEscpageStringPipes() {
    String escpageString = ver2.escpageString("String|with|pipe");
    assertEquals("String\\Pwith\\Ppipe", escpageString);
  }

  @Test
  public void testEscpageStringBackslash() {
    String escpageString = ver2.escpageString("String\\with\\backslash");
    assertEquals("String\\Swith\\Sbackslash", escpageString);
  }

  @Test
  public void testEscpageStringNewLine() {
    String escpageString = ver2.escpageString("String\nwith\nnewline");
    assertEquals("String\\nwith\\nnewline", escpageString);
  }
}
