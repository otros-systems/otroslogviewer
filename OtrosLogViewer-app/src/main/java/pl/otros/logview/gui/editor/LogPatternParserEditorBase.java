package pl.otros.logview.gui.editor;

import jsyntaxpane.DefaultSyntaxKit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.LogPatternParserEditor;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.parser.LogParser;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.gui.LogViewPanel;
import pl.otros.logview.gui.editor.log4j.Log4jPatternParserEditor;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.util.List;
import java.util.Properties;

public abstract class LogPatternParserEditorBase extends JPanel implements LogPatternParserEditor {
  private static final Logger LOGGER = LoggerFactory.getLogger(Log4jPatternParserEditor.class.getName());
  protected final OtrosApplication otrosApplication;
  protected final String logPatternText;
  private JButton loadLog;
  private JButton saveParser;
  protected JTextArea logFileContent;
  protected JEditorPane propertyEditor;
  protected LogViewPanel logViewPanel;
  private JOtrosVfsBrowserDialog otrosVfsBrowserDialog;
  private JLabel logFileContentLabel;
  private JButton testParser;


  public LogPatternParserEditorBase(OtrosApplication otrosApplication, String logPatternText) {
    this.otrosApplication = otrosApplication;
    this.logPatternText = logPatternText;
    createGui();
    loadDefaultPropertyEditorContent();
    createActions();
    enableDragAndDrop();
  }

  protected void enableDragAndDrop() {
    logFileContent.setDragEnabled(true);
    final TransferHandler defaultTransferHandler = logFileContent.getTransferHandler();
    TransferHandler transferHandler = new TransferHandler() {

      @Override
      public boolean importData(TransferSupport support) {
        if (isText(support)) {
          try {
            String transferData = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            if (transferData.startsWith("file://")) {
              String firstLine = transferData;
              if (firstLine.indexOf('\n') > 0) {
                firstLine = firstLine.substring(0, firstLine.indexOf('\n') - 1);
              }
              loadLogContent(VFS.getManager().resolveFile(firstLine));
            } else {
              defaultTransferHandler.importData(support);
            }
            return true;

          } catch (UnsupportedFlavorException e) {
            LOGGER.warn("Can't import data, UnsupportedFlavorException: " + e.getMessage());
          } catch (IOException e) {
            LOGGER.warn("Can't import data, IOException: " + e.getMessage());
          }
        }

        if (isListOfFiles(support)) {
          try {
            List data = (List) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (data.size() > 0) {
              File file = (File) data.get(0);
              loadLogContent(VFS.getManager().resolveFile(file.getAbsolutePath()));
              return true;
            }
          } catch (UnsupportedFlavorException e) {
            LOGGER.warn("Can't import data, UnsupportedFlavorException: " + e.getMessage());
          } catch (IOException e) {
            LOGGER.warn("Can't import data, IOException: " + e.getMessage());
          }
        }

        return defaultTransferHandler.importData(support);
      }

      @Override
      public boolean canImport(TransferSupport support) {
        return isText(support) || isListOfFiles(support) || defaultTransferHandler.canImport(support);
      }

      private boolean isListOfFiles(TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
      }

      private boolean isText(TransferSupport support) {
        return DataFlavor.selectBestTextFlavor(support.getDataFlavors()) != null;
      }

    };
    logFileContent.setTransferHandler(transferHandler);
    loadLog.setTransferHandler(transferHandler);
    logFileContentLabel.setTransferHandler(transferHandler);
  }

  protected void createGui() {
    this.setLayout(new BorderLayout());
    Font heading1Font = new JLabel().getFont().deriveFont(20f).deriveFont(Font.BOLD);
    Font heading2Font = new JLabel().getFont().deriveFont(14f).deriveFont(Font.BOLD);

    loadLog = new JButton("Load log", Icons.FOLDER_OPEN);
    testParser = new JButton("Test parser", Icons.WRENCH_ARROW);
    saveParser = new JButton("Save", Icons.DISK);
    logFileContent = new JTextArea();
    DefaultSyntaxKit.initKit();
    propertyEditor = new JEditorPane();

    logFileContent = new JTextArea();
    logViewPanel = new LogViewPanel(new LogDataTableModel(), TableColumns.ALL_WITHOUT_LOG_SOURCE, otrosApplication);
    JPanel panelEditorActions = new JPanel(new BorderLayout(5, 5));
    JToolBar actionsToolBar = new JToolBar("Actions");
    actionsToolBar.setFloatable(false);
    actionsToolBar.add(testParser);
    actionsToolBar.add(saveParser);

    JToolBar propertyEditorToolbar = new JToolBar();
    JLabel labelEditProperties = new JLabel("2. Edit your properties: and test parser");
    labelEditProperties.setFont(heading2Font);
    propertyEditorToolbar.add(labelEditProperties);
    panelEditorActions.add(propertyEditorToolbar, BorderLayout.NORTH);
    panelEditorActions.add(actionsToolBar, BorderLayout.SOUTH);
    panelEditorActions.add(new JScrollPane(propertyEditor));

    logFileContentLabel = new JLabel("1.  Load your log file, paste from clipboard or drag and drop file. ");
    JToolBar loadToolbar = new JToolBar();
    loadToolbar.add(logFileContentLabel);
    loadToolbar.add(loadLog);
    logFileContentLabel.setFont(heading2Font);
    JPanel logContentPanel = new JPanel(new BorderLayout(5, 5));
    logContentPanel.add(new JScrollPane(logFileContent));
    logContentPanel.add(loadToolbar, BorderLayout.NORTH);

    JSplitPane northSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    northSplit.setOneTouchExpandable(true);
    northSplit.add(logContentPanel);
    northSplit.add(panelEditorActions);

    JPanel southPanel = new JPanel(new BorderLayout(5, 5));
    JLabel labelParsingResult = new JLabel("3. Parsing result:");
    labelParsingResult.setFont(heading1Font);
    southPanel.add(labelParsingResult, BorderLayout.NORTH);
    southPanel.add(logViewPanel);

    final JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainSplit.setOneTouchExpandable(true);
    mainSplit.add(northSplit);
    mainSplit.add(southPanel);

    addHierarchyListener(e -> mainSplit.setDividerLocation(0.5));
    add(mainSplit);

    propertyEditor.setContentType("text/properties");

  }

