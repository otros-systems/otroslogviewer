package pl.otros.logview.gui.open;

import com.google.common.primitives.Ints;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.ContentProbe;
import pl.otros.logview.gui.session.OpenMode;
import pl.otros.vfs.browser.table.FileSize;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

class FileObjectToImportTableModel extends AbstractTableModel {

  private static final int COLUMN_NAME = 0;
  private static final int COLUMN_SIZE = 1;
  private static final int COLUMN_LEVEL = 2;
  private static final int COLUMN_OPEN_MODE = 3;
  private static final int COLUMN_CAN_PARSE = 4;
  private static final int COLUMN_POSSIBLE_IMPORTER = 5;
  private static final int COLUMN_CONTENT = 6;
  private static final String[] columnNames = new String[]{"Name", "Size", "Level threshold", "Open mode", "Can parse", "Log importer", "Content"};

  private java.util.List<FileObjectToImport> data = new ArrayList<>();
  private final HashMap<Integer, Class> columnClasses;
  private final HashSet<Integer> editableColumns;

  FileObjectToImportTableModel() {
    columnClasses = new HashMap<>();
    columnClasses.put(COLUMN_NAME, FileName.class);
    columnClasses.put(COLUMN_SIZE, FileSize.class);
    columnClasses.put(COLUMN_LEVEL, Level.class);
    columnClasses.put(COLUMN_OPEN_MODE, OpenMode.class);
    columnClasses.put(COLUMN_CAN_PARSE, CanParse.class);
    columnClasses.put(COLUMN_POSSIBLE_IMPORTER, PossibleLogImporters.class);
    columnClasses.put(COLUMN_CONTENT, ContentProbe.class);
    editableColumns = new HashSet<>();
    editableColumns.add(COLUMN_OPEN_MODE);
    editableColumns.add(COLUMN_LEVEL);
    editableColumns.add(COLUMN_POSSIBLE_IMPORTER);
  }

  public List<FileObjectToImport> getData() {
    return new ArrayList<>(data);
  }

  public void add(FileObjectToImport fileObjectToImport) {
    data.add(fileObjectToImport);
    fireTableRowsInserted(data.size() - 1, data.size() - 1);
  }

  void delete(int... rows) {
    Arrays.sort(rows);
    final List<Integer> rowsList = Ints.asList(rows);
    Collections.reverse(rowsList);
    rowsList.forEach(row -> {
      data.remove(row.intValue());
      fireTableRowsDeleted(row, row);
    });
  }

  void clear() {
    data.clear();
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    final FileObjectToImport fileObjectToImport = data.get(rowIndex);
    if (columnIndex == COLUMN_NAME) {
      return fileObjectToImport.getFileName();
    } else if (columnIndex == COLUMN_SIZE) {
      return fileObjectToImport.getFileSize();
    } else if (columnIndex == COLUMN_OPEN_MODE) {
      return fileObjectToImport.getOpenMode();
    } else if (columnIndex == COLUMN_LEVEL) {
      return fileObjectToImport.getLevel();
    } else if (columnIndex == COLUMN_CAN_PARSE) {
      return fileObjectToImport.getCanParse();
    } else if (columnIndex == COLUMN_POSSIBLE_IMPORTER) {
      return fileObjectToImport.getPossibleLogImporters();
    } else if (columnIndex == COLUMN_CONTENT) {
      return fileObjectToImport.getContent();
    }
    return "";
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    final FileObjectToImport fileObjectToImport = data.get(rowIndex);
    if (columnIndex == COLUMN_OPEN_MODE) {
      fileObjectToImport.setOpenMode((OpenMode) aValue);
    } else if (columnIndex == COLUMN_LEVEL) {
      fileObjectToImport.setLevel((Level) aValue);
    } else if (columnIndex == COLUMN_POSSIBLE_IMPORTER) {
      LogImporter logImporter = (LogImporter) aValue;
      final PossibleLogImporters possibleLogImporters = fileObjectToImport.getPossibleLogImporters();
      possibleLogImporters.setLogImporter(Optional.of(logImporter));
    }
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return editableColumns.contains(columnIndex);
  }

  FileObjectToImport getFileObjectToImport(int rowIndex) {
    return data.get(rowIndex);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClasses.get(columnIndex);
  }

  void setCanParse(FileObject fileObject, CanParse canParse) {
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setCanParse(canParse);
        fireTableCellUpdated(i, COLUMN_CAN_PARSE);
      }
    }
  }

  void setContent(FileObject fileObject, ContentProbe contentProbe) {
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setContent(contentProbe);
        fireTableCellUpdated(i, COLUMN_CONTENT);
      }
    }
  }

  void setPossibleLogImporters(FileObject fileObject, PossibleLogImporters possibleLogImporters) {
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setPossibleLogImporters(possibleLogImporters);
        fireTableCellUpdated(i, COLUMN_POSSIBLE_IMPORTER);
      }
    }
  }

  void setOpenMode(FileObject fileObject, OpenMode openMode) {
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setOpenMode(openMode);
        fireTableCellUpdated(i, COLUMN_POSSIBLE_IMPORTER);
      }
    }
  }

  public boolean contains(FileObject fileObject) {
    return data.stream().anyMatch(f -> f.getFileObject().equals(fileObject));
  }
}
