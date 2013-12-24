/*******************************************************************************
 * Copyright 2011 krzyh
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
package pl.otros.logview.batch;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.LogData;
import pl.otros.logview.LogInvestiagionPersitanceUtil;
import pl.otros.logview.gui.LogDataTableModel.Memento;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;
import pl.otros.logview.store.file.FileLogDataStore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BatchProcessingContext {

  public enum LogDataStoreType {
    FILE, MEMORY
  }

  private FileObject currentFile;
  private List<FileObject> allFiles;
  private HashMap<String, Object> attributes = new HashMap<String, Object>();
  private DataConfiguration configuration;
  private boolean verbose;
  private LogDataStore dataStore;

  public BatchProcessingContext() {
    configuration = new DataConfiguration(new BaseConfiguration());
  }

  public LogDataStore createLogDataStore(LogDataStoreType type) throws IOException {
    if (LogDataStoreType.MEMORY == type) {
      dataStore = new MemoryLogDataStore();
    } else {
      dataStore = new FileLogDataStore();
    }
    return dataStore;
  }

  public void saveLogDataStore(String fileBaseName, String logName) throws IOException {

    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(fileBaseName + ".zip.olv");
      saveLogDataStore(fout, fileBaseName);
    } finally {
      IOUtils.closeQuietly(fout);
    }
  }

  public void saveLogDataStore(OutputStream out, String logName) throws IOException {
    ArrayList<LogData> list = new ArrayList<LogData>() {

      @Override
      public Iterator<LogData> iterator() {
        return dataStore.iterator();
      }
    };

    Memento memento = new Memento();
    memento.setList(list);
    memento.setName(String.format("%s on %s", logName, InetAddress.getLocalHost().getHostName()));
    for (LogData logData : dataStore) {
      if (logData.isMarked()) {
        memento.getMarks().put(Integer.valueOf(logData.getId()), Boolean.TRUE);
        memento.getMarksColor().put(Integer.valueOf(logData.getId()), logData.getMarkerColors());
      }
      if (logData.getNote() != null) {
        memento.getNotes().put(Integer.valueOf(logData.getId()), logData.getNote());
      }
    }
    LogInvestiagionPersitanceUtil.saveMemento(memento, out);

  }

  public void printIfVerbose(String message) {
    if (verbose) {
      System.out.println(message);
    }
  }

  public void printIfVerbose(String message, Object... args) {
    if (verbose) {
      System.out.println(String.format(message, args));
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(String key, Class<T> clazz) {
    return (T) attributes.get(key);
  }

  public <T> T getAttribute(String key, Class<T> clazz, T defaultValue) {
    T result = null;
    if (attributes.containsKey(key)) {
      result = getAttribute(key, clazz);
    } else {
      result = defaultValue;
    }
    return result;
  }

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  public boolean containsAttribute(String key) {
    return attributes.containsKey(key);
  }

  public List<FileObject> getAllFiles() {
    return allFiles;
  }

  public void setAllFiles(List<FileObject> allFiles) {
    this.allFiles = allFiles;
  }

  public FileObject getCurrentFile() {
    return currentFile;
  }

  public void setCurrentFile(FileObject currentFile) {
    this.currentFile = currentFile;
  }

  public DataConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(DataConfiguration configuration) {
    this.configuration = configuration;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public LogDataStore getDataStore() {
    return dataStore;
  }

}
