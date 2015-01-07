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
package pl.otros.logview.gui;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.accept.AcceptCondition;
import pl.otros.logview.gui.note.NotableTableModel;
import pl.otros.logview.gui.note.NoteEvent;
import pl.otros.logview.gui.note.NoteEvent.EventType;
import pl.otros.logview.gui.note.NoteObserver;
import pl.otros.logview.gui.table.TableColumns;
import pl.otros.logview.store.CachedLogStore;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;
import pl.otros.logview.store.SynchronizedLogDataStore;
import pl.otros.logview.store.file.FileLogDataStore;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogDataTableModel extends AbstractTableModel implements LogDataCollector, MarkableTableModel, NotableTableModel {

  private static final String EMPTY_STRING = "";

  private static final Logger LOGGER = Logger.getLogger(LogDataTableModel.class.getName());

  private static final LogData EMPTY_LOG_DATA = new LogData();

  private static final Note EMPTY_NOTE = new Note("");
  private Set<NoteObserver> noteObservers;
  private LogDataStore logDataStore;
  private Map<String, ClassWrapper> classWrapperCache;

  private int maximumMessageLength = 2000;

  public LogDataTableModel() {
    classWrapperCache = new HashMap<String, ClassWrapper>(100);

    String cached = System.getProperty("cacheEvents");
    if (StringUtils.equalsIgnoreCase(cached, "true")) {
      try {
        LOGGER.info("Trying to use cache log store");
        logDataStore = new CachedLogStore(new FileLogDataStore());
      } catch (IOException e) {
        LOGGER.severe("Can't create cached log store: " + e.getMessage());
      }
    }

    if (logDataStore == null) {
      logDataStore = new MemoryLogDataStore();
    }

    logDataStore = new SynchronizedLogDataStore(logDataStore);

    EMPTY_LOG_DATA.setId(Integer.MAX_VALUE);
    EMPTY_LOG_DATA.setDate(new Date(0));
    EMPTY_LOG_DATA.setMessage("----- Out of bounds ------");
    EMPTY_LOG_DATA.setLevel(Level.SEVERE);
    noteObservers = new HashSet<NoteObserver>();
  }

  public String getColumnName(int column) {
    return TableColumns.getColumnById(column).getName();
  }

  public int getColumnCount() {
    return TableColumns.values().length;
  }

  public int getRowCount() {
    return logDataStore.getCount();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    LogData ld = getLogData(rowIndex);
    if (ld == null) {
      System.err.println("LogDataTableModel.getValueAt() null form row " + rowIndex);
      dumpInfo();
    }
    Object result = null;
    TableColumns selecteColumn = TableColumns.getColumnById(columnIndex);
    switch (selecteColumn) {
      case ID:
        result = ld.getId();
        break;
      case TIME:
        result = ld.getDate();
        break;
      case DELTA:
        result = new TimeDelta(ld.getDate());
        break;
      case LEVEL:
        result = ld.getLevel();
        break;
      case MESSAGE:
        result = ld.getMessage();
        break;
      case CLASS:
        String clazz = ld.getClazz();
        if (!classWrapperCache.containsKey(clazz)) {
          classWrapperCache.put(clazz, new ClassWrapper(clazz));
        }
        result = classWrapperCache.get(clazz);
        break;
      case METHOD:
        result = StringUtils.left(ld.getMethod(), maximumMessageLength);
        break;
      case THREAD:
        result = ld.getThread();
        break;
      case MARK:
        result = ld.getMarkerColors();
        break;
      case NOTE:
        result = ld.getNote();
        if (result == null) {
          result = EMPTY_NOTE;
        }
        break;
      case FILE:
        result = ld.getFile();
        break;
      case LINE:
        result = ld.getLine();
        break;
      case NDC:
        result = ld.getNDC();
        break;
      case PROPERTIES:
        result = getProperties(ld);
        break;
      case LOGGER_NAME:
        result = ld.getLoggerName();
        break;
      case LOG_SOURCE:
        result = ld.getLogSource();
        break;
      default:
        break;
    }
    return result;
  }

  private String getProperties(LogData ld) {
    if (ld.getProperties() != null && ld.getProperties().size() > 0) {
      return Joiner.on(", ").withKeyValueSeparator("=").join(ld.getProperties());
    }
    return EMPTY_STRING;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == TableColumns.MARK.getColumn() || columnIndex == TableColumns.NOTE.getColumn()) {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see pl.otros.logview.gui.LogDataCollector#add(pl.otros.logview.LogData[])
   */
  public void add(LogData... logDatas) {
    if (logDatas.length == 0) {
      return;
    }
    for (LogData logData : logDatas) {
      addLogDataToTable(logData);
    }
    fireTableRowsInserted(getRowCount() - logDatas.length, getRowCount() - 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see pl.otros.logview.gui.LogDataCollector#add(pl.otros.logview.LogData)
   */
  public void add(LogData logData) {
    addLogDataToTable(logData);
    int rowToNotify = getRowCount() - 1;
    fireTableRowsInserted(rowToNotify, rowToNotify);
  }

  private void addLogDataToTable(LogData logData) {
    logDataStore.add(logData);
  }

  public int removeRows(AcceptCondition acceptCondition) {
    LOGGER.fine(String.format("Removing rows using accept condition: %s", acceptCondition.getName()));
    ArrayList<Integer> toDelete = new ArrayList<Integer>();
    for (int row = 0; row < logDataStore.getCount(); row++) {
      LogData logData = logDataStore.getLogData(row);
      if (acceptCondition.accept(logData)) {
        toDelete.add(row);
      }
    }
    LOGGER.fine(String.format("To remove using accept condition %s is %d rows", acceptCondition.getName(), toDelete.size()));
    if (toDelete.size() > 0) {
      int[] rows = new int[toDelete.size()];
      for (int i = 0; i < rows.length; i++) {
        rows[i] = toDelete.get(i);
      }
      removeRows(rows);
    }
    LOGGER.fine(String.format("Using accept condition %s was removed %d rows", acceptCondition.getName(), toDelete.size()));
    return toDelete.size();
  }

  protected void removeRows(int... rows) {
    logDataStore.remove(rows);

    if (rows.length > 0) {
      int firstRow = rows[0];
      int lastRow = rows[rows.length - 1];
      LOGGER.finest(String.format("Firing event fireTableRowsDeleted %d->%d", firstRow, lastRow));
      fireTableRowsDeleted(firstRow, lastRow);
      LOGGER.finest(String.format("Firing event fireTableRowsDeleted has ended"));
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    // return super.getColumnClass(columnIndex); LogData ld = list.get(rowIndex);
    Class<?> result;
    TableColumns selectedColumn = TableColumns.getColumnById(columnIndex);
    switch (selectedColumn) {
      case ID:
        result = Integer.class;
        break;
      case TIME:
        result = Date.class;
        break;
      case DELTA:
        result = TimeDelta.class;
        break;
      case LEVEL:
        result = Level.class;
        break;
      case MESSAGE:
        result = String.class;
        break;
      case CLASS:
        result = ClassWrapper.class;
        break;
      case MARK:
        result = MarkerColors.class;
        break;
      case NOTE:
        result = Note.class;
        break;
      default:
        result = String.class;
        break;
    }
    return result;
  }

  public LogData getLogData(int row) {
    if (row < 0 || row >= logDataStore.getCount()) {
      return EMPTY_LOG_DATA;
    }
    return logDataStore.getLogData(row);
  }

  public LogData[] getLogData() {
    return logDataStore.getLogData();
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    if (columnIndex == TableColumns.MARK.getColumn()) {
      MarkerColors marked = (MarkerColors) value;
      if (marked != null) {
        logDataStore.markRows(marked, rowIndex);
      } else {
        logDataStore.unmarkRows(rowIndex);
      }
    } else if (columnIndex == TableColumns.NOTE.getColumn()) {
      Note n = (Note) value;
      // logDataStore.addNoteToRow(rowIndex, n);
      addNoteToRow(rowIndex, n);
      // notable.addNoteToRow(getLogData(rowIndex).getId(), n);
    }
    fireTableRowsUpdated(rowIndex, rowIndex);
  }

  @Override
  public boolean isMarked(int row) {
    return logDataStore.isMarked(row);
  }

  @Override
  public MarkerColors getMarkerColors(int row) {
    return logDataStore.getMarkerColors(row);
  }

  @Override
  public void markRows(MarkerColors markerColor, int... rows) {
    logDataStore.markRows(markerColor, rows);
    if (rows.length > 0) {
      Arrays.sort(rows);
      fireTableRowsUpdated(rows[0], rows[rows.length - 1]);
    }
  }

  @Override
  public void unmarkRows(int... rows) {
    logDataStore.unmarkRows(rows);

    if (rows.length > 0) {
      Arrays.sort(rows);
      fireTableRowsUpdated(rows[0], rows[rows.length - 1]);
    }
  }

  @Override
  public void addNoteToRow(int row, Note note) {
    logDataStore.addNoteToRow(row, note);
    NoteEvent event = new NoteEvent(EventType.REMOVE, this, note, row);
    notifyAllNoteObservers(event);
    fireTableRowsUpdated(row, row);
  }

  @Override
  public void clearNotes() {
    logDataStore.clearNotes();
    fireTableDataChanged();
  }

  @Override
  public TreeMap<Integer, Note> getAllNotes() {
    return logDataStore.getAllNotes();
  }

  @Override
  public Note getNote(int row) {
    Note n = logDataStore.getNote(row);
    return n != null ? n : EMPTY_NOTE;
  }

  @Override
  public Note removeNote(int row) {
    Note n = logDataStore.removeNote(row);
    fireTableRowsUpdated(row, row);
    return n;
  }

  @Override
  public void addNoteObserver(NoteObserver observer) {
    noteObservers.add(observer);
  }

  @Override
  public void removeAllNoteObserver() {
    noteObservers.clear();
  }

  @Override
  public void removeNoteObserver(NoteObserver observer) {
    noteObservers.remove(observer);
  }

  @Override
  public void notifyAllNoteObservers(NoteEvent noteEvent) {
    for (NoteObserver observer : noteObservers) {
      observer.update(noteEvent);
    }
  }

  public Memento saveToMemento() {
    Memento m = new Memento();
    m.dataLimit = logDataStore.getLimit();
    m.list = new ArrayList<LogData>();
    m.list.addAll(Arrays.asList(logDataStore.getLogData()));
    m.marks = new TreeMap<Integer, Boolean>();
    for (int row = 0; row < logDataStore.getCount(); row++) {
      LogData ld = logDataStore.getLogData(row);
      if (ld.isMarked()) {
        m.marks.put(row, Boolean.TRUE);
        m.marksColor.put(row, ld.getMarkerColors());
      }
      Note note = ld.getNote();
      if (note != null && StringUtils.isNotBlank(note.getNote())) {
        m.notes.put(row, note);
      }
    }
    return m;
  }

  public void restoreFromMemento(Memento memento) {
    logDataStore.clear();
    logDataStore.setLimit(memento.dataLimit);

    logDataStore.add(memento.list.toArray(new LogData[memento.list.size()]));

    TreeMap<Integer, MarkerColors> marksColor = memento.getMarksColor();
    for (Integer row : marksColor.keySet()) {
      logDataStore.markRows(marksColor.get(row), row);
    }
    logDataStore.clearNotes();
    for (Integer row : memento.notes.keySet()) {
      logDataStore.addNoteToRow(row, memento.notes.get(row));
    }
    fireTableDataChanged();
  }

  private void dumpInfo() {
    System.out.println("LogDataTableModel.dumpInfo() list.size" + logDataStore.getCount());
    System.out.println("LogDataTableModel.dumpInfo() dumping content");

    // for (Integer idx : list.keySet()) {
    // System.out.println("LogDataTableModel.dumpInfo() idx: " + idx + ": " + list.get(idx));
    // }
    // System.out.println("LogDataTableModel.enclosing_method() marks size" + marks.size());
  }

  public int getDataLimit() {
    return logDataStore.getLimit();
  }

  public void setDataLimit(int dataLimit) {
    logDataStore.setLimit(dataLimit);

  }

  @Override
  public Note removeNote(int row, boolean notify) {
    Note n = logDataStore.removeNote(row);
    if (notify) {
      NoteEvent event = new NoteEvent(EventType.REMOVE, this, n, row);
      notifyAllNoteObservers(event);
    }
    return n;
  }

  @Override
  public int clear() {
    int clear = logDataStore.clear();
    fireTableDataChanged();
    return clear;
  }

  public LogDataStore getLogDataStore() {
    return logDataStore;
  }

  public static class Memento implements Serializable {

    private static final long serialVersionUID = 1L;
    private int dataLimit = Integer.MAX_VALUE;
    private int shift = 0;
    private int addIndex = 0;
    private Set<Integer> visibleColumns = new HashSet<Integer>();
    private ArrayList<LogData> list = new ArrayList<LogData>();
    private TreeMap<Integer, Boolean> marks = new TreeMap<Integer, Boolean>();
    private TreeMap<Integer, MarkerColors> marksColor = new TreeMap<Integer, MarkerColors>();
    private TreeMap<Integer, Note> notes = new TreeMap<Integer, Note>();
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getDataLimit() {
      return dataLimit;
    }

    public void setDataLimit(int dataLimit) {
      this.dataLimit = dataLimit;
    }

    public int getShift() {
      return shift;
    }

    public void setShift(int shift) {
      this.shift = shift;
    }

    public int getAddIndex() {
      return addIndex;
    }

    public void setAddIndex(int addIndex) {
      this.addIndex = addIndex;
    }

    public ArrayList<LogData> getList() {
      return list;
    }

    public void setList(ArrayList<LogData> list) {
      this.list = list;
    }

    public TreeMap<Integer, Boolean> getMarks() {
      return marks;
    }

    public void setMarks(TreeMap<Integer, Boolean> marks) {
      this.marks = marks;
    }

    public TreeMap<Integer, Note> getNotes() {
      return notes;
    }

    public void setNotes(TreeMap<Integer, Note> notes) {
      this.notes = notes;
    }

    public TreeMap<Integer, MarkerColors> getMarksColor() {
      return marksColor;
    }

    public void setMarksColor(TreeMap<Integer, MarkerColors> marksColor) {
      this.marksColor = marksColor;
    }

    public Set<Integer> getVisibleColumns() {
      return visibleColumns;
    }

    public void setVisibleColumns(Set<Integer> visibleColumns) {
      this.visibleColumns = visibleColumns;
    }

  }

}
