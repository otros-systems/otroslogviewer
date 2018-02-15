package pl.otros.logview.gui;

import com.google.common.io.Files;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXButton;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.LayoutEncoderConverter;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.logppattern.LogbackLayoutEncoderConverter;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.swing.OtrosSwingUtils;
import pl.otros.swing.functional.MultilineStringTableCellRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConvertLogFormatPanel extends JPanel {

  private OtrosApplication otrosApplication;
  private static final String PANEL_SELECT = "panel select";
  private static final String PANEL_APPROVE = "panel approve";
  private CardLayout cardLayout;
  private final LogPatternsTableModel logPatternsTableModel = new LogPatternsTableModel(Collections.emptyList());
  private final LayoutEncoderConverter logbackLayoutEncoderConverter = new LogbackLayoutEncoderConverter();
  private AbstractAction addLoggers;

  public ConvertLogFormatPanel(OtrosApplication otrosApplication, Function<JComponent,Void> closeFunction) {
    this.otrosApplication = otrosApplication;
    final JFileChooser jFileChooser = new JFileChooser();
    jFileChooser.setFileFilter(new XmlPropertiesFilter());
    final JXButton fromFile = new JXButton("Import from logback.xml/log4j.xml/log4j.properties file", Icons.DOCUMENT_CODE);
    fromFile.setName("ConvertLogFormatPanel.fromFile");
    OtrosSwingUtils.fontSize2(fromFile);
    fromFile.addActionListener(e -> {

      final int i = jFileChooser.showOpenDialog(ConvertLogFormatPanel.this);
      if (i == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = jFileChooser.getSelectedFile();
        try {
          final List<String> layoutPatterns = extractLayoutPatterns(selectedFile);
          final List<LogPatternsTableModelEntry> newData = layoutPatterns
            .stream()
            .map(pattern -> {
              try {
                final Properties properties = logbackLayoutEncoderConverter.convert(pattern);
                return new LogPatternsTableModelEntry(pattern, properties, checkIfAlreadyExist(properties) ? new Duplicated() : new WillImport());
              } catch (Exception exception) {
                return new LogPatternsTableModelEntry(pattern, new Properties(), new Error(exception.getMessage()));
              }
            }).collect(Collectors.toList());
          logPatternsTableModel.setData(newData);
          addLoggers.setEnabled(newData.stream().anyMatch(p -> p.status instanceof WillImport));
          cardLayout.show(ConvertLogFormatPanel.this, PANEL_APPROVE);
        } catch (IOException e1) {
          e1.printStackTrace();
        }

      }
    });
    final JXButton pasteButton = new JXButton("Paste logger configuration", Icons.CLIPBOARD_PASTE);
    pasteButton.setName("ConvertLogFormatPanel.paste");
    OtrosSwingUtils.fontSize2(pasteButton);
    final JPanel panelSelect = new JPanel(new MigLayout("fillx", "[center]", "[center]10[center]"));
    panelSelect.add(new JLabel(""),"wrap, pushy");
    panelSelect.add(fromFile,"wrap");
    panelSelect.add(pasteButton,"wrap");
    panelSelect.add(new JLabel(""),"wrap, pushy");

    JPanel panelApprove = new JPanel(new BorderLayout());
    panelApprove.add(new JLabel("Parsing result:"), BorderLayout.NORTH);
    final JTable jTable = new JTable(logPatternsTableModel);
    jTable.setDefaultRenderer(Properties.class, new MultilineStringTableCellRenderer<Properties>(
      properties -> properties
        .keySet()
        .stream()
        .map(Object::toString)
        .sorted()
        .map(key -> key + "=" + properties.getProperty(key))
        .collect(Collectors.joining("\n"))
    ));
    panelApprove.add(new JScrollPane(jTable));
    addLoggers = new AbstractAction("Add logger definition", Icons.PLUS_24) {

      @Override
      public void actionPerformed(ActionEvent e) {
        List<LogPatternsTableModelEntry> toAdd = logPatternsTableModel.data
          .stream()
          .filter(x -> x.status instanceof WillImport)
          .collect(Collectors.toList());
        toAdd.forEach(p -> {
          final Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
          final LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(logParser);
          try (OutputStream out = new FileOutputStream(new File(AllPluginables.USER_LOG_IMPORTERS, UUID.randomUUID().toString() + ".pattern"))) {
            p.getProperties().setProperty("name", p.getPattern());
            logImporterUsingParser.init(p.properties);
            otrosApplication.getAllPluginables().getLogImportersContainer().addElement(logImporterUsingParser);
            p.getProperties().store(out, "Imported log pattern");
          } catch (InitializationException | IOException e1) {
            //Ignore it
          }
        });
        closeFunction.apply(ConvertLogFormatPanel.this);
        otrosApplication.getStatusObserver().updateStatus("Log patterns imported");
      }
    };

    final JXButton addButton = new JXButton(addLoggers);
    addButton.setMnemonic('a');
    addButton.setName("ConvertLogFormatPanel.addButton");
    OtrosSwingUtils.fontSize2(addButton);

    panelApprove.add(addButton, BorderLayout.SOUTH);

    cardLayout = new CardLayout();
    this.setLayout(cardLayout);
    this.add(panelSelect, PANEL_SELECT);
    this.add(panelApprove, PANEL_APPROVE);
    cardLayout.show(this, PANEL_SELECT);
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

  private List<String> extractLayoutPatterns(File selectedFile) throws IOException {
    String content = Files.readLines(selectedFile, Charset.forName("UTF-8")).stream().collect(Collectors.joining("\n"));
    if (selectedFile.getName().endsWith(".xml")) {
      //TODO log4j.xml pattern
      Pattern p = Pattern.compile("<pattern>\\s*(.*?)\\s*</pattern>", Pattern.MULTILINE);
      final Matcher matcher = p.matcher(content);
      List<String> r = new ArrayList<>();
      while (matcher.find()) {
        r.add(matcher.group(1));
      }
      return r;
    } else if (selectedFile.getName().endsWith(".properties")) {
      final Properties properties = new Properties();
      properties.load(new StringReader(content));
      return properties
        .<String>keySet()
        .stream()
        .map(Object::toString)
        .filter(key -> key.endsWith("ConversionPattern"))
        .map(properties::getProperty)
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
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
        return d.pattern;
      } else if (columnIndex == 1) {
        return d.getProperties();
      } else {
        return d.status;
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

}