  protected void loadDefaultPropertyEditorContent() {

    propertyEditor.setText(logPatternText);
    propertyEditor.setCaretPosition(0);
  }

  public void createActions() {
    loadLog.addActionListener(e -> {
      try {
        selectLogFileAndLoad();
      } catch (IOException e1) {
        LOGGER.error("Error loading file " + e1.getMessage());
        LOGGER.error("Error loading file: " + e1.getMessage(), e1);
      }
    });
    testParser.addActionListener(e -> {
      try {
        testParser();
      } catch (InitializationException e1) {
        LOGGER.error("Error during parser test: " + e1.getMessage(), e1);
        JOptionPane.showMessageDialog(LogPatternParserEditorBase.this, "Can't initialize Log parser: " + e1.getMessage(), "Log parser error",
          JOptionPane.ERROR_MESSAGE);
      } catch (Exception e1) {
        LOGGER.error("Error during parser test: " + e1.getMessage(), e1);
      }
    });

    saveParser.addActionListener(e -> saveParser());
  }

  protected void saveParser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(AllPluginables.USER_LOG_IMPORTERS);
    chooser.addChoosableFileFilter(new FileFilter() {

      @Override
      public String getDescription() {
        return "*.pattern files";
      }

      @Override
      public boolean accept(File f) {
        return f.getName().endsWith(".pattern") || f.isDirectory();
      }
    });
    int showSaveDialog = chooser.showSaveDialog(this);
    if (showSaveDialog == JFileChooser.APPROVE_OPTION) {
      File selectedFile = chooser.getSelectedFile();
      if (!selectedFile.getName().endsWith(".pattern")) {
        selectedFile = new File(selectedFile.getAbsolutePath() + ".pattern");
      }
      if (selectedFile.exists()
        && JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, "Do you want to overwrite file " + selectedFile.getName() + "?", "Save parser",
        JOptionPane.YES_NO_OPTION)) {
        return;
      }
      String text = propertyEditor.getText();
      FileOutputStream output = null;
      try {
        output = new FileOutputStream(selectedFile);
        IOUtils.write(text, output);
        LogImporterUsingParser log4jImporter = craeteLogImporter(text);
        otrosApplication.getAllPluginables().getLogImportersContainer().addElement(log4jImporter);
      } catch (Exception e) {
        LOGGER.error("Can't save parser: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Can't save parser: " + e.getMessage(), "Error saving parser", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      } finally {
        IOUtils.closeQuietly(output);
      }
    }
  }

  private LogImporterUsingParser craeteLogImporter(String text) throws IOException, InitializationException {
    Properties p = new Properties();
    p.load(new ByteArrayInputStream(text.getBytes()));
    LogParser parser = createLogParser(p);
    LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(parser);
    logImporterUsingParser.init(p);
    return logImporterUsingParser;
  }

  public void selectLogFileAndLoad() throws IOException {
    if (otrosVfsBrowserDialog == null) {
      otrosVfsBrowserDialog = new JOtrosVfsBrowserDialog();
      otrosVfsBrowserDialog.setMultiSelectionEnabled(false);
    }
    JOtrosVfsBrowserDialog.ReturnValue returnValue = otrosVfsBrowserDialog.showOpenDialog(this, "Select file");
    Utils.closeQuietly(otrosVfsBrowserDialog.getSelectedFile());
    if (returnValue.equals(JOtrosVfsBrowserDialog.ReturnValue.Approve)) {
      loadLogContent(otrosVfsBrowserDialog.getSelectedFile());
    }
  }

  protected void loadLogContent(FileObject fileObject) throws IOException {
    try {
      byte[] loadProbe = Utils.loadProbe(fileObject.getContent().getInputStream(), 50 * 1024);
      logFileContent.setText(new String(loadProbe));
      logFileContent.setCaretPosition(0);
    } finally {
      fileObject.getContent().close();
      Utils.closeQuietly(fileObject);
    }
  }

  public LogViewPanel getLogViewPanel() {
    return logViewPanel;
  }

  protected void testParser() throws IOException, InitializationException {
    Properties p = new Properties();
    p.load(new StringReader(propertyEditor.getText()));
    LogParser jsonLogParser = createLogParser(p);
    jsonLogParser.init(p);
    LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(jsonLogParser);
    logViewPanel.getDataTableModel().clear();
    ParsingContext parsingContext = new ParsingContext();
    ByteArrayInputStream in = new ByteArrayInputStream(logFileContent.getText().getBytes());
    logImporterUsingParser.initParsingContext(parsingContext);
    logImporterUsingParser.importLogs(in, logViewPanel.getDataTableModel(), parsingContext);
    IOUtils.closeQuietly(in);
    int loaded = logViewPanel.getDataTableModel().getRowCount();
    if (loaded > 0) {
      otrosApplication.getStatusObserver().updateStatus(String.format("Parsed %d events.", loaded));
    } else {
      otrosApplication.getStatusObserver().updateStatus("0 events parsed!", StatusObserver.LEVEL_WARNING);
    }
  }

  protected abstract LogParser createLogParser(Properties p) throws InitializationException;

  public void setLogPattern(String logPattern) {
    propertyEditor.setText(logPattern);
  }

}
