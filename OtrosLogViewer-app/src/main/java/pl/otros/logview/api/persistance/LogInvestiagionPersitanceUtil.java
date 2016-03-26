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
package pl.otros.logview.api.persistance;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import pl.otros.logview.api.gui.LogDataTableModel.Memento;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LogInvestiagionPersitanceUtil {

  private static final String ENTRY_INFO = "info";
  private static final String ENTRY_LOGS = "logs";
  private static final String ENTRY_MARKS = "marks";
  private static final String ENTRY_MARKS_COLORS = "marksColors";
  private static final String ENTRY_NOTES = "notes";
  private static final String INFO_VERSION = "Version";
  private static final String INFO_NAME = "name";
  private static final String INFO_ADD_INDEX = "index";
  private static final String INFO_SHIFT = "shift";
  private static final String INFO_VISIBLE_COLUMNS = "visibleColumns";

  private static JFileChooser chooser;

  private static final LogDataListPersistanceVer1 persistanceVer1 = new LogDataListPersistanceVer1();
  private static final LogDataListPersistanceVer2 persistanceVer2 = new LogDataListPersistanceVer2();

  public static JFileChooser getFileChooser() {
    if (chooser == null) {
      chooser = new JFileChooser();
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.addChoosableFileFilter(new FileFilter() {

        @Override
        public String getDescription() {
          return "Otros LogView investigation *.zip.olv";
        }

        @Override
        public boolean accept(File f) {
          return f.isDirectory() || f.getName().endsWith("zip.olv");
        }
      });
    }
    return chooser;
  }

  public static void saveMemento(Memento memento, OutputStream out) throws IOException {
    ZipOutputStream zout = new ZipOutputStream(out);
    zout.putNextEntry(new ZipEntry(ENTRY_INFO));
    Properties info = new Properties();
    info.put(INFO_VERSION, Integer.toString(2));
    info.put(INFO_NAME, memento.getName());
    info.put(INFO_ADD_INDEX, Integer.toString(memento.getAddIndex()));
    info.put(INFO_SHIFT, Integer.toString(memento.getShift()));
    info.put(INFO_VISIBLE_COLUMNS, Joiner.on(",").join(memento.getVisibleColumns()));
    info.store(zout, "Info file");

    zout.putNextEntry(new ZipEntry(ENTRY_NOTES));
    TreeMap<Integer, Note> notes = memento.getNotes();
    Properties notesP = new Properties();
    for (Integer index : notes.keySet()) {
      notesP.put(index.toString(), notes.get(index).getNote());
    }
    notesP.store(zout, null);

    zout.putNextEntry(new ZipEntry(ENTRY_MARKS));
    TreeMap<Integer, Boolean> marks = memento.getMarks();
    Properties marksP = new Properties();
    for (Integer index : marks.keySet()) {
      marksP.put(index.toString(), marks.get(index).toString());
    }
    marksP.store(zout, null);

    zout.putNextEntry(new ZipEntry(ENTRY_MARKS_COLORS));
    TreeMap<Integer, MarkerColors> marksColor = memento.getMarksColor();
    Properties marksColorP = new Properties();
    for (Integer index : marksColor.keySet()) {
      marksColorP.put(index.toString(), marksColor.get(index).toString());
    }
    marksColorP.store(zout, null);

    zout.putNextEntry(new ZipEntry(ENTRY_LOGS));
    List<LogData> list = memento.getList();
    persistanceVer2.saveLogsList(zout, list);
    zout.close();

  }

  public static Memento loadMemento(InputStream in) throws Exception {
    Memento m = new Memento();
    ZipInputStream zin = new ZipInputStream(in);
    ZipEntry ze = null;
    LogDataListPersistance listPersistance = null;
    int version = 0;
    while ((ze = zin.getNextEntry()) != null) {
      if (ze.getName().equalsIgnoreCase(ENTRY_INFO)) {
        Properties p = new Properties();
        p.load(zin);
        m.setAddIndex(Integer.parseInt(p.getProperty(INFO_ADD_INDEX)));
        m.setShift(Integer.parseInt(p.getProperty(INFO_SHIFT)));
        m.setName(p.getProperty(INFO_NAME));
        version = Integer.parseInt(p.getProperty(INFO_VERSION));

        if (p.getProperty(INFO_VISIBLE_COLUMNS, "").length() > 0) {
          Iterable<String> split = Splitter.on(",").split(p.getProperty(INFO_VISIBLE_COLUMNS));
          for (String string : split) {
            m.getVisibleColumns().add(Integer.parseInt(string));
          }
        }
      } else if (ze.getName().equalsIgnoreCase(ENTRY_MARKS)) {
        Properties p = new Properties();
        p.load(zin);
        for (Object key : p.keySet()) {
          m.getMarks().put(Integer.parseInt(key.toString()), Boolean.parseBoolean(p.getProperty(key.toString())));
        }
      } else if (ze.getName().equalsIgnoreCase(ENTRY_NOTES)) {
        Properties p = new Properties();
        p.load(zin);
        for (Object key : p.keySet()) {
          m.getNotes().put(Integer.parseInt(key.toString()), new Note(p.getProperty(key.toString())));
        }
      } else if (ze.getName().equalsIgnoreCase(ENTRY_LOGS)) {
        if (version == 1) {
          listPersistance = persistanceVer1;
        } else if (version == 2) {
          listPersistance = persistanceVer2;
        } else if (version > 2) {
          throw new Exception("Logs saved with newer version, check for update.");
        }
        List<LogData> loadLogsList = listPersistance.loadLogsList(zin);
        ArrayList<LogData> list = new ArrayList<>(loadLogsList.size());
        list.addAll(loadLogsList);
        m.setList(list);
      } else if (ze.getName().equalsIgnoreCase(ENTRY_MARKS_COLORS)) {
        Properties p = new Properties();
        p.load(zin);
        for (Object key : p.keySet()) {
          m.getMarksColor().put(Integer.parseInt(key.toString()), MarkerColors.valueOf(p.getProperty(key.toString())));
        }
      }
    }
    return m;
  }

}
