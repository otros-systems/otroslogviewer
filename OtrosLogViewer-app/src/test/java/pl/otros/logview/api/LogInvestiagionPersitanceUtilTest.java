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
package pl.otros.logview.api;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import pl.otros.logview.TestUtils;
import pl.otros.logview.api.LogDataTableModel.Memento;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class LogInvestiagionPersitanceUtilTest {

  public Memento getMemento() {
    Memento m = new Memento();
    m.setAddIndex(0);
    m.setShift(0);
    m.setName("superlog.txt");
    for (int i = 0; i < 30; i++) {
      m.getList().add(TestUtils.generateLogData());
      m.getNotes().put(i, new Note("Note " + i));
      m.getMarks().put(i, i % 2 == 0 ? true : false);
      m.getMarksColor().put(Integer.valueOf(i), MarkerColors.values()[i % MarkerColors.values().length]);
    }
    HashSet<Integer> visibleColumns = new HashSet<>();
    visibleColumns.add(Integer.valueOf(3));
    visibleColumns.add(Integer.valueOf(4));
    m.setVisibleColumns(visibleColumns);
    return m;
  }

  @Test
  public void testLoadMemento() throws Exception {
    Memento source = getMemento();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    LogInvestiagionPersitanceUtil.saveMemento(source, bout);
    ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
    Memento restored = LogInvestiagionPersitanceUtil.loadMemento(bin);

    AssertJUnit.assertEquals(source.getName(), restored.getName());
    AssertJUnit.assertEquals(source.getAddIndex(), restored.getAddIndex());

    AssertJUnit.assertEquals(source.getShift(), restored.getShift());
    TreeMap<Integer, Boolean> sourceMarks = source.getMarks();
    for (Integer indx : sourceMarks.keySet()) {
      AssertJUnit.assertEquals(sourceMarks.get(indx), restored.getMarks().get(indx));
    }
    TreeMap<Integer, Note> sourceNotes = source.getNotes();
    for (Integer indx : sourceNotes.keySet()) {
      AssertJUnit.assertEquals(sourceNotes.get(indx).getNote(), restored.getNotes().get(indx).getNote());
    }

    Set<Integer> visibleColumns = source.getVisibleColumns();
    AssertJUnit.assertEquals(source.getVisibleColumns().size(), restored.getVisibleColumns().size());
    for (Integer integer : visibleColumns) {
      AssertJUnit.assertTrue(restored.getVisibleColumns().contains(integer));
    }
  }

}
