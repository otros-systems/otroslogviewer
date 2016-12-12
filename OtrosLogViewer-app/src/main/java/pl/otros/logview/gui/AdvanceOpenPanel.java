package pl.otros.logview.gui;

import com.google.common.primitives.Ints;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.RenamedLevel;
import pl.otros.logview.accept.LevelHigherOrEqualAcceptCondition;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.loading.LogLoadingSession;
import pl.otros.logview.api.loading.VfsSource;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.table.FileSize;
import pl.otros.vfs.browser.table.FileSizeTableCellRenderer;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdvanceOpenPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdvanceOpenPanel.class.getName());
  private final FileObjectToImportTableModel tableModel;
  private final AbstractAction importAction;


  public enum OpenMode {
    FROM_START, ALMOST_END
  }

  public enum CanParse {
    YES("Yes"), NO("No"), FILE_TOO_SMALL("File too small"), NOT_TESTED("Not checked"), TESTING("Checking"), TESTING_ERROR("Testing error");
    private String msg;

    CanParse(String msg) {
      this.msg = msg;
    }

    public String getMsg() {
      return msg;
    }
  }


  public AdvanceOpenPanel(OtrosApplication otrosApplication) {

    this.setLayout(new BorderLayout());
    final JToolBar jToolBar = new JToolBar();
    jToolBar.add(new AbstractAction("Add new") {
      @Override
      public void actionPerformed(ActionEvent e) {
        final JOtrosVfsBrowserDialog browserDialog = otrosApplication.getOtrosVfsBrowserDialog();
        browserDialog.showOpenDialog(AdvanceOpenPanel.this, "Select log file");
        final FileObject[] selectedFiles = browserDialog.getSelectedFiles();
        Arrays.asList(selectedFiles).forEach(f -> {
          try {
            final FileObjectToImport fileObjectToImport = new FileObjectToImport(
                f,
                f.getName(),
                new FileSize(f.getContent().getSize()),
                Level.FINEST,
                OpenMode.ALMOST_END,
                CanParse.NOT_TESTED);
            tableModel.add(fileObjectToImport);
          } catch (FileSystemException e1) {
            e1.printStackTrace();
          }
        });

      }
    });
    this.add(jToolBar, BorderLayout.NORTH);
    tableModel = new FileObjectToImportTableModel();

    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        importAction.setEnabled(tableModel.getRowCount() > 0);
      }
    });

    JTable table = new JTable(tableModel);
    table.setDefaultRenderer(FileObject.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String path = value.toString();
        if (value instanceof FileObject) {
          FileObject fileObject = (FileObject) value;
          path = fileObject.getName().getPath();
        }
        return super.getTableCellRendererComponent(table, path, isSelected, hasFocus, row, column);
      }
    });

    table.setDefaultRenderer(OpenMode.class, new DefaultTableCellRenderer());
    table.setDefaultEditor(OpenMode.class, new DefaultCellEditor(new JComboBox(OpenMode.values())));

    table.setDefaultRenderer(FileSize.class, new FileSizeTableCellRenderer());
    table.setDefaultRenderer(Level.class, new LevelRenderer(LevelRenderer.Mode.IconsAndText));
    Level[] levels = {
        RenamedLevel.FINEST_TRACE,
        RenamedLevel.FINER,
        RenamedLevel.FINE_DEBUG,
        RenamedLevel.CONFIG,
        RenamedLevel.INFO,
        RenamedLevel.WARNING_WARN,
        RenamedLevel.SEVERE_ERROR_FATAL
    };
    table.setDefaultEditor(Level.class, new DefaultCellEditor(new JComboBox(levels)));
    table.setDefaultRenderer(CanParse.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        CanParse canParse = (CanParse) value;
        final JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Icon i = Icons.STATUS_UNKNOWN;
        switch (canParse) {
          case NOT_TESTED:
            i = Icons.STATUS_UNKNOWN;
            break;
          case NO:
            i = Icons.STATUS_ERROR;
            break;
          case YES:
            i = Icons.STATUS_OK;
            break;
          case FILE_TOO_SMALL:
            i = Icons.AUTOMATIC_UNMARKERS;
            break;
          case TESTING:
            i = Icons.MAGNIFIER;
            break;
        }
        component.setText(canParse.getMsg());
        component.setIcon(i);

        return component;
      }
    });
    table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE");
    table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
    table.getActionMap().put("DELETE", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        tableModel.delete(table.getSelectedRows());
        System.out.println("DELEtING!");
      }
    });

    this.add(new JScrollPane(table));
    importAction = new AbstractAction("Import") {

      final class LoadingBean {

        public LoadingBean(FileObjectToImport fileObjectToImport, LoadingInfo loadingInfo) {
          this.fileObjectToImport = fileObjectToImport;
          this.loadingInfo = loadingInfo;
        }

        FileObjectToImport fileObjectToImport;
        LoadingInfo loadingInfo;
      }

      TableColumns[] visibleColumns = {
          TableColumns.ID,//
          TableColumns.TIME,//
          TableColumns.LEVEL,//
          TableColumns.MESSAGE,//
          TableColumns.CLASS,//
          TableColumns.METHOD,//
          TableColumns.THREAD,//
          TableColumns.MARK,//
          TableColumns.NOTE,//
          TableColumns.LOG_SOURCE

      };
      @Override
      public void actionPerformed(ActionEvent e) {

        //TODO use SwingWorker because of IO operations (possibly on remote file systems)
        final int rowCount = tableModel.getRowCount();
        final LogViewPanelWrapper logViewPanelWrapper = new LogViewPanelWrapper(
            "Multiple log files " + rowCount,
            null,
            visibleColumns,
            otrosApplication);
        final LogLoader logLoader = otrosApplication.getLogLoader();
        LogDataCollector logDataCollector = logViewPanelWrapper.getDataTableModel();

        LogImporter importer = new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements());
        try {
          importer.init(new Properties());
        } catch (InitializationException e1) {
          LOGGER.error("Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage());
          JOptionPane.showMessageDialog(AdvanceOpenPanel.this, "Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage(), "Open error",
              JOptionPane.ERROR_MESSAGE);

        }


        final List<FileObjectToImport> fileObjects = IntStream.range(0, rowCount).mapToObj(tableModel::getFileObjectToImport).collect(Collectors.toList());

        ArrayList<LoadingBean> loadingBeans = new ArrayList<>();
        for (final FileObjectToImport file : fileObjects) {
          try {
            final LoadingInfo e1 = Utils.openFileObject(file.getFileObject(), true);
            loadingBeans.add(new LoadingBean(file,e1));
          } catch (Exception e1) {
            final String msg = String.format("Can't open file %s: %s", file.getFileName().getFriendlyURI(), e1.getMessage());
            LOGGER.warn(msg);
            JOptionPane.showConfirmDialog(AdvanceOpenPanel.this,msg,"Error opening file", JOptionPane.DEFAULT_OPTION,JOptionPane.ERROR_MESSAGE);
            loadingBeans.forEach(li -> Utils.closeQuietly(li.loadingInfo.getFileObject()));
            return ;
          }
        }
        final java.util.List<LogLoadingSession> collect = loadingBeans.stream()
            .map(loadingInfo -> {
              final LogLoadingSession session = logLoader.startLoading(
                  new VfsSource(loadingInfo.loadingInfo.getFileObject()), importer, logDataCollector, 3000, Optional.of(2000L));
              logLoader.changeFilters(session,new LevelHigherOrEqualAcceptCondition( loadingInfo.fileObjectToImport.getLevel()));
              return session;
            })
            .collect(Collectors.toList());

        logViewPanelWrapper.goToLiveMode();
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
        configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);

        LOGGER.info("Have sessions: {}" , collect.stream().map(LogLoadingSession::getId).collect(Collectors.joining(", ")));

        logViewPanelWrapper.onClose(() -> logLoader.close(logDataCollector));

        String tooltip = loadingBeans.stream()
            .map(l->l.loadingInfo)
            .map(LoadingInfo::getFriendlyUrl)
            .collect(Collectors.joining(",","<html>Multiple files:<br>", "</html>"));
        otrosApplication.addClosableTab(String.format("Multiple logs [%d]", loadingBeans.size()), tooltip, Icons.ARROW_REPEAT, logViewPanelWrapper, true);
        otrosApplication.getjTabbedPane().remove(AdvanceOpenPanel.this);

      }
    };
    importAction.setEnabled(false);
    this.add(new JButton(importAction), BorderLayout.SOUTH);
    tableModel.addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        final int firstRow = e.getFirstRow();
        final int lastRow = e.getLastRow();
        for (int i = firstRow; i < lastRow; i++) {
          final FileObjectToImport fileObjectAt = tableModel.getFileObjectToImport(i);
          LOGGER.info("Added " + fileObjectAt + " to table");
          final SwingWorker<Void, CanParse> swingWorker = new SwingWorker<Void, CanParse>() {

            @Override
            protected void process(List<CanParse> chunks) {
              chunks.forEach(c -> tableModel.setCanParse(fileObjectAt.getFileObject(), c));
            }


            @Override
            protected Void doInBackground() throws Exception {
              publish(CanParse.TESTING);
              final Collection<LogImporter> logImporters = otrosApplication.getAllPluginables().getLogImportersContainer().getElements();
              try (InputStream in = fileObjectAt.getFileObject().getContent().getInputStream()) {
                final byte[] bytes = Utils.loadProbe(in, 4000);
                final PossibleLogImporters possibleLogImporters = Utils.detectPossibleLogImporter(logImporters, bytes);
                if (possibleLogImporters.getLogImporter().isPresent()) {
                  publish(CanParse.YES);
                } else if (bytes.length == 0) {
                  publish(CanParse.FILE_TOO_SMALL);
                } else {
                  publish(CanParse.NO);
                }
              } catch (IOException e) {
                publish(CanParse.TESTING_ERROR);
              }
              return null;
            }
          };
          swingWorker.execute();
        }
      }
    });
  }
}

