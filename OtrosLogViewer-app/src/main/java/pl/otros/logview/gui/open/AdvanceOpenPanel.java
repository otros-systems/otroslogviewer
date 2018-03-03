package pl.otros.logview.gui.open;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.RenamedLevel;
import pl.otros.logview.accept.LevelHigherOrEqualAcceptCondition;
import pl.otros.logview.api.*;
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
import pl.otros.logview.api.pluginable.PluginableElement;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.StatsService;
import pl.otros.logview.gui.GuiUtils;
import pl.otros.logview.gui.PopupListener;
import pl.otros.logview.gui.renderers.ContentProbeRenderer;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.gui.renderers.LogImporterRenderer;
import pl.otros.logview.gui.renderers.PossibleLogImportersRenderer;
import pl.otros.logview.gui.session.*;
import pl.otros.logview.gui.util.DocumentInsertUpdateHandler;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.swing.OtrosSwingUtils;
import pl.otros.swing.Progress;
import pl.otros.swing.renderer.NamedTableRenderer;
import pl.otros.swing.suggest.SuggestDecorator;
import pl.otros.swing.suggest.SuggestionSource;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.table.FileSize;
import pl.otros.vfs.browser.table.FileSizeTableCellRenderer;
import pl.otros.vfs.browser.util.VFSUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class AdvanceOpenPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdvanceOpenPanel.class.getName());
  private static final String CARD_TABLE = "table";
  private static final String CARD_EMPTY_VIEW = "emptyView";
  public static final String SESSIONS = "sessions";

  private final FileObjectToImportTableModel tableModel;
  private final AbstractAction importAction;
  private final CardLayout cardLayout;
  private final JProgressBar loadingProgressBar;

  private final AbstractAction deleteSelectedAction;
  private final AbstractAction addMoreFilesAction;
  private final AbstractAction selectedFromStart;
  private final AbstractAction selectedFromEnd;
  private final HashMap<OpenMode, Icon> openModeIcons;
  private final AbstractAction saveSession;
  private final AbstractAction loadSession;

  //TODO columns widths
  //TODO tail from last xxxx KB
  //TODO create log parser pattern if some logs can't be parsed
  public AdvanceOpenPanel(OtrosApplication otrosApplication) {
    this.setName("OpenPanel");
    openModeIcons = new HashMap<>();
    openModeIcons.put(OpenMode.FROM_START, Icons.FROM_START);
    openModeIcons.put(OpenMode.FROM_END, Icons.FROM_END);

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
        final boolean isEmpty = tableModel.getRowCount() > 0;
        importAction.setEnabled(isEmpty);
      }
    });


    JTable table = new JTable(tableModel);
    table.setAutoCreateRowSorter(true);
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

    final DefaultListCellRenderer openModeRenderer = new OpenModeListCellRenderer();
    table.setDefaultRenderer(OpenMode.class, new OpenModeTableCellRenderer(this));
    final JComboBox<OpenMode> openModeCbx = new JComboBox<>(OpenMode.values());
    openModeCbx.setRenderer(openModeRenderer);
    table.setDefaultEditor(OpenMode.class, new DefaultCellEditor(openModeCbx));

    table.setDefaultRenderer(FileSize.class, new FileSizeTableCellRenderer());
    table.setDefaultRenderer(Level.class, new LevelRenderer(LevelRenderer.Mode.IconsAndText));

    final DefaultTableCellRenderer possibleLogImporterRenderer = new PossibleLogImportersRenderer();
    table.setDefaultRenderer(PossibleLogImporters.class, possibleLogImporterRenderer);
    table.setDefaultRenderer(ContentProbe.class, new ContentProbeRenderer());

    final JComboBox<LogImporter> comboBox = new JComboBox<>();
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
    table.setDefaultEditor(Level.class, new DefaultCellEditor(new JComboBox<>(levels)));
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
        tableModel.delete(selectedRowsStream(table).toArray());
      }
    };
    deleteSelectedAction.setEnabled(false);
    selectedFromStart = new AbstractAction("Selected from start", Icons.FROM_START) {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectedRowsStream(table)
            .mapToObj(tableModel::getFileObjectToImport)
            .forEach(l -> tableModel.setOpenMode(l.getFileObject(), OpenMode.FROM_START));
      }
    };
    selectedFromStart.setEnabled(false);
    selectedFromEnd = new AbstractAction("Selected from end", Icons.FROM_END) {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectedRowsStream(table)
            .mapToObj(tableModel::getFileObjectToImport)
            .forEach(l -> tableModel.setOpenMode(l.getFileObject(), OpenMode.FROM_END));
      }
    };
    selectedFromEnd.setEnabled(false);
    saveSession = new AbstractAction("Save session", Icons.DISK) {
      @Override
      public void actionPerformed(ActionEvent e) {
        final PersistService persistService = otrosApplication.getServices().getPersistService();
        try {
          final List<Session> sessions = persistService.load(SESSIONS, Collections.emptyList(), new SessionDeserializer());
          final Map<String, Session> sessionMap = sessions.stream().collect(Collectors.toMap(Session::getName, Function.identity(), (s1, s2) -> s1));
          final List<String> sessionNames = sessionMap.keySet().stream().sorted().collect(Collectors.toList());

          final JDialog dialog = new JDialog(otrosApplication.getApplicationJFrame());
          dialog.setModal(true);
          dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
          final JPanel contentPanel = new JPanel(new MigLayout(new LC().fill().width("100%")));
          final JLabel label = new JLabel("Enter session name:");
          final JXTextField textField = new JXTextField("session name");
          textField.setColumns(40);
          label.setDisplayedMnemonic('n');
          label.setLabelFor(textField);
          final JLabel overwriteLabel = new JLabel(" ");
          textField.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
            @Override
            protected void documentChanged(DocumentEvent e) {
              final boolean present = sessionNames.stream().anyMatch(s -> s.equals(textField.getText()));
              if (present) {
                overwriteLabel.setText("You will overwrite session");
                overwriteLabel.setIcon(Icons.LEVEL_WARNING);
              } else {
                overwriteLabel.setText(" ");
                overwriteLabel.setIcon(null);
              }
            }
          });

          final SuggestionSource<String> suggestionSource = query -> sessionNames.stream().filter(s -> s.contains(query.getValue())).collect(Collectors.toList());
          SuggestDecorator.decorate(textField,
              suggestionSource,
              JLabel::new,
              value -> textField.setText(value.getValue()),
              true);



          final AbstractAction saveAction = new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
              String sessionName = textField.getText();
              final List<FileObjectToImport> data = tableModel.getData();
              List<FileToOpen> files = data.stream()
                      .map(f ->
                              new FileToOpen(f.getFileName().getURI(),
                                      f.getOpenMode(), f.getLevel(),
                                      f.getPossibleLogImporters().getLogImporter().map(PluginableElement::getPluginableId)))
                      .collect(Collectors.toList());
              final Session session = new Session(sessionName, files);
              dialog.setVisible(false);
              dialog.dispose();
              otrosApplication.getStatusObserver().updateStatus("Session saved as " + sessionName, StatusObserver.LEVEL_NORMAL);
              try {
                ArrayList<Session> toSave = sessions.stream().filter(s -> !s.getName().equals(sessionName)).collect(Collectors.toCollection(ArrayList::new));
                toSave.add(session);
                persistService.persist("sessions", toSave, new SessionSerializer());
              } catch (Exception e1) {
                e1.printStackTrace();
              }
            }

          };

          textField.addActionListener(saveAction);
          final JButton saveButton = new JButton(saveAction);
          saveButton.addActionListener(saveAction);

          final AbstractAction cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
              dialog.setVisible(false);
            }
          };
          final JButton cancelButton = new JButton(cancelAction);

          final InputMap inputMap = contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
          inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
          contentPanel.getActionMap().put("close", cancelAction);

          contentPanel.add(label, "wrap, span, width 250:250:250");
          contentPanel.add(textField, "wrap, growx, span");
          contentPanel.add(overwriteLabel, "wrap, growx, span");
          contentPanel.add(saveButton, "center, pushx");
          contentPanel.add(cancelButton, "center, pushx, wrap");
          dialog.setContentPane(contentPanel);
          dialog.pack();
          GuiUtils.centerOnScreen(dialog);
          dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
          dialog.setVisible(true);
        } catch (Exception e1) {
          LOGGER.error("Can't load sessions from persist service", e1);
          JOptionPane.showMessageDialog(
              AdvanceOpenPanel.this,
              "Can't open saved sessions: " + e1.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    loadSession = new AbstractAction("Load session", Icons.FOLDER_OPEN) {
      @Override
      public void actionPerformed(ActionEvent e) {
        final PersistService persistService = otrosApplication.getServices().getPersistService();
        final List<Session> sessions = persistService.load("sessions", Collections.emptyList(), new SessionDeserializer());
        if (sessions.isEmpty()) {
          JOptionPane.showMessageDialog(AdvanceOpenPanel.this, "There is no saved session");
          return;
        }
        final Map<String, Session> sessionMap = sessions.stream().collect(Collectors.toMap(Session::getName, Function.identity(), (s1, s2) -> s1));
        final List<String> sessionNames = sessionMap.keySet().stream().sorted().collect(Collectors.toList());
        JComboBox<String> sessionCbx = new JComboBox<>(new Vector<>(sessionNames));
        final int biggestSession = sessionMap
            .values()
            .stream()
            .map(SessionUtil::toStringGroupedByServer)
            .mapToInt(s->s.split("\n").length)
            .max().orElse(1);
        final int longestName = sessionMap
            .values()
            .stream()
            .flatMap(s -> s.getFilesToOpen().stream())
            .mapToInt(f -> f.getUri().length())
            .max()
            .orElse(20);
        final JTextArea jTextArea = new JTextArea(biggestSession + 1, longestName);
        jTextArea.setEditable(false);
        jTextArea.setBorder(BorderFactory.createTitledBorder("Files in session"));

        sessionCbx.addActionListener((ActionEvent e13) -> {
          final String sessionName = (String) sessionCbx.getSelectedItem();
          final Session session = sessionMap.get(sessionName);
          String text = SessionUtil.toStringGroupedByServer(session);
          jTextArea.setText(text);
        });
        sessionCbx.setSelectedIndex(0);
        final JLabel selectSessionLabel = new JLabel("Select session name:");
        selectSessionLabel.setDisplayedMnemonic('S');
        selectSessionLabel.setLabelFor(sessionCbx);

        JDialog dialog = new JDialog(otrosApplication.getApplicationJFrame(), true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        final JPanel contentPane = new JPanel(new MigLayout());

        contentPane.add(selectSessionLabel);
        contentPane.add(new JScrollPane(jTextArea), "wrap, span 1 2");
        contentPane.add(sessionCbx,"wrap, aligny top");
        Action loadAction = new AbstractAction("Load") {
          @Override
          public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
            dialog.dispose();
            showLoadingPanel();
            String chosenSession = (String) sessionCbx.getSelectedItem();
            tableModel.clear();
            new SwingWorker<SessionLoadResult, Progress>() {
              @Override
              protected SessionLoadResult doInBackground() {
                List<FileToOpen> failed = new ArrayList<>();
                List<FileObjectToImport> successFullyLoaded = new ArrayList<>();
                for (FileToOpen f : sessionMap.get(chosenSession).getFilesToOpen()) {
                  try {
                    final FileObject fileObject = VFSUtils.resolveFileObject(f.getUri());
                    final FileName name = fileObject.getName();
                    final FileSize fileSize = new FileSize(fileObject.getContent().getSize());
                    final FileObjectToImport toImport = new FileObjectToImport(fileObject, name, fileSize, f.getLevel(), f.getOpenMode(), CanParse.NOT_TESTED);
                    successFullyLoaded.add(toImport);
                  } catch (FileSystemException e1) {
                    LOGGER.error("Can't load file to session: ", e1);
                    failed.add(f);
                  }
                }
                return new SessionLoadResult(chosenSession, failed, successFullyLoaded);
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
                  final SessionLoadResult sessionLoadResult = get();
                  final List<FileToOpen> fileToOpens = sessionLoadResult.getFailedToOpen();
                  if (fileToOpens.size() > 0) {
                    final String msg = fileToOpens.stream()
                        .map(FileToOpen::getUri)
                        .collect(Collectors.joining("\n", "Failed to open files:\n", ""));
                    JOptionPane.showMessageDialog(AdvanceOpenPanel.this, new JTextArea(msg), "Error", JOptionPane.ERROR_MESSAGE);
                  } else {
                    otrosApplication.getStatusObserver().updateStatus("Session \"" + sessionLoadResult.getName() + "\" loaded");
                  }
                  final List<FileObjectToImport> successfullyOpened = sessionLoadResult.getSuccessfullyOpened();
                  tableModel.add(successfullyOpened);
                } catch (InterruptedException | ExecutionException e1) {
                  e1.printStackTrace();
                } finally {
                  showMainPanel();
                }
              }
            }.execute();
          }
        };
        final AbstractAction cancelLoadAction = new AbstractAction("Cancel") {
          @Override
          public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
            dialog.dispose();
          }
        };

        final JButton loadButton = new JButton(loadAction);
        loadButton.setMnemonic('L');
        loadButton.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"open");

        final JButton cancelButton = new JButton(cancelLoadAction);
        cancelButton.setMnemonic('C');

        contentPane.getActionMap().put("close", cancelLoadAction);
        contentPane.getActionMap().put("open", loadAction);
        final InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");

        contentPane.add(loadButton);
        contentPane.add(cancelButton);
        dialog.setContentPane(contentPane);
        dialog.pack();
        GuiUtils.centerOnScreen(dialog);
        dialog.setVisible(true);
        comboBox.requestFocusInWindow();


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
    table.getSelectionModel().addListSelectionListener((e) -> selectedFromStart.setEnabled(table.getSelectedRowCount() > 0));
    table.getSelectionModel().addListSelectionListener((e) -> selectedFromEnd.setEnabled(table.getSelectedRowCount() > 0));
    initContextMenu(table);
    initToolbar(jToolBar);

    importAction = new AbstractAction("Merge", Icons.ARROW_JOIN_24) {

      @Override
      public void actionPerformed(ActionEvent e) {
        showLoadingPanel();
        final int rowCount = tableModel.getRowCount();
        final List<Optional<LogImporter>> logImporters = tableModel.getData()
            .stream()
            .map(l -> l.getPossibleLogImporters().getLogImporter())
            .collect(toList());
        
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
    importButton.setName("OpenPanel.import");
    importButton.setMnemonic('I');
    OtrosSwingUtils.fontSize2(importButton);
    mainPanel.add(importButton, BorderLayout.SOUTH);
    tableModel.addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        final int firstRow = e.getFirstRow();
        final int lastRow = e.getLastRow();
        for (int i = firstRow; i <= lastRow; i++) {
          final FileObjectToImport fileObjectAt = tableModel.getFileObjectToImport(i);
          LOGGER.info("Added " + fileObjectAt + " to table");

          final SwingWorker<Void, AddingDetail> swingWorker = new SwingWorker<Void, AddingDetail>() {

            @Override
            protected void process(List<AddingDetail> chunks) {
              chunks.forEach(c -> {
                final FileObject fileObject = fileObjectAt.getFileObject();
                tableModel.setCanParse(fileObject, c.getCanParse());
                tableModel.setContent(fileObject, c.getContentProbe());
                tableModel.setPossibleLogImporters(fileObject, c.getPossibleLogImporters());
              });
            }

            @Override
            protected Void doInBackground() {
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

    JPanel emptyView = new JPanel(new MigLayout("fillx", "[center]", "[center]10[center]"));
    emptyView.add(new JLabel(""),"wrap, pushy");
    emptyView.add(OtrosSwingUtils.fontSize2(new JButton(addMoreFilesAction)),"wrap");
    emptyView.add(OtrosSwingUtils.fontSize2(new JButton(loadSession)),"wrap");
    emptyView.add(new JLabel(""),"wrap, pushy");
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

  private IntStream selectedRowsStream(JTable table) {
    return Arrays.stream(table.getSelectedRows()).map(table::convertRowIndexToModel);
  }

  private void initToolbar(JToolBar toolBar) {
    final JButton addButton = new JButton(addMoreFilesAction);
    addButton.setMnemonic('A');
    addButton.setName("OpenPanel.add more files");
    toolBar.add(addButton);
    final JButton deleteButton = new JButton(deleteSelectedAction);
    deleteButton.setMnemonic('D');
    toolBar.add(deleteButton);
    final JButton fromStartButton = new JButton(selectedFromStart);
    fromStartButton.setMnemonic('t');
    toolBar.add(fromStartButton);
    final JButton fromEndButton = new JButton(selectedFromEnd);
    fromEndButton.setMnemonic('E');
    toolBar.add(fromEndButton);
    final JButton saveSession = new JButton(this.saveSession);
    saveSession.setMnemonic('S');
    toolBar.add(saveSession);
    final JButton loadSession = new JButton(this.loadSession);
    loadSession.setMnemonic('L');
    toolBar.add(loadSession);
  }

  private void initContextMenu(JTable table) {
    JPopupMenu popupMenu = new JPopupMenu("Options");
    popupMenu.add(addMoreFilesAction);
    popupMenu.add(deleteSelectedAction);
    popupMenu.add(selectedFromStart);
    popupMenu.add(selectedFromEnd);
    table.addMouseListener(new PopupListener(popupMenu));
  }

  private void showLoadingPanel() {
    cardLayout.show(AdvanceOpenPanel.this, "loadingPanel");
  }

  private void showMainPanel() {
    cardLayout.show(AdvanceOpenPanel.this, "mainPanel");
  }

  private TableColumns[] mergeColumns(List<Optional<LogImporter>> logImporters) {
    return logImporters.stream()
      .filter(Optional::isPresent)
      .map(Optional::get)
      .flatMap(l -> Arrays.stream(l.getTableColumnsToUse()))
      .distinct()
      .toArray(TableColumns[]::new);
  }


  private class AddFilesSwingWorker extends SwingWorker<Void, FileObjectToImport> {

    private final FileObject[] selectedFiles;

    AddFilesSwingWorker(FileObject[] selectedFiles) {
      this.selectedFiles = selectedFiles;
    }

    @Override
    protected Void doInBackground() {
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
              OpenMode.FROM_START,
              CanParse.NOT_TESTED);
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
      final StatsService statsService = otrosApplication.getServices().getStatsService();

      LogDataCollector logDataCollector = logViewPanelWrapper.getDataTableModel();
      LogImporter detectLaterImporter = new DetectOnTheFlyLogImporter(otrosApplication.getAllPluginables().getLogImportersContainer().getElements());
      try {
        detectLaterImporter.init(new Properties());
      } catch (InitializationException e1) {
        LOGGER.error("Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage());
        showMainPanel();
        throw e1;
      }
      final List<FileObjectToImport> fileObjects = IntStream.range(0, tableModel.getRowCount()).mapToObj(tableModel::getFileObjectToImport).collect(
          toList());
      ArrayList<LoadingBean> loadingBeans = new ArrayList<>();
      int progress = 0;
      for (final FileObjectToImport file : fileObjects) {
        try {
          progress++;
          if (file.getCanParse() != CanParse.FILE_TOO_SMALL && file.getCanParse() != CanParse.YES) {
            continue;
          }
          publish(new Progress(progress, fileObjects.size(), "Processing " + file.getFileName().getBaseName()));
          final LoadingInfo e1 = new LoadingInfo(file.getFileObject(), true);
          loadingBeans.add(new LoadingBean(file, e1));
          Utils.closeQuietly(file.getFileObject());
        } catch (Exception e1) {
          final String msg = String.format("Can't open file %s: %s", file.getFileName().getFriendlyURI(), e1.getMessage());
          LOGGER.warn(msg);
          loadingBeans.forEach(li -> li.loadingInfo.close());
          showMainPanel();
          e1.printStackTrace();
          throw e1;
        }
      }
      loadingBeans.forEach(l -> {
        final LogLoadingSession session = logLoader.startLoading(
            new VfsSource(
                l.loadingInfo.getFileObject(),
                l.fileObjectToImport.getOpenMode()
            ),
            l.fileObjectToImport.getPossibleLogImporters().getLogImporter().orElse(detectLaterImporter),
            logDataCollector,
            3000,
            Optional.of(2000L));
        logLoader.changeFilters(session, new LevelHigherOrEqualAcceptCondition(l.fileObjectToImport.getLevel()));
      });
      statsService.filesImportedIntoOneView(loadingBeans.size());
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

  private class OpenModeListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      final Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      JLabel l = (JLabel) listCellRendererComponent;
      l.setIcon(openModeIcons.get(value));
      return listCellRendererComponent;
    }
  }

  class OpenModeTableCellRenderer extends DefaultTableCellRenderer {

    private AdvanceOpenPanel advanceOpenPanel;

    public OpenModeTableCellRenderer(AdvanceOpenPanel advanceOpenPanel) {
      this.advanceOpenPanel = advanceOpenPanel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final Component listCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      JLabel l = (JLabel) listCellRendererComponent;
      l.setIcon(advanceOpenPanel.openModeIcons.get(value));
      return listCellRendererComponent;
    }
  }

}
