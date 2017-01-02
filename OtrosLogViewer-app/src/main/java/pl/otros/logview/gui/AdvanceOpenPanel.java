package pl.otros.logview.gui;

import com.google.common.primitives.Ints;
import net.miginfocom.swing.MigLayout;
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
import pl.otros.logview.api.parser.TableColumnNameSelfDescribable;
import pl.otros.logview.gui.renderers.ContentProbeRenderer;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.gui.renderers.LogImporterRenderer;
import pl.otros.logview.gui.renderers.PossibleLogImportersRenderer;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.swing.Named;
import pl.otros.swing.Progress;
import pl.otros.swing.renderer.NamedTableRenderer;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdvanceOpenPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdvanceOpenPanel.class.getName());
  public static final String CARD_TABLE = "table";
  public static final String CARD_EMPTY_VIEW = "emptyView";

  private final FileObjectToImportTableModel tableModel;
  private final AbstractAction importAction;
  private final CardLayout cardLayout;
  private final JProgressBar loadingProgressBar;

  private final AbstractAction deleteSelectedAction;
  private final AbstractAction addMoreFilesAction;
  private final AbstractAction switchAllToFromEnd;
  private final AbstractAction switchAllToFromBegging;


  enum OpenMode implements Named {
    FROM_START("From begging"), FROM_END("From end");
    private String name;

    OpenMode(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  enum CanParse implements Named {
    YES("Yes"), NO("No"), FILE_TOO_SMALL("File too small"), NOT_TESTED("Not checked"), TESTING("Checking"), TESTING_ERROR("Testing error");
    private String name;

    CanParse(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }


  //TODO view for empty table
  //TODO sorting
  //TODO columns widths
  //TODO tail from last xxxx KB
  //TODO create log parser pattern if some logs can't be parsed
  //TODO save state
  public AdvanceOpenPanel(OtrosApplication otrosApplication) {
    cardLayout = new CardLayout();
    this.setLayout(cardLayout);
    final JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    loadingProgressBar = new JProgressBar();
    loadingProgressBar.setMinimumSize(new Dimension(800, 30));
    loadingProgressBar.setPreferredSize(new Dimension(800, 30));
    loadingProgressBar.setSize(800, 30);
    loadingProgressBar.setMinimum(0);
    loadingProgressBar.setStringPainted(true);
    loadingProgressBar.setString("loading ................");
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
        final SwingWorker<Void, FileObjectToImport> swingWorker = new AddFilesSwingWorker(selectedFiles);
        swingWorker.execute();
      }
    };

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

    table.setDefaultRenderer(OpenMode.class, new NamedTableRenderer());
    table.setDefaultEditor(OpenMode.class, new DefaultCellEditor(new JComboBox(OpenMode.values())));

    table.setDefaultRenderer(FileSize.class, new FileSizeTableCellRenderer());
    table.setDefaultRenderer(Level.class, new LevelRenderer(LevelRenderer.Mode.IconsAndText));

    final DefaultTableCellRenderer possibleLogImporterRenderer = new PossibleLogImportersRenderer();
    table.setDefaultRenderer(PossibleLogImporters.class, possibleLogImporterRenderer);
    table.setDefaultRenderer(ContentProbe.class, new ContentProbeRenderer());

    final JComboBox comboBox = new JComboBox();
    comboBox.setRenderer(new LogImporterRenderer());
    table.setDefaultEditor(PossibleLogImporters.class, new DefaultCellEditor(comboBox) {
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
    table.setDefaultRenderer(CanParse.class, new NamedTableRenderer() {
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
        component.setIcon(i);
        return component;
      }
    });
    table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE");
    table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
    deleteSelectedAction = new AbstractAction("Delete selected", Icons.DELETE) {
      @Override
      public void actionPerformed(ActionEvent e) {
        tableModel.delete(table.getSelectedRows());
      }
    };
    deleteSelectedAction.setEnabled(false);
    switchAllToFromBegging = new AbstractAction("Open selected from begging") {
      @Override
      public void actionPerformed(ActionEvent e) {
        IntStream.range(0, tableModel.getRowCount())
            .filter(table::isRowSelected)
            .forEach(i -> tableModel.setOpenMode(tableModel.getFileObjectToImport(i).getFileObject(), OpenMode.FROM_START));
      }
    };
    switchAllToFromEnd = new AbstractAction("Open selected from end") {
      @Override
      public void actionPerformed(ActionEvent e) {
        IntStream.range(0, tableModel.getRowCount())
            .filter(table::isRowSelected)
            .forEach(i -> tableModel.setOpenMode(tableModel.getFileObjectToImport(i).getFileObject(), OpenMode.FROM_END));
      }
    };


    table.getActionMap().put("DELETE", deleteSelectedAction);
    table.getColumn("Size").setMaxWidth(100);
    table.getColumn("Level threshold").setMaxWidth(120);
    table.getColumn("Open mode").setMaxWidth(120);
    table.getColumn("Content").setMaxWidth(120);
    table.getColumn("Log importer").setMaxWidth(140);
    table.getColumn("Can parse").setMaxWidth(120);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    table.getSelectionModel().addListSelectionListener((e) -> deleteSelectedAction.setEnabled(table.getSelectedRowCount() > 0));
    initContextMenu(table);
    initToolbar(jToolBar);

    importAction = new AbstractAction("Import", Icons.ARROW_JOIN_24) {

      @Override
      public void actionPerformed(ActionEvent e) {
        showLoadingPanel();
        final int rowCount = tableModel.getRowCount();
        final List<Optional<LogImporter>> logImporters = tableModel.getData()
            .stream()
            .map(l -> l.getPossibleLogImporters().getLogImporter())
            .collect(Collectors.toList());

        final LogViewPanelWrapper logViewPanelWrapper = new LogViewPanelWrapper(
            "Multiple log files " + rowCount,
            null,
            mergeColumns(logImporters),
            otrosApplication);

        final LogLoader logLoader = otrosApplication.getLogLoader();

        logViewPanelWrapper.goToLiveMode();
        BaseConfiguration configuration = new BaseConfiguration();
        configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
        configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);

        final SwingWorker<List<LoadingBean>, Progress> swingWorker = new ImportSwingWorker(logViewPanelWrapper, logLoader, otrosApplication);
        swingWorker.execute();
      }
    };
    importAction.setEnabled(false);

    final JButton importButton = new JButton(importAction);
    final Font font = importButton.getFont();
    importButton.setFont(font.deriveFont(font.getSize() * 2f));
    mainPanel.add(importButton, BorderLayout.SOUTH);
    tableModel.addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        final int firstRow = e.getFirstRow();
        final int lastRow = e.getLastRow();
        for (int i = firstRow; i < lastRow; i++) {
          final FileObjectToImport fileObjectAt = tableModel.getFileObjectToImport(i);
          LOGGER.info("Added " + fileObjectAt + " to table");
          class AddingDetail {
            AddingDetail(CanParse canParse, PossibleLogImporters possibleLogImporters, ContentProbe contentProbe) {
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
              publish(new AddingDetail(CanParse.TESTING, possibleLogImporters, new ContentProbe(bytes)));
              try (InputStream in = fileObjectAt.getFileObject().getContent().getInputStream()) {
                bytes = Utils.loadProbe(in, 4000, true);
                possibleLogImporters = Utils.detectPossibleLogImporter(logImporters, bytes);
                if (possibleLogImporters.getLogImporter().isPresent()) {
                  canParse = CanParse.YES;
                } else if (bytes.length == 0) {
                  canParse = CanParse.FILE_TOO_SMALL;
                }
              } catch (IOException e) {
                canParse = CanParse.TESTING_ERROR;
              }
              publish(new AddingDetail(canParse, possibleLogImporters, new ContentProbe(bytes)));
              return null;
            }
          };
          swingWorker.execute();
        }
      }
    });

    final JScrollPane scrollPane = new JScrollPane(table);

    JPanel emptyView = new JPanel(new MigLayout("debug, fill", "[center]", "[center]"));
    emptyView.add(new JButton(addMoreFilesAction));
    final CardLayout cardLayoutTablePanel = new CardLayout();
    final JPanel tablePanel = new JPanel(cardLayoutTablePanel);
    tablePanel.add(scrollPane, CARD_TABLE);
    tablePanel.add(emptyView, CARD_EMPTY_VIEW);
    cardLayoutTablePanel.show(tablePanel, CARD_EMPTY_VIEW);
    tableModel.addTableModelListener(e -> {
      if (tableModel.getRowCount() == 0) {
        cardLayoutTablePanel.show(tablePanel, CARD_EMPTY_VIEW);
      } else {
        cardLayoutTablePanel.show(tablePanel, CARD_TABLE);
      }
    });

    mainPanel.add(tablePanel, BorderLayout.CENTER);

  }

  private void initToolbar(JToolBar toolBar) {
    toolBar.add(new JButton(addMoreFilesAction));
    toolBar.add(new JButton(deleteSelectedAction));
  }

  private void initContextMenu(JTable table) {
    JPopupMenu popupMenu = new JPopupMenu("Options");
    popupMenu.add(addMoreFilesAction);
    popupMenu.add(deleteSelectedAction);
    popupMenu.add(switchAllToFromBegging);
    popupMenu.add(switchAllToFromEnd);
    table.addMouseListener(new PopupListener(popupMenu));
  }

  private void showLoadingPanel() {
    cardLayout.show(AdvanceOpenPanel.this, "loadingPanel");
  }

  private void showMainPanel() {
    cardLayout.show(AdvanceOpenPanel.this, "mainPanel");
  }

  private TableColumns[] mergeColumns(List<Optional<LogImporter>> logImporters) {
    Set<TableColumns> s = logImporters.stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(l -> !(l instanceof DetectOnTheFlyLogImporter))
        .filter(l -> l instanceof TableColumnNameSelfDescribable)
        .map(l -> (TableColumnNameSelfDescribable) l)
        .flatMap(l -> Arrays.stream(l.getTableColumnsToUse()))
        .collect(Collectors.toSet());
    return s.toArray(new TableColumns[0]);
  }


  private class AddFilesSwingWorker extends SwingWorker<Void, FileObjectToImport> {

    private final FileObject[] selectedFiles;

    AddFilesSwingWorker(FileObject[] selectedFiles) {
      this.selectedFiles = selectedFiles;
    }

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
              OpenMode.FROM_END,
              CanParse.NOT_TESTED);
          Thread.sleep(100);
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
  }


  private class ImportSwingWorker extends SwingWorker<List<LoadingBean>, Progress> {

    private final LogViewPanelWrapper logViewPanelWrapper;
    private final LogLoader logLoader;
    private final OtrosApplication otrosApplication;

    ImportSwingWorker(LogViewPanelWrapper logViewPanelWrapper, LogLoader logLoader, OtrosApplication otrosApplication) {
      this.logViewPanelWrapper = logViewPanelWrapper;
      this.logLoader = logLoader;
      this.otrosApplication = otrosApplication;
    }

    @Override
    protected List<LoadingBean> doInBackground() throws Exception {
      LogDataCollector logDataCollector = logViewPanelWrapper.getDataTableModel();
      LogImporter detectLaterImporter = new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements());
      try {
        detectLaterImporter.init(new Properties());
      } catch (InitializationException e1) {
        LOGGER.error("Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage());
        showMainPanel();
        throw e1;
      }
      final List<FileObjectToImport> fileObjects = IntStream.range(0, tableModel.getRowCount()).mapToObj(tableModel::getFileObjectToImport).collect(Collectors
          .toList());
      ArrayList<LoadingBean> loadingBeans = new ArrayList<>();
      int progress = 0;
      for (final FileObjectToImport file : fileObjects) {
        try {
          progress++;
          if (file.getCanParse() != CanParse.FILE_TOO_SMALL && file.getCanParse() != CanParse.YES) {
            continue;
          }
          publish(new Progress(progress, fileObjects.size(), "Processing " + file.getFileName().getBaseName()));
          final LoadingInfo e1 = Utils.openFileObject(file.getFileObject(), true);
          loadingBeans.add(new LoadingBean(file, e1));
          Utils.closeQuietly(file.getFileObject());
        } catch (Exception e1) {
          final String msg = String.format("Can't open file %s: %s", file.getFileName().getFriendlyURI(), e1.getMessage());
          LOGGER.warn(msg);
          loadingBeans.forEach(li -> Utils.closeQuietly(li.loadingInfo.getFileObject()));
          showMainPanel();
          e1.printStackTrace();
          throw e1;
        }
      }
      loadingBeans.forEach(l -> {
        final LogLoadingSession session = logLoader.startLoading(
            new VfsSource(
                l.loadingInfo.getFileObject(),
                l.fileObjectToImport.getOpenMode().equals(OpenMode.FROM_END) ? l.loadingInfo.getLastFileSize() : 0
            ),
            l.fileObjectToImport.getPossibleLogImporters().getLogImporter().orElse(detectLaterImporter),
            logDataCollector,
            3000,
            Optional.of(2000L));
        logLoader.changeFilters(session, new LevelHigherOrEqualAcceptCondition(l.fileObjectToImport.getLevel()));
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
  }
}

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
    columnClasses.put(COLUMN_OPEN_MODE, AdvanceOpenPanel.OpenMode.class);
    columnClasses.put(COLUMN_CAN_PARSE, AdvanceOpenPanel.CanParse.class);
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
    fireTableRowsInserted(data.size() - 1, data.size());
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

  FileObjectToImport getFileObjectToImport(int rowIndex) {
    return data.get(rowIndex);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClasses.get(columnIndex);
  }

  void setCanParse(FileObject fileObject, AdvanceOpenPanel.CanParse canParse) {
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

  void setOpenMode(FileObject fileObject, AdvanceOpenPanel.OpenMode openMode) {
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

class LoadingBean {

  LoadingBean(FileObjectToImport fileObjectToImport, LoadingInfo loadingInfo) {
    this.fileObjectToImport = fileObjectToImport;
    this.loadingInfo = loadingInfo;
  }

  FileObjectToImport fileObjectToImport;
  LoadingInfo loadingInfo;
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

  FileObjectToImport(FileObject fileObject, FileName fileName, FileSize fileSize, Level level, AdvanceOpenPanel.OpenMode openMode, AdvanceOpenPanel
      .CanParse canParse) {
    this.fileObject = fileObject;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.level = level;
    this.openMode = openMode;
    this.canParse = canParse;
  }

  PossibleLogImporters getPossibleLogImporters() {
    return possibleLogImporters;
  }

  void setPossibleLogImporters(PossibleLogImporters possibleLogImporters) {
    this.possibleLogImporters = possibleLogImporters;
  }

  public ContentProbe getContent() {
    return content;
  }

  public void setContent(ContentProbe content) {
    this.content = content;
  }

  FileObject getFileObject() {
    return fileObject;
  }

  FileName getFileName() {
    return fileName;
  }

  FileSize getFileSize() {
    return fileSize;
  }

  public Level getLevel() {
    return level;
  }

  AdvanceOpenPanel.OpenMode getOpenMode() {
    return openMode;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  void setOpenMode(AdvanceOpenPanel.OpenMode openMode) {
    this.openMode = openMode;
  }

  AdvanceOpenPanel.CanParse getCanParse() {
    return canParse;
  }

  void setCanParse(AdvanceOpenPanel.CanParse canParse) {
    this.canParse = canParse;
  }
}
