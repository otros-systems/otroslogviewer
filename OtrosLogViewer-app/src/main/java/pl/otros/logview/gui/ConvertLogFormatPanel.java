package pl.otros.logview.gui;

import com.google.common.io.Files;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.LayoutEncoderConverter;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.logppattern.LogbackLayoutEncoderConverter;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.util.LoggerConfigUtil;
import pl.otros.swing.OtrosSwingUtils;
import pl.otros.swing.TableColumnModelListenerAdapter;
import pl.otros.swing.functional.MultilineStringTableCellRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConvertLogFormatPanel extends JPanel {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConvertLogFormatPanel.class.getName());

  private OtrosApplication otrosApplication;
  private static final String PANEL_SELECT = "panel select";
  private static final String PANEL_PASTE = "panel paste";
  private static final String PANEL_APPROVE = "panel approve";
  private String backFromApproveTo = PANEL_SELECT;
  private CardLayout cardLayout;
  private final LogPatternsTableModel logPatternsTableModel = new LogPatternsTableModel(Collections.emptyList());
  private final LayoutEncoderConverter logbackLayoutEncoderConverter = new LogbackLayoutEncoderConverter();
  private AbstractAction addLoggers;
  private JXTextArea pasteTextArea;
  private final JTable jTable;

  public ConvertLogFormatPanel(OtrosApplication otrosApplication, Function<JComponent, Void> closeFunction) {
    this.otrosApplication = otrosApplication;
    final JFileChooser jFileChooser = new JFileChooser();
    jFileChooser.setFileFilter(new XmlPropertiesFilter());
    final JXButton fromFile = new JXButton("Import from logback.xml/log4j.xml/log4j.properties file", Icons.DOCUMENT_CODE);
    fromFile.setName("ConvertLogFormatPanel.fromFile");
    OtrosSwingUtils.fontSize2(fromFile);
    fromFile.addActionListener(e -> {
      backFromApproveTo = PANEL_SELECT;
      final int i = jFileChooser.showOpenDialog(ConvertLogFormatPanel.this);
      if (i == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = jFileChooser.getSelectedFile();
        try {
          String content = Files.readLines(selectedFile, Charset.forName("UTF-8")).stream().collect(Collectors.joining("\n"));
          processLoggerConfig(content);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    final JXButton pasteButton = new JXButton("Paste logger configuration", Icons.CLIPBOARD_PASTE);
    pasteButton.addActionListener(e -> {
      try {
        backFromApproveTo = PANEL_PASTE;
        String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        pasteTextArea.setText(data);
      } catch (UnsupportedFlavorException | IOException e1) {
        LOGGER.info("Can't paste logger configuration" + e1.getMessage());
      }
      cardLayout.show(ConvertLogFormatPanel.this, PANEL_PASTE);
    });
    pasteButton.setName("ConvertLogFormatPanel.paste");
    OtrosSwingUtils.fontSize2(pasteButton);
    final JPanel panelSelect = new JPanel(new MigLayout("fillx", "[center]", "[center]10[center]"));
    panelSelect.add(new JLabel("."), "wrap, pushy");
    panelSelect.add(fromFile, "wrap");
    panelSelect.add(pasteButton, "wrap");
    panelSelect.add(OtrosSwingUtils.fontSize2(new JLabel("You can drag and drop logger configuration files here.")), "wrap");
    panelSelect.add(new JLabel(""), "wrap, pushy");

    JPanel panelApprove = new JPanel(new BorderLayout());
    panelApprove.add(new JLabel("Parsing result:"), BorderLayout.NORTH);
    jTable = new JTable(logPatternsTableModel);
    jTable.setDefaultRenderer(Properties.class, new MultilineStringTableCellRenderer<Properties>(
      properties -> properties
        .keySet()
        .stream()
        .map(Object::toString)
        .sorted()
        .map(key -> key + "=" + properties.getProperty(key))
        .collect(Collectors.joining("\n"))
      , true));
    jTable.setDefaultRenderer(String.class, new MultilineStringTableCellRenderer<String>(s -> s, true));
    jTable.setDefaultRenderer(LogPatternStatus.class, new MultilineStringTableCellRenderer<>(LogPatternStatus::toString, true));
    logPatternsTableModel.addTableModelListener(e -> updateRowHeights());
    jTable.getColumnModel().addColumnModelListener(new TableColumnModelListenerAdapter() {
      @Override
      public void columnMarginChanged(ChangeEvent e) {
        updateRowHeights();
      }
    });
    jTable.setIntercellSpacing(new Dimension(10, 10));
    panelApprove.add(new JScrollPane(jTable));
    addLoggers = new AbstractAction("Add logger definition", Icons.PLUS_24) {

      @Override
      public void actionPerformed(ActionEvent e) {
        logPatternsTableModel.data
          .stream()
          .filter(x -> x.getStatus() instanceof WillImport)
          .forEach(p -> addLog4jParser(otrosApplication, p.getPattern(), p.getProperties()));
        closeFunction.apply(ConvertLogFormatPanel.this);
        otrosApplication.getStatusObserver().updateStatus("Log patterns imported");
      }
    };

    final JXButton addButton = new JXButton(addLoggers);
    addButton.setMnemonic('a');
    addButton.setName("ConvertLogFormatPanel.addButton");
    OtrosSwingUtils.fontSize2(addButton);

    final JButton approveBackButton = new JButton(Icons.ARROW_LEFT_24);
    approveBackButton.addActionListener(e -> backFromApprove());

    panelApprove.add(addButton, BorderLayout.SOUTH);
    panelApprove.add(approveBackButton, BorderLayout.WEST);

    final JPanel panelPaste = new JPanel(new BorderLayout());
    pasteTextArea = new JXTextArea("Paste logback, log4j configuration file or just layout pattern");
    panelPaste.add(pasteTextArea);
    final JXButton processPastedButton = new JXButton("Process");
    OtrosSwingUtils.fontSize2(processPastedButton);
    processPastedButton.addActionListener(e -> processLoggerConfig(pasteTextArea.getText()));
    panelPaste.add(processPastedButton, BorderLayout.SOUTH);
    final JButton pasteBackButton = new JButton(Icons.ARROW_LEFT_24);
    pasteBackButton.addActionListener(e -> cardLayout.show(ConvertLogFormatPanel.this, PANEL_SELECT));
    panelPaste.add(pasteBackButton, BorderLayout.WEST);

    cardLayout = new CardLayout();
    this.setLayout(cardLayout);
    this.add(panelSelect, PANEL_SELECT);
    this.add(panelApprove, PANEL_APPROVE);
    this.add(panelPaste, PANEL_PASTE);
    cardLayout.show(this, PANEL_SELECT);

    final TransferHandler newHandler = new DragAndDropTransferHandler();
    this.setTransferHandler(newHandler);

  }

  private void addLog4jParser(OtrosApplication otrosApplication, String name, Properties properties) {
    final Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    final LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(logParser);
    try (OutputStream out = new FileOutputStream(new File(AllPluginables.USER_LOG_IMPORTERS, UUID.randomUUID().toString() + ".pattern"))) {
      properties.setProperty("name", name);
      logImporterUsingParser.init(properties);
      otrosApplication.getAllPluginables().getLogImportersContainer().addElement(logImporterUsingParser);
      properties.store(out, "Imported log pattern");
    } catch (InitializationException | IOException e1) {
      //Ignore it
      LOGGER.error("Problem with saving Log4jPatternMultilineLogParser configuration: ", e1);
    }
  }

  private void updateRowHeights() {
    for (int row = 0; row < jTable.getRowCount(); row++) {
      int rowHeight = jTable.getRowHeight();
      for (int column = 0; column < jTable.getColumnCount(); column++) {
        Component comp = jTable.prepareRenderer(jTable.getCellRenderer(row, column), row, column);
        comp.setSize(jTable.getColumnModel().getColumn(column).getWidth(), comp.getPreferredSize().height);
        rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
      }
      jTable.setRowHeight(row, rowHeight + jTable.getRowMargin());
    }
  }

  private void backFromApprove() {
    cardLayout.show(this, backFromApproveTo);
  }

  private void processLoggerConfig(String content) {
    final Set<String> layoutPatterns = LoggerConfigUtil.extractLayoutPatterns(content);
    final List<LogPatternsTableModelEntry> newData = layoutPatterns
      .stream()
      .map(pattern -> {
        try {
          final Properties properties = logbackLayoutEncoderConverter.convert(pattern);
          final Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
          logParser.init(properties);
          logParser.initParsingContext(new ParsingContext());
          return new LogPatternsTableModelEntry(pattern, properties, checkIfAlreadyExist(properties) ? new Duplicated() : new WillImport());
        } catch (Exception exception) {
          return new LogPatternsTableModelEntry(pattern, new Properties(), new Error(exception.getMessage()));
        }
      }).collect(Collectors.toList());
    logPatternsTableModel.setData(newData);
    addLoggers.setEnabled(newData.stream().anyMatch(p -> p.getStatus() instanceof WillImport));
    cardLayout.show(ConvertLogFormatPanel.this, PANEL_APPROVE);
    updateRowHeights();
  }

  private boolean checkIfAlreadyExist(Properties candidate) {
    return otrosApplication
      .getAllPluginables()
      .getLogImportersContainer()
      .getElements()
      .stream()
      .filter(logImporter -> logImporter instanceof LogImporterUsingParser)
      .map(LogImporterUsingParser.class::cast)
      .map(LogImporterUsingParser::getParser)
      .filter(logImporter -> logImporter instanceof Log4jPatternMultilineLogParser)
      .map(Log4jPatternMultilineLogParser.class::cast)
      .map(Log4jPatternMultilineLogParser::getProperties)
      .anyMatch(existing -> {
          final boolean pattern = existing.getProperty("pattern").equals(candidate.getProperty("pattern"));
          final boolean type = existing.getProperty("type").equals(candidate.getProperty("type"));
          final boolean dateFormat = existing.getProperty("dateFormat").equals(candidate.getProperty("dateFormat"));
          return pattern && type && dateFormat;
        }
      );
  }


  private static class XmlPropertiesFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
      return f.isDirectory() || f.getName().matches(".*(xml|properties)");
    }

    @Override
    public String getDescription() {
      return "XML or properties files";
    }
  }

  private static class LogPatternsTableModel extends AbstractTableModel {

    private List<LogPatternsTableModelEntry> data;
    private String[] columnNames = new String[]{"Pattern used in logger", "Parser configuration", "Status"};

    public void setData(List<LogPatternsTableModelEntry> data) {
      this.data = data;
      fireTableDataChanged();
    }

    private LogPatternsTableModel(List<LogPatternsTableModelEntry> data) {
      this.data = data;
    }

    @Override
    public int getRowCount() {
      return data.size();
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      final LogPatternsTableModelEntry d = data.get(rowIndex);
      if (columnIndex == 0) {
        return d.getPattern();
      } else if (columnIndex == 1) {
        return d.getProperties();
      } else {
        return d.getStatus();
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 0) {
        return String.class;
      } else if (columnIndex == 1) {
        return Properties.class;
      } else {
        return LogPatternStatus.class;
      }
    }
  }

  private abstract class LogPatternStatus {
    protected String string = "";

    @Override
    public String toString() {
      return string;
    }
  }

  private class WillImport extends LogPatternStatus {
    WillImport() {
      string = "Ok";
    }
  }


  private class Duplicated extends LogPatternStatus {
    Duplicated() {
      string = "Already defined";
    }
  }

  private class Error extends LogPatternStatus {
    private Error(String error) {
      this.string = error;
    }
  }

  private static class LogPatternsTableModelEntry {
    private final String pattern;
    private final Properties properties;
    private final LogPatternStatus status;

    LogPatternsTableModelEntry(String pattern, Properties properties, LogPatternStatus status) {
      this.pattern = pattern;
      this.properties = properties;
      this.status = status;
    }


    public String getPattern() {
      return pattern;
    }

    public Properties getProperties() {
      return properties;
    }

    public LogPatternStatus getStatus() {
      return status;
    }

  }

  private class DragAndDropTransferHandler extends TransferHandler {
    @Override
    public boolean canImport(TransferSupport support) {
      return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {

      try {
        final List<File> transferData = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        final List<Optional<String>> collect = transferData
          .stream()
          .map(file -> {
            try {
              return Optional.of(Files.toString(file, Charset.forName("UTF-8")));
            } catch (IOException e) {
              return Optional.<String>empty();
            }
          }).collect(Collectors.toList());

        if (collect.stream().allMatch(Optional::isPresent)) {
          String content = collect.stream().map(o -> o.orElse("")).collect(Collectors.joining("\n"));
          processLoggerConfig(content);
          return true;
        } else {
          return false;
        }
      } catch (UnsupportedFlavorException | IOException e) {
        return false;
      }
    }
  }
}