class FileObjectToImportTableModel extends AbstractTableModel {

  public static final int COLUMN_NAME = 0;
  public static final int COLUMN_SIZE = 1;
  public static final int COLUMN_LEVEL = 2;
  public static final int COLUMN_OPEN_MODE = 3;
  public static final int COLUMN_CAN_PARSE = 4;
  public static final String[] columnNames = new String[]{"Name", "Size", "Level threshold", "Open mode", "Can parse"};

  private java.util.List<FileObjectToImport> data = new ArrayList<>();
  private final HashMap<Integer, Class> columnClasses;
  private final HashSet<Integer> editableColumns;

  public FileObjectToImportTableModel() {
    columnClasses = new HashMap<>();
    columnClasses.put(COLUMN_NAME, FileName.class);
    columnClasses.put(COLUMN_SIZE, FileSize.class);
    columnClasses.put(COLUMN_LEVEL, Level.class);
    columnClasses.put(COLUMN_OPEN_MODE, AdvanceOpenPanel.OpenMode.class);
    columnClasses.put(COLUMN_CAN_PARSE, AdvanceOpenPanel.CanParse.class);
    editableColumns = new HashSet<>();
    editableColumns.add(COLUMN_OPEN_MODE);
    editableColumns.add(COLUMN_LEVEL);
  }

