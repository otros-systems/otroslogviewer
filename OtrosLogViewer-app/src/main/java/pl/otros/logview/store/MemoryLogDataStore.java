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

package pl.otros.logview.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.LogData;

import java.util.*;

public class MemoryLogDataStore extends AbstractMemoryLogStore implements LogDataStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLogDataStore.class.getName());

  protected ArrayList<LogData> list;
  protected SortedSet<Date> s = new TreeSet<>();

  public MemoryLogDataStore() {
    list = new ArrayList<>();
  }

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public void add(LogData... logDatas) {
    Arrays.sort(logDatas, logDataTimeComparator);
    for (LogData logData : logDatas) {
      logData.setId(getNextLogId());
      if (list.size() == 0 || logDataTimeComparator.compare(logData, list.get(list.size() - 1)) >= 0) {
        list.add(logData);
      } else {
        int index = getIndexToInsert(logData.getDate(), 0, list.size() - 1, list.size() / 2);
        list.add(index, logData);
      }
      if (list.size() > limit) {
        remove(0);
      }

    }
  }

  protected int getIndexToInsert(Date date, final int downLimit, final int upLimit, int startAt) {
    int startPoint = startAt;
    int effectiveDownLimit = downLimit;
    Date dateInList = list.get(startPoint).getDate();
    int compareTo = date.compareTo(dateInList);
    int effectiveUpLimit = upLimit;
    if (effectiveUpLimit - effectiveDownLimit < 3) {
      for (int i = effectiveUpLimit; i >= effectiveDownLimit; i--) {
        dateInList = list.get(i).getDate();
        compareTo = date.compareTo(dateInList);
        if (compareTo == 0) {
          return i;
        } else if (compareTo > 0) {
          return i + 1;
        }
      }
      return effectiveDownLimit;
    }

    if (compareTo < 0) {
      effectiveUpLimit = startPoint;
    } else if (compareTo > 0) {
      effectiveDownLimit = startPoint;
    } else {
      return startPoint;
    }
    startPoint = (effectiveDownLimit + effectiveUpLimit) / 2;
    return getIndexToInsert(date, effectiveDownLimit, effectiveUpLimit, startPoint);
  }

  @Override
  public void remove(int... rows) {
    LOGGER.debug(String.format("Removing %d rows, first sorting by id", rows.length));
    Arrays.sort(rows);
    LOGGER.trace("Rows sorted, removing from end");
    for (int i = rows.length - 1; i >= 0; i--) {
      LogData removed = list.remove(rows[i]);
      notable.removeNote(removed.getId(), false);
    }
    LOGGER.trace(String.format("%d rows where removed ", rows.length));

  }

  @Override
  public LogData getLogData(int id) {
    return list.get(id);
  }

  @Override
  public LogData[] getLogData() {
    LogData[] datas = new LogData[list.size()];
    datas = list.toArray(datas);
    return datas;
  }

  @Override
  public int clear() {
    int size = list.size();
    if (size > 0) {
      list.clear();
      clearNotes();
    }
    return size;
  }

  @Override
  public Iterator<LogData> iterator() {
    return list.iterator();
  }

  @Override
  public Integer getLogDataIdInRow(int row) {
    return list.get(row).getId();
  }

}
