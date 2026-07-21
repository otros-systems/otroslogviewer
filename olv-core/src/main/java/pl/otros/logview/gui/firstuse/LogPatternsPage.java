package pl.otros.logview.gui.firstuse;

import com.google.common.io.Files;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.logppattern.LogbackLayoutEncoderConverter;
import pl.otros.logview.util.LoggerConfigUtil;
import pl.otros.swing.functional.DocumentChangeListener;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

class LogPatternsPage extends JPanel implements FirstUsePage {

  private final JTextPane loggerConfigTextPane;
  private final DefaultStyledDocument styledDocument;
  private final JTextPane resultTextPane;
  private final Style defaultStyle;
  private JFileChooser jFileChooser;

  LogPatternsPage() {
    initFileChooser();
    setLayout(new BorderLayout());
    final JPanel toolbar = new JPanel(new FlowLayout());
    toolbar.add(new JButton(new AbstractAction("Paste clipboard", Icons.CLIPBOARD_PASTE) {
      @Override
      public void actionPerformed(ActionEvent e) {
        pasteClipboard();
      }
    }));
    toolbar.add(new JButton(new AbstractAction("Import logback/log4j configuration file", Icons.FOLDER_OPEN) {
      @Override
      public void actionPerformed(ActionEvent e) {
        importFile();
      }
    }));

    loggerConfigTextPane = new JTextPane();
    loggerConfigTextPane.setText("Paste logback, log4j configuration files of just list of log patterns");
    loggerConfigTextPane.setCaretColor(loggerConfigTextPane.getForeground());
    loggerConfigTextPane.setCaret(new DefaultCaret());
    loggerConfigTextPane.getDocument().addDocumentListener(new DocumentChangeListener(this::updatePatterns));
    styledDocument = new DefaultStyledDocument();
    StyleContext context = new StyleContext();
    defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setFontFamily(defaultStyle, "monospaced");

    final JScrollPane scrollPaneForPatterns = new JScrollPane(loggerConfigTextPane);
    scrollPaneForPatterns.setBorder(BorderFactory.createTitledBorder("Logback/log4j configuration file content"));
    resultTextPane = new JTextPane(styledDocument);
    resultTextPane.setEditable(false);
    final JScrollPane scrollPaneForResult = new JScrollPane(resultTextPane);
    scrollPaneForResult.setBorder(BorderFactory.createTitledBorder("Found log patterns"));

    final JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.add(scrollPaneForPatterns);
    panel.add(scrollPaneForResult);
    add(toolbar, BorderLayout.NORTH);
    add(panel);
  }

  private void initFileChooser() {
    jFileChooser = new JFileChooser();
    jFileChooser.setMultiSelectionEnabled(true);
    jFileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().contains("log");
      }

      @Override
      public String getDescription() {
        return "Logback or log4j configuration";
      }
    });
  }

  private void importFile() {

    final int response = jFileChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION) {
      StringBuilder sb = new StringBuilder();
      final File[] selectedFile = jFileChooser.getSelectedFiles();
      for (File file : selectedFile) {
        try {
          final String read = Files.asCharSource(file, Charset.forName("UTF-8")).read();

          sb.append(read).append("\n");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      loggerConfigTextPane.setText(sb.toString());
    }
  }

  private void pasteClipboard() {
    try {
      String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
      loggerConfigTextPane.setText(data);
    } catch (UnsupportedFlavorException | IOException ignore) {
    }
  }

  private void updatePatterns() {
    final String text = loggerConfigTextPane.getText();

    final List<LogPattern> resultList = getLogPatterns(text);

    try {
      styledDocument.remove(0, styledDocument.getLength());
      for (LogPattern result : resultList) {
        resultTextPane.insertIcon(result.isValid() ? Icons.STATUS_OK : Icons.STATUS_ERROR);
        styledDocument.insertString(styledDocument.getLength(), " " + result.getPattern() + "\n", defaultStyle);
      }
    } catch (BadLocationException ignore) {
    }
    if (resultList.isEmpty()) {
      resultTextPane.setText("No logger patterns detected.");
    }

  }

  private List<LogPattern> getLogPatterns(String text) {
    final Set<String> layoutPatterns = LoggerConfigUtil.extractLayoutPatterns(text);
    final LogbackLayoutEncoderConverter converter = new LogbackLayoutEncoderConverter();
    return layoutPatterns
      .stream()
      .map(x -> {
        try {
          return new LogPattern(x, converter.convert(x), true);
        } catch (Exception e) {
          return new LogPattern(x, new Properties(), false);
        }
      })
      .collect(Collectors.toList());
  }

  @Override
  public String getTitle() {
    return "Log patterns";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public JComponent getView() {
    return this;
  }

  @Override
  public boolean onNext(WizardContext settings) {
    settings.put(Config.LOG_PATTERNS, new LogPatterns(getLogPatterns(loggerConfigTextPane.getText())));
    return true;
  }

}
