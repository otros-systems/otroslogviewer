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

import jsyntaxpane.DefaultSyntaxKit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import pl.otros.logview.gui.table.TableColumns;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporterUsingParser;
import pl.otros.logview.io.Utils;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jPatternParserEditor extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(Log4jPatternParserEditor.class.getName());

  private JButton loadLog;
  private JButton testParser;
  private JButton saveParser;

  private JTextArea logFileContent;
  private JEditorPane propertyEditor;
  private LogViewPanel logViewPanel;
  private JOtrosVfsBrowserDialog otrosVfsBrowserDialog;
  private Font heading1Font;
  private Font heading2Font;

  private JLabel logFileContentLabel;
	private final OtrosApplication otrosApplication;

	public Log4jPatternParserEditor(OtrosApplication otrosApplication) {
		this.otrosApplication = otrosApplication;
    createGui();
    loadDefaultPropertyEditorContent();
    createActions();
    enableDragAndDrop();
  }

  private void enableDragAndDrop() {
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
        if (isText(support) || isListOfFiles(support)) {
          return true;
        }
        return defaultTransferHandler.canImport(support);
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

  private void createGui() {
    this.setLayout(new BorderLayout());
    heading1Font = new JLabel().getFont().deriveFont(20f).deriveFont(Font.BOLD);
    heading2Font = new JLabel().getFont().deriveFont(14f).deriveFont(Font.BOLD);

    loadLog = new JButton("Load log", Icons.FOLDER_OPEN);
    testParser = new JButton("Test parser", Icons.WRENCH_ARROW);
    saveParser = new JButton("Save", Icons.DISK);
    logFileContent = new JTextArea();
    DefaultSyntaxKit.initKit();
    propertyEditor = new JEditorPane();

    logFileContent = new JTextArea();
    logViewPanel = new LogViewPanel(new LogDataTableModel(), TableColumns.ALL_WITHOUT_LOG_SOURCE,otrosApplication);
    JPanel panelEditorActions = new JPanel(new BorderLayout(5, 5));
    JToolBar actionsToolBar = new JToolBar("Actions");
    actionsToolBar.setFloatable(false);
    actionsToolBar.add(testParser);
    actionsToolBar.add(saveParser);

    JToolBar propertyEditorToolbar = new JToolBar();
    JLabel labelEditProperties = new JLabel("Edit your properties: and test parser");
    labelEditProperties.setFont(heading2Font);
    propertyEditorToolbar.add(labelEditProperties);
    panelEditorActions.add(propertyEditorToolbar, BorderLayout.NORTH);
    panelEditorActions.add(actionsToolBar, BorderLayout.SOUTH);
    panelEditorActions.add(new JScrollPane(propertyEditor));

    logFileContentLabel = new JLabel(" Load your log file, paste from clipboard or drag and drop file. ");
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
    JLabel labelParsingResult = new JLabel(" Parsing result:");
    labelParsingResult.setFont(heading1Font);
    southPanel.add(labelParsingResult, BorderLayout.NORTH);
    southPanel.add(logViewPanel);

    JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainSplit.setOneTouchExpandable(true);
    mainSplit.add(northSplit);
    mainSplit.add(southPanel);
    mainSplit.setDividerLocation(0.5f);

    add(mainSplit);

    propertyEditor.setContentType("text/properties");

  }

  private void loadDefaultPropertyEditorContent() {
    InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("log4jDefaultPatternParser.txt");
    if (resourceAsStream != null) {
      String string;
      try {
        string = IOUtils.toString(resourceAsStream);
        propertyEditor.setText(string);
        propertyEditor.setCaretPosition(0);
      } catch (IOException e) {
        LOGGER.error("Can't load default value of property editor");
      }
    }
  }

  public void createActions() {
    loadLog.addActionListener(e -> {
      try {
        selectLogFileAndLoad();
      } catch (IOException e1) {
        LOGGER.error("Error loading file " + e1.getMessage());
        LOGGER.error("Error loading file: " + e1.getMessage(),e1);
      }
    });
    testParser.addActionListener(e -> {
      try {
        testParser();
      } catch (InitializationException e1){
        LOGGER.error("Error during parser test: " + e1.getMessage(),e1);
        JOptionPane.showMessageDialog(Log4jPatternParserEditor.this,"Can't initialize Log parser: " + e1.getMessage(),"Log parser error",
            JOptionPane.ERROR_MESSAGE);
      } catch (Exception e1) {
        LOGGER.error("Error during parser test: " + e1.getMessage(),e1);
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
				LogImporterUsingParser log4jImporter = createLog4jImporter(text);
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

	private LogImporterUsingParser createLog4jImporter(String text) throws IOException, InitializationException {
		Log4jPatternMultilineLogParser log4jPatternMultilineLogParser = new Log4jPatternMultilineLogParser();
		Properties p = new Properties();
		p.load(new ByteArrayInputStream(text.getBytes()));
		LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(log4jPatternMultilineLogParser);
		logImporterUsingParser.init(p);
		return logImporterUsingParser;
	}

	public void selectLogFileAndLoad() throws IOException {
    if (otrosVfsBrowserDialog == null) {
      otrosVfsBrowserDialog = new JOtrosVfsBrowserDialog();
      otrosVfsBrowserDialog.setMultiSelectionEnabled(false);
    }
    JOtrosVfsBrowserDialog.ReturnValue returnValue = otrosVfsBrowserDialog.showOpenDialog(this, "Select file");
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
      Utils.closeQuietly(fileObject);
    }
  }

  protected void testParser() throws IOException, InitializationException {
    Properties p = new Properties();
    p.load(new StringReader(propertyEditor.getText()));
    Log4jPatternMultilineLogParser log4jParser = new Log4jPatternMultilineLogParser();
    log4jParser.init(p);
    LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(log4jParser);
    logViewPanel.getDataTableModel().clear();
    ParsingContext parsingContext = new ParsingContext();
    ByteArrayInputStream in = new ByteArrayInputStream(logFileContent.getText().getBytes());
    logImporterUsingParser.initParsingContext(parsingContext);
    logImporterUsingParser.importLogs(in, logViewPanel.getDataTableModel(), parsingContext);
    int loaded = logViewPanel.getDataTableModel().getRowCount();
    if (loaded > 0) {
      otrosApplication.getStatusObserver().updateStatus(String.format("Parsed %d events.", loaded));
    } else {
      otrosApplication.getStatusObserver().updateStatus("0 events parsed!", StatusObserver.LEVEL_WARNING);
    }
  }

	public LogViewPanel getLogViewPanel() {
		return logViewPanel;
	}
}
