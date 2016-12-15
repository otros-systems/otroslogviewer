package pl.otros.logview.gui;

import com.google.common.primitives.Ints;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
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
import pl.otros.logview.api.io.ContentProbe;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.loading.LogLoadingSession;
import pl.otros.logview.api.loading.VfsSource;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.gui.renderers.ContentProbeRenderer;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.gui.renderers.LogImporterRenderer;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.swing.Progress;
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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdvanceOpenPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdvanceOpenPanel.class.getName());
  private final FileObjectToImportTableModel tableModel;
  private final AbstractAction importAction;
  private final CardLayout cardLayout;
  private final JProgressBar loadingProgressBar;

  private final AbstractAction deleteSelectedAction;
  private final AbstractAction addMoreFilesAction;


  enum OpenMode {
    FROM_START, ALMOST_END
  }

  enum CanParse {
    YES("Yes"), NO("No"), FILE_TOO_SMALL("File too small"), NOT_TESTED("Not checked"), TESTING("Checking"), TESTING_ERROR("Testing error");
    private String msg;

    CanParse(String msg) {
      this.msg = msg;
    }

    public String getMsg() {
      return msg;
    }
  }


  //TODO handle drag and drop
  //TODO context menu with delete
  //TODO sorting
  //TODO view for empty table
  //TODO Open file from beggining/end
  //TODO create log parser pattern if some logs can't be parsed
  //TODO save state
  //preview of content
  // select log parser
  public AdvanceOpenPanel(OtrosApplication otrosApplication) {
    cardLayout = new CardLayout();
    this.setLayout(cardLayout);
    final JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    loadingProgressBar = new JProgressBar();
    loadingProgressBar.setSize(400,20);
    loadingProgressBar.setMinimum(0);
    loadingProgressBar.setStringPainted(true);
    loadingProgressBar.setString("loading ...");
    loadingPanel.add(loadingProgressBar, BorderLayout.CENTER);
    final JPanel mainPanel = new JPanel(new BorderLayout());

    this.add(mainPanel, "mainPanel");
    this.add(loadingPanel, "loadingPanel");
    final JToolBar jToolBar = new JToolBar();
    addMoreFilesAction = new AbstractAction("Add more files", Icons.ADD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        final JOtrosVfsBrowserDialog browserDialog = otrosApplication.getOtrosVfsBrowserDialog();
        browserDialog.showOpenDialog(AdvanceOpenPanel.this, "Select log file");
        final FileObject[] selectedFiles = browserDialog.getSelectedFiles();
        showLoadingPanel();
        new SwingWorker<Void, FileObjectToImport>() {

          @Override
          protected Void doInBackground() throws Exception {
            try {
              for (FileObject f : selectedFiles) {
                if (f.getType() == FileType.FOLDER) {
                  continue;
                }
                final FileObjectToImport fileObjectToImport = new FileObjectToImport(
                    f,
                    f.getName(),
                    new FileSize(f.getContent().getSize()),
                    Level.FINEST,
                    OpenMode.ALMOST_END,
                    CanParse.NOT_TESTED);
                //TODO remove it later
                Thread.sleep(400);
                publish(fileObjectToImport);
              }
              return null;
            } catch (Exception e) {
              return null;
            }
          }

          @Override
          protected void process(List<FileObjectToImport> chunks) {
            chunks.stream().filter(f -> !tableModel.contains(f.getFileObject())).forEach(tableModel::add);
            chunks.forEach(f -> loadingProgressBar.setString("Reading " + f.getFileName().getBaseName()));
          }

          @Override
          protected void done() {
            showMainPanel();
          }
        }.execute();
      }
    };
    jToolBar.add(new JButton(addMoreFilesAction));
    mainPanel.add(jToolBar, BorderLayout.NORTH);
    tableModel = new FileObjectToImportTableModel();

    tableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        loadingProgressBar.setMaximum(tableModel.getColumnCount());
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

    table.setDefaultRenderer(PossibleLogImporters.class, new DefaultTableCellRenderer(){
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof PossibleLogImporters) {
          PossibleLogImporters poss = (PossibleLogImporters) value;
          final String text = poss.getLogImporter().map(LogImporter::getName).orElse("N/A");
          component.setText(text);
        }
        return component;
      }
    });
    table.setDefaultRenderer(ContentProbe.class,new ContentProbeRenderer());

    final JComboBox comboBox = new JComboBox();
    comboBox.setRenderer(new LogImporterRenderer());
    table.setDefaultEditor(PossibleLogImporters.class, new DefaultCellEditor(comboBox){
      @Override
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final JComboBox cbx = (JComboBox) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        final PossibleLogImporters importers = (PossibleLogImporters) value;
        cbx.removeAllItems();
        importers.getAvailableImporters().forEach(cbx::addItem);
        importers.getLogImporter().ifPresent(cbx::setSelectedItem);
        return cbx;
      }
    });

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
    deleteSelectedAction = new AbstractAction("Delete selected",Icons.DELETE) {
      @Override
      public void actionPerformed(ActionEvent e) {
        tableModel.delete(table.getSelectedRows());
      }
    };
    table.getActionMap().put("DELETE", deleteSelectedAction);
    table.getColumn("Size").setMaxWidth(60);
    table.getColumn("Level threshold").setMaxWidth(120);
    table.getColumn("Open mode").setMaxWidth(120);
    initContextMenu(table);


    importAction = new AbstractAction("Import") {

      final class LoadingBean {

        LoadingBean(FileObjectToImport fileObjectToImport, LoadingInfo loadingInfo) {
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
        showLoadingPanel();
        final int rowCount = tableModel.getRowCount();
        final LogViewPanelWrapper logViewPanelWrapper = new LogViewPanelWrapper(
            "Multiple log files " + rowCount,
            null,
            visibleColumns,
            otrosApplication);

        final LogLoader logLoader = otrosApplication.getLogLoader();
        LogDataCollector logDataCollector = logViewPanelWrapper.getDataTableModel();
        logViewPanelWrapper.goToLiveMode();
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
        configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);

        final SwingWorker<List<LoadingBean>, Progress> swingWorker = new SwingWorker<List<LoadingBean>, Progress>() {

          @Override
          protected List<LoadingBean> doInBackground() throws Exception {
            LogImporter importer = new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements());
            try {
              importer.init(new Properties());
            } catch (InitializationException e1) {
              LOGGER.error("Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage());
              showMainPanel();
              throw e1;
            }
            final List<FileObjectToImport> fileObjects = IntStream.range(0, rowCount).mapToObj(tableModel::getFileObjectToImport).collect(Collectors.toList());
            ArrayList<LoadingBean> loadingBeans = new ArrayList<>();
            int progress = 0;
            for (final FileObjectToImport file : fileObjects) {
              try {
                progress++;
                if (file.getCanParse() != CanParse.FILE_TOO_SMALL && file.getCanParse() != CanParse.YES){
                  continue;
                }
                publish(new Progress(progress, fileObjects.size(), "Processing " + file.getFileName().getBaseName()));
                final LoadingInfo e1 = Utils.openFileObject(file.getFileObject(), true);
                loadingBeans.add(new LoadingBean(file, e1));
              } catch (Exception e1) {
                final String msg = String.format("Can't open file %s: %s", file.getFileName().getFriendlyURI(), e1.getMessage());
                LOGGER.warn(msg);
                //TODO handle errors!
//                JOptionPane.showConfirmDialog(AdvanceOpenPanel.this, msg, "Error opening file", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                loadingBeans.forEach(li -> Utils.closeQuietly(li.loadingInfo.getFileObject()));
                showMainPanel();
                throw e1;
              }
            }
            loadingBeans.forEach(loadingInfo -> {
              final LogLoadingSession session = logLoader.startLoading(
                  new VfsSource(loadingInfo.loadingInfo.getFileObject()), importer, logDataCollector, 3000, Optional.of(2000L));
              logLoader.changeFilters(session, new LevelHigherOrEqualAcceptCondition(loadingInfo.fileObjectToImport.getLevel()));
            });
            logViewPanelWrapper.onClose(() -> logLoader.close(logDataCollector));
            return loadingBeans;
          }

          @Override
          protected void process(List<Progress> chunks) {
            chunks.forEach(progress -> {
              loadingProgressBar.setMinimum(progress.getMin());
              loadingProgressBar.setMaximum(progress.getMax());
              loadingProgressBar.setValue(progress.getValue());
              progress.getMessage().ifPresent(loadingProgressBar::setString);
            });
          }

          @Override
          protected void done() {
            try {
              final List<LoadingBean> loadingBeans = get();
              String tooltip = loadingBeans.stream()
                  .map(l -> l.loadingInfo)
                  .map(LoadingInfo::getFriendlyUrl)
                  .collect(Collectors.joining("<br>", "<html>Multiple files:<br>", "</html>"));
              otrosApplication.addClosableTab(String.format("Multiple logs [%d]", loadingBeans.size()), tooltip, Icons.ARROW_REPEAT, logViewPanelWrapper, true);
              otrosApplication.getjTabbedPane().remove(AdvanceOpenPanel.this);
            } catch (InterruptedException | ExecutionException e1) {
              JOptionPane.showMessageDialog(AdvanceOpenPanel.this, "Error opening logs: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
        };
        swingWorker.execute();
      }
    };
    importAction.setEnabled(false);
    final JButton importButton = new JButton(importAction);
    final Font font = importButton.getFont();
    importButton.setFont(font.deriveFont(font.getSize()*2f));
    mainPanel.add(importButton, BorderLayout.SOUTH);
    tableModel.addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        final int firstRow = e.getFirstRow();
        final int lastRow = e.getLastRow();
        for (int i = firstRow; i < lastRow; i++) {
          final FileObjectToImport fileObjectAt = tableModel.getFileObjectToImport(i);
          LOGGER.info("Added " + fileObjectAt + " to table");
          class AddingDetail {
            public AddingDetail(CanParse canParse, PossibleLogImporters possibleLogImporters, ContentProbe contentProbe) {
              this.canParse = canParse;
              this.possibleLogImporters = possibleLogImporters;
              this.contentProbe = contentProbe;
            }

            CanParse canParse;
            PossibleLogImporters possibleLogImporters;
            ContentProbe contentProbe;
          }
          final SwingWorker<Void, AddingDetail> swingWorker = new SwingWorker<Void, AddingDetail>() {

            @Override
            protected void process(List<AddingDetail> chunks) {
              chunks.forEach(c -> {
                final FileObject fileObject = fileObjectAt.getFileObject();
                tableModel.setCanParse(fileObject, c.canParse);
                tableModel.setContent(fileObject, c.contentProbe);
                tableModel.setPossibleLogImporters(fileObject, c.possibleLogImporters);
              });
            }

            @Override
            protected Void doInBackground() throws Exception {
              final Collection<LogImporter> logImporters = otrosApplication.getAllPluginables().getLogImportersContainer().getElements();
              CanParse canParse = CanParse.NO;
              byte[] bytes = new byte[0];
              PossibleLogImporters possibleLogImporters = new PossibleLogImporters();
              publish(new AddingDetail(CanParse.TESTING,possibleLogImporters,new ContentProbe(bytes)));
              try (InputStream in = fileObjectAt.getFileObject().getContent().getInputStream()) {
                bytes = Utils.loadProbe(in, 4000);
                possibleLogImporters = Utils.detectPossibleLogImporter(logImporters, bytes);
                if (possibleLogImporters.getLogImporter().isPresent()) {
                  canParse = CanParse.YES;
                } else if (bytes.length == 0) {
                  canParse = CanParse.FILE_TOO_SMALL;
                }
              } catch (IOException e) {
                canParse = CanParse.TESTING_ERROR;
              }
              publish(new AddingDetail(canParse,possibleLogImporters,new ContentProbe(bytes)));
              return null;
            }
          };
          swingWorker.execute();
        }
      }
    });

    final JScrollPane scrollPane = new JScrollPane(table);
    mainPanel.add(scrollPane);

  }

  private void initContextMenu(JTable table) {
    JPopupMenu popupMenu = new JPopupMenu("Options");
    popupMenu.add(addMoreFilesAction);
    popupMenu.add(deleteSelectedAction);
    table.addMouseListener(new PopupListener(popupMenu));
  }

  private void showLoadingPanel() {
    cardLayout.show(AdvanceOpenPanel.this, "loadingPanel");
  }

  private void showMainPanel() {
    cardLayout.show(AdvanceOpenPanel.this, "mainPanel");
  }
}

