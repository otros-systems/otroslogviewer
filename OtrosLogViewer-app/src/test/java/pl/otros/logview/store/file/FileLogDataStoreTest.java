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
package pl.otros.logview.store.file;

import org.testng.annotations.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.Note;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.LogDataStoreTestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;

public class FileLogDataStoreTest extends LogDataStoreTestBase {

  private LogData[] logDatas;
  private FileLogDataStore dataStore;

  @Override
  public LogDataStore getLogDataStore() throws IOException {
    dataStore = new FileLogDataStore();
    logDatas = new LogData[10];
    for (int i = 0; i < logDatas.length; i++) {
      logDatas[i] = new LogData();
      logDatas[i].setId(i);
      logDatas[i].setMessage("A: " + i);
      logDatas[i].setDate(new Date(i));
      logDatas[i].setNote(new Note("N" + i));
    }

    return dataStore;
  }

  @Test
  public void testIterator2() throws IOException {
    // given
    dataStore.add(logDatas);


    // when
    ArrayList<LogData> acutal = new ArrayList<>();
    for (LogData ld : dataStore) {
      acutal.add(ld);
    }

    // then
    assertArrayEquals(logDatas, acutal.toArray(new LogData[acutal.size()]));

  }

  @Test
  public void testGetCount2() throws IOException {
    // given
    dataStore.add(logDatas);

    // when
    int count = dataStore.getCount();

    // then
    assertEquals(logDatas.length, count);

  }

  @Test
  public void testAdd() throws IOException {
    dataStore.add(logDatas);

    // when
    int count = dataStore.getCount();

    // then
    assertEquals(logDatas.length, count);
  }

  @Test
  public void testRemove2() throws IOException {
    dataStore.add(logDatas);

    // when
    dataStore.remove(1, 4, 6);

    // then
    assertEquals(logDatas.length - 3, dataStore.getCount());
    assertEquals(0, dataStore.getLogData(0).getId());
    assertEquals(2, dataStore.getLogData(1).getId());
    assertEquals(3, dataStore.getLogData(2).getId());
    assertEquals(5, dataStore.getLogData(3).getId());
    assertEquals(7, dataStore.getLogData(4).getId());
    assertEquals(8, dataStore.getLogData(5).getId());
    assertEquals(9, dataStore.getLogData(6).getId());
  }

  @Test
  public void testGetLogDataInt2() throws IOException {
    dataStore.add(logDatas);

    // when

    // then
    assertEquals(logDatas.length, dataStore.getCount());
    assertEquals(0, dataStore.getLogData(0).getId());
    assertEquals(1, dataStore.getLogData(1).getId());
    assertEquals(2, dataStore.getLogData(2).getId());
    assertEquals(3, dataStore.getLogData(3).getId());
    assertEquals(4, dataStore.getLogData(4).getId());
    assertEquals(5, dataStore.getLogData(5).getId());
    assertEquals(6, dataStore.getLogData(6).getId());
  }

  @Test
  public void testGetLogData2() throws IOException {

    dataStore.add(logDatas);

    // when
    LogData[] logData = dataStore.getLogData();
    // then
    assertEquals(logDatas.length, logData.length);
    for (int i = 0; i < logData.length; i++) {
      assertEquals(logDatas[i], logData[i]);

    }
  }

  @Test
  public void testSetLimit() {
    dataStore.setLimit(100);
    assertEquals(100, dataStore.getLimit());
    assertEquals(0, dataStore.getCount());

    dataStore.add(logDatas);

    assertEquals(logDatas.length, dataStore.getCount());
    dataStore.setLimit(5);
    assertEquals(5, dataStore.getCount());

    LogData ld = new LogData();
    ld.setId(100);
    ld.setMessage("");
    ld.setDate(new Date());

    dataStore.add(ld);
    assertEquals(5, dataStore.getCount());

  }

  @Test
  public void testClear() {
    // given
    dataStore.add(logDatas);

    assertEquals(logDatas.length, dataStore.getCount());

    // when
    dataStore.clear();

    // then
    assertEquals(0, dataStore.getCount());
    assertEquals(0, dataStore.getLogData().length);
  }

}
