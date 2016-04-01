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
package pl.otros.logview.api.store;

import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.api.model.LogDataStore;

import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class MemoryLogDataStoreTest extends LogDataStoreTestBase {

  public LogDataStore getLogDataStore() {
    return new MemoryLogDataStore();
  }

  @Test
  public void testGetIndexToInsert() {
    // given
    MemoryLogDataStore memoryLogDataStore = (MemoryLogDataStore) logDataStore;
    for (int i = 0; i < 100; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }

    // when
    int indexToInsert = memoryLogDataStore.getIndexToInsert(new Date(40), 0, logDataStore.getCount(), logDataStore.getCount() / 2);

    // then
    assertEquals(40, indexToInsert);
  }

  @Test
  public void testGetIndexToInsertTheSameDate() {
    // given
    MemoryLogDataStore memoryLogDataStore = (MemoryLogDataStore) logDataStore;
    for (int i = 0; i < 100; i++) {
      logDataStore.add(new LogDataBuilder().withId(i).withDate(new Date(i)).build());
    }
    for (int i = 0; i < 10; i++) {
      logDataStore.add(new LogDataBuilder().withId(100 + i).withDate(new Date(100)).build());
    }
    for (int i = 0; i < 10; i++) {
      logDataStore.add(new LogDataBuilder().withId(110 + i).withDate(new Date(110 + i)).build());
    }

    // when
    int indexToInsert = memoryLogDataStore.getIndexToInsert(new Date(100), 0, logDataStore.getCount(), logDataStore.getCount() / 2);

    // then
    assertTrue(100 <= indexToInsert && indexToInsert < 110);
  }
}