  public void add(FileObjectToImport fileObjectToImport) {
    data.add(fileObjectToImport);
    fireTableRowsInserted(data.size() - 1, data.size());
  }

  public void delete(int... rows) {
    Arrays.sort(rows);
    final List<Integer> rowsList = Ints.asList(rows);
    Collections.reverse(rowsList);
    rowsList.forEach(row -> {
      data.remove(row.intValue());
      fireTableRowsDeleted(row, row);
    });
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
    }
    return "";
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    final FileObjectToImport fileObjectToImport = data.get(rowIndex);
    if (columnIndex == COLUMN_OPEN_MODE) {
      fileObjectToImport.setOpenMode((AdvanceOpenPanel.OpenMode) aValue);
    } else if (columnIndex == COLUMN_LEVEL) {
      fileObjectToImport.setLevel((Level) aValue);
    } else if (columnIndex == COLUMN_CAN_PARSE) {
      fileObjectToImport.getCanParse();
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

  public FileObjectToImport getFileObjectToImport(int rowIndex) {
    return data.get(rowIndex);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClasses.get(columnIndex);
  }

  public void setCanParse(FileObject fileObject, AdvanceOpenPanel.CanParse canParse) {
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setCanParse(canParse);
        fireTableCellUpdated(i, COLUMN_CAN_PARSE);
      }
    }
  }
}

class FileObjectToImport {
  private FileObject fileObject;
  private FileName fileName;
  private FileSize fileSize;
  private Level level;
  private AdvanceOpenPanel.OpenMode openMode;
  private AdvanceOpenPanel.CanParse canParse;

  public FileObjectToImport(FileObject fileObject, FileName fileName, FileSize fileSize, Level level, AdvanceOpenPanel.OpenMode openMode, AdvanceOpenPanel
      .CanParse canParse) {
    this.fileObject = fileObject;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.level = level;
    this.openMode = openMode;
    this.canParse = canParse;
  }

  public FileObject getFileObject() {
    return fileObject;
  }

  public FileName getFileName() {
    return fileName;
  }

  public FileSize getFileSize() {
    return fileSize;
  }

  public Level getLevel() {
    return level;
  }

  public AdvanceOpenPanel.OpenMode getOpenMode() {
    return openMode;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  public void setOpenMode(AdvanceOpenPanel.OpenMode openMode) {
    this.openMode = openMode;
  }

  public AdvanceOpenPanel.CanParse getCanParse() {
    return canParse;
  }

  public void setCanParse(AdvanceOpenPanel.CanParse canParse) {
    this.canParse = canParse;
  }
}
