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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.LogData;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.gui.note.NotableTableModel;
import pl.otros.logview.gui.note.NotableTableModelImpl;
import pl.otros.logview.store.AbstractMemoryLogStore;
import pl.otros.logview.store.LogDataStore;

import java.io.*;
import java.util.*;

public class FileLogDataStore extends AbstractMemoryLogStore implements LogDataStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileLogDataStore.class.getName());
  private static final int INITIAL_MAPPING_SIZE = 10000;
  private HashMap<Integer, Long> storeIdFilePositionMapping;
  private RandomAccessFile randomAccessFile;
  private ArrayList<IdAndDate> logDatasId;
  protected TreeMap<Integer, Boolean> marks;
  protected TreeMap<Integer, MarkerColors> marksColor;
  protected NotableTableModel notable;

  public FileLogDataStore() throws IOException {
    init();
    marks = new TreeMap<>();
    marksColor = new TreeMap<>();
    notable = new NotableTableModelImpl();
  }

  protected void init() throws IOException {
    File createTempFile = File.createTempFile("OLV_", "_");
    createTempFile.deleteOnExit();
    randomAccessFile = new RandomAccessFile(createTempFile, "rw");
    storeIdFilePositionMapping = new HashMap<>(INITIAL_MAPPING_SIZE);
    logDatasId = new ArrayList<>(INITIAL_MAPPING_SIZE);
  }

  @Override
  public Iterator<LogData> iterator() {
    return new LogDataIterator(logDatasId.iterator());
  }

  @Override
  public int getCount() {
    return logDatasId.size();
  }

  @Override
  public void add(LogData... logDatas) {
    ObjectOutputStream oout = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    Arrays.sort(logDatas, logDataTimeComparator);
    try {
      HashMap<Integer, Long> newLogDataPosition = new HashMap<>(logDatas.length);
      long length = randomAccessFile.length();
      LOGGER.trace(String.format("Setting position in file %s to %d", randomAccessFile.getFD().toString(), length));
      randomAccessFile.seek(length);
      for (LogData logData1 : logDatas) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(byteArrayOutputStream);
        logData1.setId(getNextLogId());
        int logDataId = logData1.getId();
        long positionInFile = randomAccessFile.length();

        oout.writeObject(logData1);
        oout.flush();

        randomAccessFile.writeInt(byteArrayOutputStream.size());
        randomAccessFile.write(byteArrayOutputStream.toByteArray());
        newLogDataPosition.put(Integer.valueOf(logDataId), Long.valueOf(positionInFile));

        marks.put(Integer.valueOf(logDataId), logData1.isMarked());
        marksColor.put(Integer.valueOf(logDataId), logData1.getMarkerColors());

        if (logData1.getNote() != null) {
          notable.addNoteToRow(logDataId, logData1.getNote());
        }
      }
      storeIdFilePositionMapping.putAll(newLogDataPosition);

      // TODO sorting by date!
      for (LogData logData : logDatas) {
        // logDatasId.add(new IdAndDate(Integer.valueOf(logDatas[i].getId()), logDatas[i].getDate()));
        if (logDatasId.size() == 0 || logData.getDate().compareTo(logDatasId.get(logDatasId.size() - 1).date) >= 0) {
          logDatasId.add(new IdAndDate(Integer.valueOf(logData.getId()), logData.getDate()));
        } else {
          int index = getIndexToInsert(logData.getDate(), 0, logDatasId.size() - 1, logDatasId.size() / 2);
          logDatasId.add(index, new IdAndDate(Integer.valueOf(logData.getId()), logData.getDate()));
        }

      }

      ensureLimit();
    } catch (IOException e) {
      LOGGER.error(String.format("Error adding %d events: %s", logDatas.length, e.getMessage()));
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(oout);
    }

  }

  @Override
  public void remove(int... rows) {
    LOGGER.debug(String.format("Removing %d rows, first sorting by id", rows.length));
    Arrays.sort(rows);
    LOGGER.trace("Rows sorted, removing from end");
    for (int i = rows.length - 1; i >= 0; i--) {
      Integer removeId = logDatasId.remove(rows[i]).id;
      notable.removeNote(removeId, false);
      marks.remove(removeId);
      storeIdFilePositionMapping.remove(removeId);
    }
    LOGGER.trace(String.format("%d rows where removed ", rows.length));

  }

  @Override
  public LogData getLogData(int row) {
    Integer logDataId = logDatasId.get(row).id;
    try {
      return getLogDataById(logDataId);
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error(String.format("Can't load data for row %d: %s", row, e.getMessage()));
    }
    return null;
  }

  @Override
  public Integer getLogDataIdInRow(int row) {
    return logDatasId.get(row).id;
  }

  private LogData getLogDataById(Integer logDataId) throws IOException, ClassNotFoundException {
    Long eventPositionInStream = storeIdFilePositionMapping.get(logDataId);
    long position = eventPositionInStream.longValue();
    randomAccessFile.seek(position);
    int size = randomAccessFile.readInt();
    byte[] buff = new byte[size];
    randomAccessFile.readFully(buff);
    ByteArrayInputStream bin = new ByteArrayInputStream(buff);
    ObjectInputStream objectInputStreams = new ObjectInputStream(bin);
    LogData readObject = (LogData) objectInputStreams.readObject();
    if (marksColor.containsKey(logDataId) && marks.containsKey(logDataId) && marks.get(logDataId).booleanValue()) {
      readObject.setMarked(true);
      readObject.setMarkerColors(marksColor.get(logDataId));
    }
    if (notable.getNote(logDataId.intValue()) != null) {
      readObject.setNote(notable.getNote(logDataId.intValue()));
    }
    return readObject;
  }

  @Override
  public LogData[] getLogData() {
    ArrayList<LogData> list = new ArrayList<>(getCount());
    for (LogData ld : this) {
      list.add(ld);
    }
    return list.toArray(new LogData[list.size()]);
  }

  @Override
  public int clear() {
    int size = logDatasId.size();
    RandomAccessFile old = randomAccessFile;
    storeIdFilePositionMapping.clear();
    logDatasId.clear();
    try {
      init();
    } catch (IOException e) {
      LOGGER.info("Can't initialize new log file after clear: " + e.getMessage());
      return 0;
    }
    try {
      old.close();
    } catch (IOException e) {
      LOGGER.warn("Can't close temporary file: " + e.getMessage());
      e.printStackTrace();
    }
    return size;
  }

  private class LogDataIterator implements Iterator<LogData> {

    private final Iterator<IdAndDate> idsIterator;

    public LogDataIterator(Iterator<IdAndDate> idsIterator) {
      this.idsIterator = idsIterator;
    }

    @Override
    public boolean hasNext() {
      return idsIterator.hasNext();
    }

    @Override
    public LogData next() {
      Integer logId = idsIterator.next().id;
      try {
        return getLogDataById(logId);
      } catch (Exception e) {
        // TODO
        e.printStackTrace();
        throw new RuntimeException("Can't get next data: " + e.getMessage(), e);
      }
    }

    @Override
    public void remove() {
      idsIterator.remove();
    }

  }

  public void addNoteToRow(int row, Note note) {
    notable.addNoteToRow(row, note);
  }

  public Note getNote(int row) {
    return notable.getNote(row);
  }

  public Note removeNote(int row) {
    return notable.removeNote(row);
  }

  public void removeNote(int row, boolean notify) {
    notable.removeNote(row, notify);
  }

  public void clearNotes() {
    notable.clearNotes();
  }

  public TreeMap<Integer, Note> getAllNotes() {
    return notable.getAllNotes();
  }

  protected int getIndexToInsert(Date date, int downLimit, int upLimit, int startPoint) {
    Date dateInList = logDatasId.get(startPoint).date;
    int compareTo = date.compareTo(dateInList);
    int effectiveDownLimit = downLimit;
    int effectiveUpLimit = upLimit;
    if (effectiveUpLimit - effectiveDownLimit < 3) {
      for (int i = effectiveUpLimit; i >= effectiveDownLimit; i--) {
        dateInList = logDatasId.get(i).date;
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

  public static class IdAndDate implements Comparable<IdAndDate> {

    public IdAndDate(Integer id, Date date) {
      super();
      this.id = id;
      this.date = date;
    }

    protected Integer id;
    protected Date date;

    @Override
    public int compareTo(IdAndDate o) {

      int compareTo = date.compareTo(o.date);
      if (compareTo == 0) {
        compareTo = id.compareTo(o.id);
      }
      return compareTo;
    }

  }

  @Override
  public boolean isMarked(int row) {
    Integer logDataIdInRow = getLogDataIdInRow(row);
    return marks.containsKey(logDataIdInRow) && marks.get(logDataIdInRow).booleanValue();
  }

  @Override
  public MarkerColors getMarkerColors(int row) {
    Integer logDataIdInRow = getLogDataIdInRow(row);
    return marksColor.get(logDataIdInRow);
  }

  @Override
  public void markRows(MarkerColors markerColor, int... rows) {
    for (int row : rows) {
      Integer logDataIdInRow = getLogDataIdInRow(row);
      marks.put(logDataIdInRow, Boolean.TRUE);
      marksColor.put(logDataIdInRow, markerColor);
    }
  }

  @Override
  public void unmarkRows(int... rows) {
    for (int row : rows) {
      Integer logDataIdInRow = getLogDataIdInRow(row);
      marks.put(logDataIdInRow, Boolean.FALSE);
      marksColor.put(logDataIdInRow, null);
    }
  }
}
