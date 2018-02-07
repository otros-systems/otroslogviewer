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
package pl.otros.logview.api.persistance;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

public class LogDataListPersistanceVer2Test {

  private LogDataListPersistanceVer2 ver2 = new LogDataListPersistanceVer2();

  @Test
  public void testLogMessagesWithCarriageReturn() throws IOException {
    List<LogData> list = new ArrayList<>();
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1\r\nLine2").withThread("T1").build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message2\r\nLine2").withThread("T1").build();
    list.add(ld1);
    list.add(ld2);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    AssertJUnit.assertEquals(2, loadLogsList.size());
    AssertJUnit.assertEquals(ld1.getMessage(), loadLogsList.get(0).getMessage());
    AssertJUnit.assertEquals(ld2.getMessage(), loadLogsList.get(1).getMessage());
    AssertJUnit.assertEquals(ld1, loadLogsList.get(0));
    AssertJUnit.assertEquals(ld2, loadLogsList.get(1));
  }

  @Test
  public void testLogMessagesWithEmptyLastParams() throws IOException {
    List<LogData> list = new ArrayList<>();
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1").build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message2").build();
    list.add(ld1);
    list.add(ld2);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    AssertJUnit.assertEquals(2, loadLogsList.size());
    AssertJUnit.assertEquals(ld1.getMessage(), loadLogsList.get(0).getMessage());
    AssertJUnit.assertEquals(ld2.getMessage(), loadLogsList.get(1).getMessage());
    AssertJUnit.assertEquals(ld1, loadLogsList.get(0));
    AssertJUnit.assertEquals(ld2, loadLogsList.get(1));
  }

  @Test
  public void testLogDataWithProperties() throws IOException {
    Map<String, String> properties = new HashMap<>();
    properties.put("key1", "value1");
    properties.put("key2", "value2");
    LogData logData = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1").withProperties(properties).build();

    List<LogData> list = new ArrayList<>();
    list.add(logData);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    AssertJUnit.assertEquals(1, loadLogsList.size());
    Map<String, String> propertiesRead = loadLogsList.get(0).getProperties();
    AssertJUnit.assertNotNull(propertiesRead);
    AssertJUnit.assertEquals(2, propertiesRead.size());
    AssertJUnit.assertEquals("value1", propertiesRead.get("key1"));
    AssertJUnit.assertEquals("value2", propertiesRead.get("key2"));
  }

  @Test
  public void testLogDataWithoutProperties() throws IOException {
    LogData logData = new LogDataBuilder().withId(1).withDate(new Date()).withLevel(Level.INFO).withMessage("My Message1").build();

    List<LogData> list = new ArrayList<>();
    list.add(logData);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ver2.saveLogsList(out, list);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    AssertJUnit.assertEquals(1, loadLogsList.size());
    Map<String, String> propertiesRead = loadLogsList.get(0).getProperties();
    AssertJUnit.assertEquals(0, propertiesRead.size());
  }

  @Test
  public void testLogDataWithoutPropertiesColumn() throws IOException {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("persistance/logs_without_properties_column.txt");
    List<LogData> loadLogsList = ver2.loadLogsList(in);

    AssertJUnit.assertEquals(40, loadLogsList.size());
    Map<String, String> propertiesRead = loadLogsList.get(0).getProperties();
    AssertJUnit.assertEquals(0, propertiesRead.size());
  }

  @Test
  public void testEscpageStringPipes() {
    String escpageString = ver2.escapedString("String|with|pipe");
    AssertJUnit.assertEquals("String\\Pwith\\Ppipe", escpageString);
  }

  @Test
  public void testEscpageStringBackslash() {
    String escpageString = ver2.escapedString("String\\with\\backslash");
    AssertJUnit.assertEquals("String\\Swith\\Sbackslash", escpageString);
  }

  @Test
  public void testEscpageStringNewLine() {
    String escpageString = ver2.escapedString("String\nwith\nnewline");
    AssertJUnit.assertEquals("String\\nwith\\nnewline", escpageString);
  }
}
