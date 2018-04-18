package pl.otros.logview.gui.firstuse;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.apache.commons.configuration.BaseConfiguration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.Ide;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeServiceImpl;
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
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

class LogPatternsPage extends WizardPage {

  private final JTextPane loggerConfigTextPane;
  private final DefaultStyledDocument styledDocument;
  private final JTextPane resultTextPane;
  private final Style defaultStyle;
  private final JButton importFromIdeButton;
  private JFileChooser jFileChooser;

  LogPatternsPage() {
    super("Log patterns", "");
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
    importFromIdeButton = new JButton(new AbstractAction("Import from IDE") {
      @Override
      public void actionPerformed(ActionEvent e) {
        importFromIde();
      }
    });
    toolbar.add(importFromIdeButton);

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


  private void importFromIde() {
    final BaseConfiguration configuration = new BaseConfiguration();
    final WizardSettings settings = this.getController().getSettings();
    configuration.setProperty(ConfKeys.JUMP_TO_CODE_HOST, settings.get(Config.IDE_HOST));
    configuration.setProperty(ConfKeys.JUMP_TO_CODE_PORT, settings.get(Config.IDE_PORT));
    loggerConfigTextPane.setEditable(false);
    loggerConfigTextPane.setText("Importing log patterns from IDE");
    new SwingWorker<Set<String>, Void>() {
      @Override
      protected Set<String> doInBackground() throws Exception {
        return new JumpToCodeServiceImpl(configuration).loggerPatterns();
      }

      @Override
      protected void done() {
        try {
          loggerConfigTextPane.setEditable(true);
          loggerConfigTextPane.setText(Joiner.on("\n").join(get()));
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
    }.execute();
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
      resultTextPane.setText("No logger patterns detected in project opened in IDE.");
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
  public void rendering(List<WizardPage> path, WizardSettings settings) {
    super.rendering(path, settings);
    final Optional<Ide> ide = Optional.ofNullable((Ide) settings.get("ide"));
    ide.ifPresent(i -> {
      if (i != Ide.DISCONNECTED) {
        importFromIde();
      }
      importFromIdeButton.setIcon(i.getIconConnected());
    });
    importFromIdeButton.setEnabled(ide.map(i -> i != Ide.DISCONNECTED).orElse(false));
  }

  @Override
  public boolean onNext(WizardSettings settings) {
    settings.put(Config.LOG_PATTERNS, new LogPatterns(getLogPatterns(loggerConfigTextPane.getText())));
    return super.onNext(settings);
  }

}