class FileObjectToImportTableModel extends AbstractTableModel {

  public static final int COLUMN_NAME = 0;
  public static final int COLUMN_SIZE = 1;
  public static final int COLUMN_LEVEL = 2;
  public static final int COLUMN_OPEN_MODE = 3;
  public static final int COLUMN_CAN_PARSE = 4;
  public static final int COLUMN_POSSIBLE_IMPORTER = 5;
  public static final int COLUMN_CONTENT = 6;
  public static final String[] columnNames = new String[]{"Name", "Size", "Level threshold", "Open mode", "Can parse", "Log importer","Content"};

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
    columnClasses.put(COLUMN_POSSIBLE_IMPORTER, PossibleLogImporters.class);
    columnClasses.put(COLUMN_CONTENT, ContentProbe.class);
    editableColumns = new HashSet<>();
    editableColumns.add(COLUMN_OPEN_MODE);
    editableColumns.add(COLUMN_LEVEL);
    editableColumns.add(COLUMN_POSSIBLE_IMPORTER);
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
      fileObjectToImport.setOpenMode((AdvanceOpenPanel.OpenMode) aValue);
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

  public void setContent(FileObject fileObject, ContentProbe contentProbe) {
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setContent(contentProbe);
        fireTableCellUpdated(i, COLUMN_CONTENT);
      }
    }
  }
  public void setPossibleLogImporters(FileObject fileObject, PossibleLogImporters possibleLogImporters){
    for (int i = 0; i < data.size(); i++) {
      FileObjectToImport f = data.get(i);
      if (f.getFileObject().equals(fileObject)) {
        f.setPossibleLogImporters(possibleLogImporters);
        fireTableCellUpdated(i, COLUMN_POSSIBLE_IMPORTER);
      }
    }

  }

  public boolean contains(FileObject fileObject){
    return data.stream().anyMatch(f->f.getFileObject().equals(fileObject));
  }
}

class FileObjectToImport {
  private final FileObject fileObject;
  private final FileName fileName;
  private final FileSize fileSize;
  private Level level;
  private AdvanceOpenPanel.OpenMode openMode;
  private AdvanceOpenPanel.CanParse canParse;
  private ContentProbe content;
  private PossibleLogImporters possibleLogImporters;

  public FileObjectToImport(FileObject fileObject, FileName fileName, FileSize fileSize, Level level, AdvanceOpenPanel.OpenMode openMode, AdvanceOpenPanel
      .CanParse canParse) {
    this.fileObject = fileObject;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.level = level;
    this.openMode = openMode;
    this.canParse = canParse;
  }

  public PossibleLogImporters getPossibleLogImporters() {
    return possibleLogImporters;
  }

  public void setPossibleLogImporters(PossibleLogImporters possibleLogImporters) {
    this.possibleLogImporters = possibleLogImporters;
  }

  public ContentProbe getContent() {
    return content;
  }

  public void setContent(ContentProbe content) {
    this.content = content;
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
