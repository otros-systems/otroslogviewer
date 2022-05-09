/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.api.persistance;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static org.testng.AssertJUnit.assertEquals;

//TODO check assert order
public class PersistanceVer2Test {

  private ArrayList<LogData> list;

  private final String result = """
    ID|TIMESTAMP|MESSAGE|CLASS|METHOD|LEVEL|LOGGER|THREAD|MDC|NDC|FILE|LINE|LOG_SOURCE|NOTE|MARKED|MARKED_COLOR|
    0|0|message\\nno \\P0\\P|class|method|INFO|LN|Thread|a=l0\\n|ndc|File|123|file:/a.txt|Note|true|Red|
    1|1|message\\nno \\P1\\P|class|method|INFO|LN|Thread|a=l1\\n|ndc|File|123|file:/a.txt|Note|false||
    2|2|message\\nno \\P2\\P|class|method|INFO|LN|Thread|a=l2\\n|ndc|File|123|file:/a.txt|Note|true|Black|
    3|3|message\\nno \\P3\\P|class|method|INFO|LN|Thread|a=l3\\n|ndc|File|123|file:/a.txt|Note|false||
    4|4|message\\nno \\P4\\P|class|method|INFO|LN|Thread|a=l4\\n|ndc|File|123|file:/a.txt|Note|true|Green|
    """;

  private static final String EMPTY_RESULT = "ID|TIMESTAMP|MESSAGE|CLASS|METHOD|LEVEL|LOGGER|THREAD|MDC|NDC|FILE|LINE|LOG_SOURCE|NOTE|MARKED|MARKED_COLOR|\n";
  private LogDataListPersistanceVer2 p;

  @BeforeMethod
  public void prepare() {
    p = new LogDataListPersistanceVer2();
    list = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      LogData ld = new LogData();
      ld.setLevel(Level.INFO);
      ld.setId(i);
      ld.setDate(new Date(i));
      ld.setClazz("class");
      ld.setMethod("method");
      ld.setThread("Thread");
      ld.setLoggerName("Logger");
      ld.setMessage("message\nno |" + i + "|");
      ld.setLoggerName("LN");
      ld.setLogSource("file:/a.txt");
      ld.setNDC("ndc");
      ld.setFile("File");
      if (i % 2 == 0) {
        ld.setMarkerColors(MarkerColors.values()[i % MarkerColors.values().length]);
        ld.setMarked(true);
      }
      ld.setNote(new Note("Note"));
      ld.setLine("123");
      HashMap<String, String> properties = new HashMap<>();
      properties.put("a", "l" + i);
      ld.setProperties(properties);

      list.add(ld);
    }
  }

  @Test
  public void testSaveLogsListVer2() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    p.saveLogsList(bout, list);
    String s = bout.toString(StandardCharsets.UTF_8);
    Assert.assertEquals(result, s);

  }

  @Test
  public void testSaveEmpty() throws IOException {
    list.clear();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    p.saveLogsList(bout, list);
    String s = bout.toString(StandardCharsets.UTF_8);
    Assert.assertEquals(EMPTY_RESULT, s);
  }

  @Test
  public void testLoadLogsListVer2() throws IOException {

    ByteArrayInputStream bin = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
    List<LogData> loadLogsListVer2 = p.loadLogsList(bin);
    Assert.assertEquals(5, loadLogsListVer2.size());
    for (int i = 0; i < list.size(); i++) {
      LogData expected = list.get(i);
      LogData actual = loadLogsListVer2.get(i);
      Assert.assertEquals(expected, actual);
    }
  }

  @Test
  public void testParseLogData() {
    HashMap<String, Integer> fieldMapping = new HashMap<>();
    int i = 0;
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_ID, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_CLASS, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_LEVEL, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_LOGGER, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_MESSAGE, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_METHOD, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_THREAD, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_TIMESTAMP, i);

    String[] line = { "3", "a.b.Class", "SEVERE", "logger", "message!\\n\\P\\S", "myMethod", "thread-1", "1000" };
    LogData ld = p.parseLogData(line, fieldMapping);
    assertEquals(3, ld.getId());
    assertEquals(Level.SEVERE, ld.getLevel());
    assertEquals("a.b.Class", ld.getClazz());
    assertEquals("myMethod", ld.getMethod());
    assertEquals("thread-1", ld.getThread());
    assertEquals(1000, ld.getDate().getTime());
    assertEquals("message!\n|\\", ld.getMessage());

  }

  @Test
  public void testEsape() {
    String s = "ala ma kota \nkot ma \\al|e";
    String expected = "ala ma kota \\nkot ma \\Sal\\Pe";
    String result = p.escapedString(s);

    assertEquals(expected, result);
  }

  @Test
  public void testUnEsape() {
    String s = "ala ma kota \\nkot ma \\Sal\\Pe";
    String expected = "ala ma kota \nkot ma \\al|e";
    String result = p.unescapedString(s);
    assertEquals(expected, result);
  }

}
