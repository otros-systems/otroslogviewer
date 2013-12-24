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

import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.accept.AcceptCondition;
import pl.otros.logview.accept.LowLevelAcceptCondition;
import pl.otros.logview.gui.table.TableColumns;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class LogDataTableModelTest {

  public LogDataTableModel dataTableModel;
  public LogData[] logDatas;
  private static final String NOTE_4 = "Note row 4";
  private static final String NOTE_9 = "Note row 9";

  @Before
  public void prepareModel() {
    dataTableModel = new LogDataTableModel();
    logDatas = new LogData[10];
    for (int i = 0; i < logDatas.length; i++) {
      logDatas[i] = generateLogData(i);
    }
    dataTableModel.add(logDatas);

    // mark row
    dataTableModel.markRows(MarkerColors.Brown, 4);
    dataTableModel.markRows(MarkerColors.Orange, 6);

    // Add notes;
    dataTableModel.addNoteToRow(4, new Note(NOTE_4));
    dataTableModel.addNoteToRow(9, new Note(NOTE_9));
    dataTableModel.addNoteToRow(2, new Note(""));

  }

  public LogData generateLogData(int id) {
    LogDataBuilder b = new LogDataBuilder();
    b.withId(id).withClass("a.b.Clazz").withMethod("myMethod").withDate(new Date(id)).withLevel(id == 2 || id == 8 ? Level.FINE : Level.INFO).withThread("T1");
    b.withMessage("Message " + id);
    return b.build();
  }

  @Test
  public void testInitalized() {
    assertEquals(logDatas.length, dataTableModel.getRowCount());
  }

  @Test
  public void testGetRowCount() {
    assertEquals(logDatas.length, dataTableModel.getRowCount());
  }

  @Test
  public void testGetValueAt() {
    for (int i = 0; i < logDatas.length; i++) {
      Level level = (Level) dataTableModel.getValueAt(i, TableColumns.LEVEL.getColumn());
      assertEquals(logDatas[i].getLevel(), level);

      String thread = (String) dataTableModel.getValueAt(i, TableColumns.THREAD.getColumn());
      assertEquals(logDatas[i].getThread(), thread);
    }
  }

  @Test
  public void testAddLogDataArray() {
    LogData ld1 = generateLogData(dataTableModel.getRowCount());
    LogData ld2 = generateLogData(dataTableModel.getRowCount() + 1);
    LogData ld3 = generateLogData(dataTableModel.getRowCount() + 2);
    LogData[] lds = new LogData[] { ld1, ld2, ld3 };
    int count = dataTableModel.getRowCount();
    dataTableModel.add(lds);
    int count2 = dataTableModel.getRowCount();
    assertEquals(count + lds.length, count2);
  }

  @Test
  public void testAddLogData() {
    for (int i = 0; i < 9; i++) {
      LogData ld1 = generateLogData(dataTableModel.getRowCount());
      int before = dataTableModel.getRowCount();
      dataTableModel.add(ld1);
      int after = dataTableModel.getRowCount();
      assertEquals(before + 1, after);
    }

  }

  @Test
  public void testRemoveRowsAcceptCondition() {
    AcceptCondition acceptCondition = new LowLevelAcceptCondition();
    dataTableModel.removeRows(acceptCondition);
    assertEquals(8, dataTableModel.getRowCount());
    for (int i = 0; i < dataTableModel.getRowCount(); i++) {
      LogData logData = dataTableModel.getLogData(i);
      int row = i;
      if (i >= 7) {
        row = i + 2;
      } else if (i >= 2) {
        row = i + 1;
      }
      LogData expected = logDatas[row];
      assertEquals(expected, logData);
    }

    // fail("Not yet implemented");
  }

  @Test
  public void testGetColumnClassInt() {
    assertEquals(Level.class, dataTableModel.getColumnClass(TableColumns.LEVEL.getColumn()));
    assertEquals(String.class, dataTableModel.getColumnClass(TableColumns.MESSAGE.getColumn()));
    assertEquals(Note.class, dataTableModel.getColumnClass(TableColumns.NOTE.getColumn()));
    assertEquals(Date.class, dataTableModel.getColumnClass(TableColumns.TIME.getColumn()));

  }

  @Test
  public void testGetLogDataInt() {
    for (int i = 0; i < logDatas.length; i++) {
      LogData logData = dataTableModel.getLogData(i);
      assertEquals(logDatas[i], logData);
    }
  }

  @Test
  public void testGetLogData() {
    LogData[] logData2 = dataTableModel.getLogData();
    for (int i = 0; i < logDatas.length; i++) {
      assertEquals(logDatas[i], logData2[i]);
    }
  }

  @Test
  public void testGetMarkerColors() {
    assertEquals(MarkerColors.Brown, dataTableModel.getMarkerColors(4));
    assertEquals(MarkerColors.Orange, dataTableModel.getMarkerColors(6));
  }

  @Test
  public void testMarkRowsIntArrayMarkerColors() {
    dataTableModel.markRows(MarkerColors.Black, 1, 3);
    assertEquals(MarkerColors.Black, dataTableModel.getMarkerColors(1));
    assertEquals(MarkerColors.Black, dataTableModel.getMarkerColors(3));

    dataTableModel.markRows(MarkerColors.Yellow, 2, 3, 4);
    assertEquals(MarkerColors.Yellow, dataTableModel.getMarkerColors(2));
    assertEquals(MarkerColors.Yellow, dataTableModel.getMarkerColors(3));
    assertEquals(MarkerColors.Yellow, dataTableModel.getMarkerColors(4));

  }

  @Test
  public void testMarkRowsIntMarkerColors() {
    assertFalse(dataTableModel.isMarked(2));
    assertFalse(dataTableModel.isMarked(3));
    assertTrue(dataTableModel.isMarked(4));
    dataTableModel.markRows(MarkerColors.Yellow, 2, 3, 4);
    assertEquals(MarkerColors.Yellow, dataTableModel.getMarkerColors(2));
    assertEquals(MarkerColors.Yellow, dataTableModel.getMarkerColors(3));
    assertEquals(MarkerColors.Yellow, dataTableModel.getMarkerColors(4));
    dataTableModel.markRows(MarkerColors.Aqua, 2);
    assertEquals(MarkerColors.Aqua, dataTableModel.getMarkerColors(2));
  }

  @Test
  public void testUnmarkRowsIntArray() {
    assertTrue(dataTableModel.isMarked(4));
    assertTrue(dataTableModel.isMarked(6));
    dataTableModel.unmarkRows(new int[] { 4, 6 });
    assertFalse(dataTableModel.isMarked(4));
    assertFalse(dataTableModel.isMarked(6));
  }

  @Test
  public void testUnmarkRowsInt() {
    assertTrue(dataTableModel.isMarked(4));
    dataTableModel.unmarkRows(new int[] { 4 });
    boolean marked = dataTableModel.isMarked(4);
    assertFalse(marked);
  }

  @Test
  public void testAddNoteToRow() {
    dataTableModel.addNoteToRow(1, new Note("a"));
    String note = dataTableModel.getNote(1).getNote();
    assertEquals("a", note);
  }

  @Test
  public void testClearNotes() {
    dataTableModel.clearNotes();
    assertEquals(0, dataTableModel.getAllNotes().size());
  }

  @Test
  public void testGetAllNotes() {
    TreeMap<Integer, Note> allNotes = dataTableModel.getAllNotes();
    assertEquals(2, allNotes.size());
    Set<Integer> keySet = allNotes.keySet();
    Integer[] keys = new Integer[2];
    keys = keySet.toArray(keys);
    Arrays.sort(keys);
    assertEquals(4, keys[0].intValue());
    assertEquals(9, keys[1].intValue());
  }

  @Test
  public void testGetNote() {
    assertEquals(NOTE_4, dataTableModel.getNote(4).getNote());
    assertEquals(NOTE_9, dataTableModel.getNote(9).getNote());
    assertEquals("", dataTableModel.getNote(2).getNote());
    assertEquals("", dataTableModel.getNote(3).getNote());

  }

  @Test
  public void testRemoveNote() {
    assertEquals(NOTE_4, dataTableModel.getNote(4).getNote());
    dataTableModel.removeNote(4);
    assertEquals("", dataTableModel.getNote(4).getNote());
  }

  @Test
  public void testRemoveFromCenter() {
    dataTableModel.removeRows(4);
    for (int i = 0; i < dataTableModel.getRowCount(); i++) {
      LogData logData = dataTableModel.getLogData(i);
      LogData expected = logDatas[i >= 4 ? i + 1 : i];
      assertEquals(expected, logData);
    }
  }

  @Test
  public void testAddingWithLimit() {
    int id = dataTableModel.getRowCount();
    dataTableModel.setDataLimit(10);
    dataTableModel.add(generateLogData(id));

    assertEquals(10, dataTableModel.getRowCount());
    assertTrue(dataTableModel.isMarked(3));
    assertTrue(dataTableModel.isMarked(5));
    assertEquals(NOTE_4, dataTableModel.getNote(3).getNote());
  }

}
