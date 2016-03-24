/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.api.store;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.LogDataBuilder;
import pl.otros.logview.api.LogDataStore;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public abstract class LogDataStoreTestBase {

  protected LogDataStore logDataStore;

  public abstract LogDataStore getLogDataStore() throws Exception;

  @BeforeMethod
  public void prepare() throws Exception {
    logDataStore = getLogDataStore();
  }

  @Test
  public void testGetCount() {
    // given
    // when
    for (int i = 0; i < 100; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    // then
    assertEquals(100, logDataStore.getCount());
  }

  @Test
  public void testGetCountWithLimit() {
    // given
    // when
    logDataStore.setLimit(20);
    for (int i = 0; i < 100; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    // then
    assertEquals(20, logDataStore.getCount());
  }

  @Test
  public void testGetCountEmpty() {
    // given
    // when
    // then
    assertEquals(0, logDataStore.getCount());
  }

  @Test
  public void testRemove() {
    // given
    for (int i = 0; i < 100; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    // when
    logDataStore.remove(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 20, 21, 22, 23);

    assertEquals(10, logDataStore.getLogData(0).getId());
    assertEquals(24, logDataStore.getLogData(10).getId());
    assertEquals(86, logDataStore.getCount());

  }

  @Test
  public void testGetLogDataInt() {
    // given
    for (int i = 0; i < 10; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    // when

    // then
    assertEquals(1, logDataStore.getLogData(1).getId());
  }

  @Test
  public void testGetLogData() {
    // given
    for (int i = 0; i < 10; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    // when
    LogData[] logData = logDataStore.getLogData();
    // then
    assertEquals(10, logData.length);
    for (int i = 0; i < 10; i++) {
      assertEquals(i, logData[i].getId());
    }
  }

  @Test
  public void testClear() {
    // given
    for (int i = 0; i < 100; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }

    // when
    logDataStore.clear();
    // then
    assertEquals(0, logDataStore.getCount());
    assertEquals(0, logDataStore.getAllNotes().size());
  }

  @Test
  public void testIterator() {
    // given
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date(1)).build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date(2)).build();
    LogData ld3 = new LogDataBuilder().withId(3).withDate(new Date(3)).build();
    LogData ld4 = new LogDataBuilder().withId(4).withDate(new Date(4)).build();
    LogData[] lds = {ld1, ld2, ld3, ld4};

    logDataStore.add(lds);
    HashMap<Integer, LogData> logDataList = new HashMap<>();
    for (LogData logData : lds) {
      logDataList.put(logData.getId(), logData);
    }

    // when
    Iterator<LogData> iterator = logDataStore.iterator();
    while (iterator.hasNext()) {
      LogData next = iterator.next();
      logDataList.remove(next.getId());
    }

    // then
    assertEquals(0, logDataList.size());

  }

  @Test
  public void testGetLogDataIdInRow() {
    // given
    for (int i = 0; i < 10; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    // when
    // then
    for (int i = 0; i < 10; i++) {
      assertEquals(i, logDataStore.getLogDataIdInRow(i).intValue());
    }
  }

  @Test
  public void testAddInDateOrder() {
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date(1)).build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date(2)).build();
    LogData ld3 = new LogDataBuilder().withId(3).withDate(new Date(3)).build();
    LogData ld4 = new LogDataBuilder().withId(4).withDate(new Date(4)).build();
    LogData ld5 = new LogDataBuilder().withId(5).withDate(new Date(5)).build();
    LogData ld6 = new LogDataBuilder().withId(6).withDate(new Date(6)).build();
    LogData ld7 = new LogDataBuilder().withId(7).withDate(new Date(7)).build();
    LogData ld8 = new LogDataBuilder().withId(8).withDate(new Date(8)).build();
    LogData ld9 = new LogDataBuilder().withId(9).withDate(new Date(9)).build();

    logDataStore.add(ld4, ld5, ld3);
    logDataStore.add(ld2, ld1);
    logDataStore.add(ld7, ld8);
    logDataStore.add(ld6);
    logDataStore.add(ld9);

    assertEquals(ld1.getId(), logDataStore.getLogData(0).getId());
    assertEquals(ld2.getId(), logDataStore.getLogData(1).getId());
    assertEquals(ld3.getId(), logDataStore.getLogData(2).getId());
    assertEquals(ld4.getId(), logDataStore.getLogData(3).getId());
    assertEquals(ld5.getId(), logDataStore.getLogData(4).getId());
    assertEquals(ld6.getId(), logDataStore.getLogData(5).getId());
    assertEquals(ld7.getId(), logDataStore.getLogData(6).getId());
    assertEquals(ld8.getId(), logDataStore.getLogData(7).getId());
    assertEquals(ld9.getId(), logDataStore.getLogData(8).getId());

  }

  @Test
  public void testAddInDateOrderWithTheSameDate() {
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date(1)).withMessage("m1").build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date(2)).withMessage("m2").build();
    LogData ld3 = new LogDataBuilder().withId(3).withDate(new Date(3)).withMessage("m3").build();
    LogData ld4 = new LogDataBuilder().withId(4).withDate(new Date(4)).withMessage("m4").build();
    LogData ld5 = new LogDataBuilder().withId(5).withDate(new Date(4)).withMessage("m5").build();
    LogData ld6 = new LogDataBuilder().withId(6).withDate(new Date(4)).withMessage("m6").build();
    LogData ld7 = new LogDataBuilder().withId(7).withDate(new Date(4)).withMessage("m7").build();
    LogData ld8 = new LogDataBuilder().withId(8).withDate(new Date(4)).withMessage("m8").build();
    LogData ld9 = new LogDataBuilder().withId(9).withDate(new Date(9)).withMessage("m9").build();

    logDataStore.add(ld3);
    logDataStore.add(ld2, ld1);
    logDataStore.add(ld7, ld8, ld6, ld5, ld4);
    logDataStore.add(ld9);

    assertEquals("m1", logDataStore.getLogData(0).getMessage());
    assertEquals("m2", logDataStore.getLogData(1).getMessage());
    assertEquals("m3", logDataStore.getLogData(2).getMessage());
    assertEquals("m4", logDataStore.getLogData(3).getMessage());
    assertEquals("m5", logDataStore.getLogData(4).getMessage());
    assertEquals("m6", logDataStore.getLogData(5).getMessage());
    assertEquals("m7", logDataStore.getLogData(6).getMessage());
    assertEquals("m8", logDataStore.getLogData(7).getMessage());
    assertEquals("m9", logDataStore.getLogData(8).getMessage());

  }

  @Test
  public void testAddInDateOrderTheSameDate() {
    LogData ld1 = new LogDataBuilder().withId(1).withDate(new Date(1)).build();
    LogData ld2 = new LogDataBuilder().withId(2).withDate(new Date(2)).build();
    LogData ld3 = new LogDataBuilder().withId(3).withDate(new Date(3)).build();
    LogData ld4 = new LogDataBuilder().withId(4).withDate(new Date(4)).build();
    LogData ld5 = new LogDataBuilder().withId(5).withDate(new Date(7)).build();
    LogData ld6 = new LogDataBuilder().withId(6).withDate(new Date(7)).build();
    LogData ld7 = new LogDataBuilder().withId(7).withDate(new Date(7)).build();
    LogData ld8 = new LogDataBuilder().withId(8).withDate(new Date(8)).build();
    LogData ld9 = new LogDataBuilder().withId(9).withDate(new Date(9)).build();

    logDataStore.add(ld4, ld5, ld3);
    logDataStore.add(ld2, ld1);
    logDataStore.add(ld7, ld8);
    logDataStore.add(ld6);
    logDataStore.add(ld9);

    long lastTime = 0;
    for (LogData ld : logDataStore) {
      assertTrue(ld.getDate().getTime() >= lastTime);
      lastTime = ld.getDate().getTime();
    }
  }

  @Test
  public void testAddWithIDGeneration() {
    for (int i = 0; i < 10; i++) {
      logDataStore.add(new LogDataBuilder().withId(0).withDate(new Date(i)).build());
    }

    long lastTime = 0;
    int lastId = -1;
    for (LogData ld : logDataStore) {
      assertTrue(ld.getDate().getTime() >= lastTime);
      assertEquals(ld.getId(), lastId + 1);
      lastTime = ld.getDate().getTime();
      lastId = ld.getId();
    }

  }

}
