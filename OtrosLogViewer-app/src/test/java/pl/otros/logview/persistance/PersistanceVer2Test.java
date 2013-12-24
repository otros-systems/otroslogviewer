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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class PersistanceVer2Test {

  private LogData[] datas;
  private ArrayList<LogData> list;
  private String result = "ID|TIMESTAMP|MESSAGE|CLASS|METHOD|LEVEL|LOGGER|THREAD|MDC|NDC|FILE|LINE|LOG_SOURCE|NOTE|MARKED|MARKED_COLOR|\n"
      + "0|0|message\\nno \\P0\\P|class|method|INFO|LN|Thread|a=l0\\n|ndc|File|123|file:/a.txt|Note|true|Red|\n" //
      + "1|1|message\\nno \\P1\\P|class|method|INFO|LN|Thread|a=l1\\n|ndc|File|123|file:/a.txt|Note|false||\n" //
      + "2|2|message\\nno \\P2\\P|class|method|INFO|LN|Thread|a=l2\\n|ndc|File|123|file:/a.txt|Note|true|Black|\n" //
      + "3|3|message\\nno \\P3\\P|class|method|INFO|LN|Thread|a=l3\\n|ndc|File|123|file:/a.txt|Note|false||\n" //
      + "4|4|message\\nno \\P4\\P|class|method|INFO|LN|Thread|a=l4\\n|ndc|File|123|file:/a.txt|Note|true|Green|\n";

  private static final String EMPTY_RESULT = "ID|TIMESTAMP|MESSAGE|CLASS|METHOD|LEVEL|LOGGER|THREAD|MDC|NDC|FILE|LINE|LOG_SOURCE|NOTE|MARKED|MARKED_COLOR|\n";
  private LogDataListPersistanceVer2 p;

  @Before
  public void prepare() {
    p = new LogDataListPersistanceVer2();
    datas = new LogData[5];
    list = new ArrayList<LogData>();
    for (int i = 0; i < datas.length; i++) {
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
      HashMap<String, String> properties = new HashMap<String, String>();
      properties.put("a", "l" + i);
      ld.setProperties(properties);

      datas[i] = ld;
      list.add(ld);
    }
  }

  @Test
  public void testSaveLogsListVer2() throws IOException {
	System.setProperty("line.separator", "\n");
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    p.saveLogsList(bout, list);
    String s = new String(bout.toByteArray(), "UTF-8");
    Assert.assertEquals(result,s);

  }

  @Test
  public void testSaveEmpty() throws IOException {
    list.clear();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    p.saveLogsList(bout, list);
    String s = new String(bout.toByteArray(), "UTF-8");
    Assert.assertEquals(EMPTY_RESULT, s);
  }

  @Test
  public void testLoadLogsListVer2() throws IOException {

    ByteArrayInputStream bin = new ByteArrayInputStream(result.getBytes("UTF-8"));
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
    HashMap<String, Integer> fieldMapping = new HashMap<String, Integer>();
    int i = 0;
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_ID, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_CLASS, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_LEVEL, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_LOGGER, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_MESSAGE, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_METHOD, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_THREAD, i++);
    fieldMapping.put(LogDataListPersistanceVer2.HEADER_TIMESTAMP, i++);

    String[] line = new String[] { "3", "a.b.Class", "SEVERE", "logger", "message!\\n\\P\\S", "myMethod", "thread-1", "1000" };
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
    String result = p.escpageString(s);

    assertEquals(expected, result);
  }

  @Test
  public void testUnEsape() {
    String s = "ala ma kota \\nkot ma \\Sal\\Pe";
    String expected = "ala ma kota \nkot ma \\al|e";
    String result = p.unescapgeString(s);
    assertEquals(expected, result);
  }

}
