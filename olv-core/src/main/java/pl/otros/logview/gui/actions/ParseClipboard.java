package pl.otros.logview.gui.actions;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelI;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.loading.VfsSource;
import pl.otros.logview.gui.suggestion.SearchSuggestion;
import pl.otros.logview.gui.suggestion.SearchSuggestionRenderer;
import pl.otros.logview.gui.util.DelayedSwingInvoke;
import pl.otros.logview.gui.util.DocumentInsertUpdateHandler;
import pl.otros.logview.gui.util.PersistentSuggestionSource;
import pl.otros.logview.util.UnixProcessing;
import pl.otros.swing.functional.LambdaAction;
import pl.otros.swing.functional.StringListCellRenderer;
import pl.otros.swing.suggest.SuggestDecorator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class ParseClipboard extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParseClipboard.class);
  private final PersistentSuggestionSource<SearchSuggestion> suggestionSource;
  private JComboBox<LogImporter> logParserComboBox;
  private JLabel statusLabel;
  private boolean patternIsValid = true;
  private JButton importButton;
  private LambdaAction importAction;
  private final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  public ParseClipboard(OtrosApplication otrosApplication) {
    super("Parse clipboard", Icons.CLIPBOARD_PASTE, otrosApplication);
    suggestionSource = new PersistentSuggestionSource<>(
      "clipboardProcessingPatterns",
      getOtrosApplication().getServices().getPersistService(),
      (s, suggestionQuery) -> s.getFullContent().contains(suggestionQuery.getValue()),
      SearchSuggestion::getFullContent,
      s -> new SearchSuggestion(s, s)
    );
  }

  @Override
  protected void actionPerformedHook(ActionEvent e) {
    final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();


    JProgressBar progressBar = new JProgressBar(0, 1);
    progressBar.setValue(1);
    progressBar.setIndeterminate(true);
    progressBar.setStringPainted(true);
    progressBar.setString("Detecting logs");
    final JDialog dialog = new JDialog(getOtrosApplication().getApplicationJFrame());
    dialog.setModal(true);
    dialog.setName("Import logs from clipboard");
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

    final JPanel contentPanel = new JPanel(new MigLayout(
      new LC().fill().width("100%")
    ));

    final JTextArea clipboardContentTextArea = new JTextArea(10, 100);
    clipboardContentTextArea.setName("importClipboard.content");
    clipboardContentTextArea.setEditable(true);
    clipboardContentTextArea.setFont(new Font(Font.MONOSPACED, clipboardContentTextArea.getFont().getStyle(), clipboardContentTextArea.getFont().getSize()));
    final JScrollPane contentView = new JScrollPane(clipboardContentTextArea);
    contentView.setBorder(BorderFactory.createTitledBorder("Clipboard content"));

    final AbstractAction refreshAction = new AbstractAction("Paste clipboard") {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateFromClipboard(systemClipboard, clipboardContentTextArea);
      }
    };
    JXHyperlink refresh = new JXHyperlink(refreshAction);
    refresh.setName("importClipboard.refresh");

    final JLabel labelCutGrepSed = new JLabel("cut  | grep | sed");
    final JXTextField processingPattern = new JXTextField("Unix CLI style: grep INFO | cut -c10-9999");
    processingPattern.setName("importClipboard.processingPattern");
    labelCutGrepSed.setDisplayedMnemonic('c');
    labelCutGrepSed.setLabelFor(processingPattern);


    SuggestDecorator.decorate(
      processingPattern,
      suggestionSource,
      new SearchSuggestionRenderer(),
      s -> processingPattern.setText(s.getValue().getFullContent()));

    final JTextArea textAreaProceed = new JTextArea("");
    textAreaProceed.setName("importClipboard.processedContent");
    textAreaProceed.setEditable(false);
    textAreaProceed.setFont(new Font(Font.MONOSPACED, clipboardContentTextArea.getFont().getStyle(), clipboardContentTextArea.getFont().getSize()));
    final JScrollPane processedContentView = new JScrollPane(textAreaProceed);
    processedContentView.setBorder(BorderFactory.createTitledBorder("Processed clipboard"));

    final JLabel logParserLabel = new JLabel("Select log parser");
    logParserLabel.setDisplayedMnemonic('p');

    final DefaultComboBoxModel<LogImporter> logImporterCbxModel = new DefaultComboBoxModel<>();
    logParserComboBox = new JComboBox<>(logImporterCbxModel);
    logParserComboBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof LogImporter) {
          LogImporter logImporter = (LogImporter) value;
          listCellRendererComponent.setText(logImporter.getName());
        }
        return listCellRendererComponent;
      }
    });
    logParserLabel.setLabelFor(logParserComboBox);

    final JLabel labelView = new JLabel("View");
    labelView.setDisplayedMnemonic('V');
    final TabWithName[] tabWithNames = getTabsWithName(getOtrosApplication().getJTabbedPane()).toArray(new TabWithName[0]);
    JComboBox<TabWithName> viewCombobox = new JComboBox<>(tabWithNames);
    Arrays.stream(tabWithNames)
      .filter(TabWithName::isSelected)
      .findFirst()
      .ifPresent(viewCombobox::setSelectedItem);

    labelView.setLabelFor(viewCombobox);
    viewCombobox.setRenderer(new StringListCellRenderer<>(TabWithName::getTitle));

    statusLabel = new JLabel(" ");
    importAction = new LambdaAction("Import",
      x -> {
        try {
          final String processingPatternText = processingPattern.getText();
          final TabWithName target = viewCombobox.getItemAt(viewCombobox.getSelectedIndex());
          loadLogFileAsContent(processText(clipboardContentTextArea.getText(), processingPatternText), target);
          dialog.dispose();
          if (StringUtils.isNotBlank(processingPatternText)) {
            suggestionSource.add(new SearchSuggestion(processingPatternText.trim(), processingPatternText.trim()));
          }
        } catch (IOException e1) {
          statusLabel.setIcon(Icons.STATUS_ERROR);
          statusLabel.setText("Can't import logs: " + e1.getMessage());
          LOGGER.error("Can't import logs", e1);
        }
      }
    );
    importButton = new JButton(importAction);
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(x -> dialog.dispose());

    final JPanel buttonsPanel = new JPanel();
    buttonsPanel.add(importButton, "growx");
    buttonsPanel.add(cancelButton, "");


    final DelayedSwingInvoke delayedSwingInvoke = new DelayedSwingInvoke() {

      private Optional<ParsingWorker> worker = Optional.empty();

      @Override
      protected void performActionHook() {
        try {
          final String text = clipboardContentTextArea.getText();
          final String pattern = processingPattern.getText();
          String processed = processText(text, pattern);
          textAreaProceed.setText(processed);
          textAreaProceed.setCaretPosition(0);
          processingPattern.setBackground(Color.GREEN);
          worker.ifPresent(w -> w.cancel(false));
          final ParsingWorker parsingWorker = new ParsingWorker(textAreaProceed, progressBar, logParserComboBox, logImporterCbxModel);
          worker = Optional.of(parsingWorker);
          parsingWorker.execute();
          statusLabel.setText("Processing command is OK");
          statusLabel.setIcon(Icons.STATUS_OK);
          patternIsValid = true;
        } catch (Exception e) {
          e.printStackTrace();
          processingPattern.setBackground(Color.RED);
          statusLabel.setText("Wrong processing command");
          statusLabel.setIcon(Icons.STATUS_ERROR);
          patternIsValid = false;
        } finally {
          LOGGER.debug("Delayed action finished");
          updateImportButtonState();
        }
      }
    };
    processingPattern.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        LOGGER.debug("Text field processing pattern changed, firing delayed action");
        processingPattern.setBackground(Color.YELLOW);
        delayedSwingInvoke.performAction();
      }
    });
    clipboardContentTextArea.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        LOGGER.debug("Text area with source log changed, firing delayed action");
        delayedSwingInvoke.performAction();
      }
    });


    contentPanel.getActionMap().put("refresh", refreshAction);
    contentPanel.getActionMap().put("cancel", new LambdaAction(x -> dialog.dispose()));
    contentPanel.getActionMap().put("import", importAction);

    contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, mask), "refresh");
    contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, mask), "import");


    contentPanel.add(contentView, "wmin 500, hmin 200, span, wrap");
    contentPanel.add(refresh, "wrap");
    contentPanel.add(labelCutGrepSed);
    contentPanel.add(processingPattern, "wrap, growx");
    contentPanel.add(processedContentView, "wmin 500, hmin 200, span, wrap");
    contentPanel.add(logParserLabel);
    contentPanel.add(logParserComboBox, "wmin 150, growx, wrap");
    contentPanel.add(labelView);
    contentPanel.add(viewCombobox, "wmin 150, growx, wrap");
    contentPanel.add(new JLabel("Detecting log format"));
    contentPanel.add(progressBar, "wrap, growx");
    contentPanel.add(statusLabel, "wrap, growx, span");


    delayedSwingInvoke.performAction();
    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
    dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
    dialog.setUndecorated(false);
    dialog.setTitle("Import logs from clipboard");
    updateFromClipboard(systemClipboard, clipboardContentTextArea);
    dialog.pack();
    dialog.setLocationRelativeTo(getOtrosApplication().getApplicationJFrame());
    dialog.setVisible(true);
    processingPattern.requestFocus();
  }

  private List<TabWithName> getTabsWithName(JTabbedPane jTabbedPane) {
    final ArrayList<TabWithName> tabs = new ArrayList<>();
    tabs.add(0, new TabWithName("New view", Optional.empty(), false));
    final int selectedIndex = jTabbedPane.getSelectedIndex();
    for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
      final JComponent tabComponentAt = (JComponent) jTabbedPane.getComponentAt(i);
      if (tabComponentAt instanceof LogViewPanelWrapper) {
        final LogViewPanelWrapper logViewPanelWrapper = (LogViewPanelWrapper) tabComponentAt;
        final LogViewPanelI collector = logViewPanelWrapper.getLogViewPanel();
        tabs.add(new TabWithName(jTabbedPane.getTitleAt(i), Optional.of(collector), i == selectedIndex));
      }
    }
    return tabs;
  }

  private String processText(String text, String pattern) {
    String processed = text;
    if (pattern.trim().length() != 0) {
      processed = new UnixProcessing().processText(text, pattern);
    }
    return processed;
  }

  private void loadLogFileAsContent(String data, TabWithName target) throws IOException {
    final FileObject tempFileWithClipboard = VFS.getManager().resolveFile("clipboard://clipboard_" + System.currentTimeMillis());
    tempFileWithClipboard.createFile();
    final OutputStream outputStream = tempFileWithClipboard.getContent().getOutputStream();
    outputStream.write(data.getBytes());
    outputStream.flush();
    outputStream.close();
    final LogImporter logImporter = logParserComboBox.getItemAt(logParserComboBox.getSelectedIndex());
    if (target.getLogDataCollector().isPresent()) {
      final LogViewPanelI logViewPanelI = target.getLogDataCollector().get();
      getOtrosApplication().getLogLoader().startLoading(new VfsSource(tempFileWithClipboard), logImporter, logViewPanelI);
    } else {
      final String tabTitle = new SimpleDateFormat("HH:mm:ss").format(new Date());
      new TailLogActionListener(getOtrosApplication(), logImporter)
        .openFileObjectInTailMode(tempFileWithClipboard, "Clipboard " + tabTitle);
    }


  }

  private void updateFromClipboard(Clipboard systemClipboard, JTextArea textArea) {
    final Optional<String> maybeContent = getStringFromClipboard(systemClipboard);
    if (maybeContent.isPresent()) {
      textArea.setText(maybeContent.get());
      textArea.setCaretPosition(0);
    } else {
      textArea.setText("");
      statusLabel.setIcon(Icons.STATUS_ERROR);
      statusLabel.setText("Clipboard content is not text");
    }
  }

  private Optional<String> getStringFromClipboard(Clipboard systemClipboard) {
    Optional<String> data = Optional.empty();
    try {
      data = Optional.of((String) systemClipboard.getData(DataFlavor.stringFlavor));
    } catch (UnsupportedFlavorException | IOException e1) {
      LOGGER.trace("No String found in Clipboard.");
    } catch (OutOfMemoryError | IllegalStateException e) {
      //the getData() Method can use a lot of memory. Try to display a hint for the user.
      LOGGER.error("Memory limit reached while reading from Clipboard");
      JPanel message = new JPanel();
      String recommended = (getMemoryInGb() * 2L) + "G";
      message.add(new JLabel("The Clipboard is too big to parse. " +
        "Increase the memory configuration in olv.bat/olv.sh for example to MEMORY=-Xmx" + recommended));
      JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    return data;
  }

  /**
   * Get the maximal heap memory in GB. If it is less than 1GB it will be return 1GB.
   */
  private long getMemoryInGb() {
    long maxMemory = Runtime.getRuntime().maxMemory() / 1024L / 1024L / 1024L;
    return maxMemory <= 0L ? 1L : maxMemory;
  }


  private class ParsingWorker extends SwingWorker<PossibleLogImporters, Boolean> {

    private JTextArea textArea;
    private JProgressBar progressBar;
    private JComboBox<LogImporter> logImporterJComboBox;
    private DefaultComboBoxModel<LogImporter> logImporterCbxModel;

    ParsingWorker(JTextArea textArea, JProgressBar progressBar, JComboBox<LogImporter> logImporterJComboBox, DefaultComboBoxModel<LogImporter> logImporterCbxModel) {
      this.textArea = textArea;
      this.progressBar = progressBar;
      this.logImporterJComboBox = logImporterJComboBox;
      this.logImporterCbxModel = logImporterCbxModel;
    }

    @Override
    protected void process(List<Boolean> chunks) {
      progressBar.setString("Detecting logs");
      progressBar.setIndeterminate(true);
    }

    @Override
    protected PossibleLogImporters doInBackground() throws Exception {
      publish(Boolean.TRUE);
      String text = StringUtils.substring(textArea.getText(), 0, 20000);
      LOGGER.info("Will process " + text.length() + " chars from clipboard");
      final long start = System.currentTimeMillis();
      final Collection<LogImporter> logImporters = getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();

      final PossibleLogImporters possibleLogImporters = Utils.detectPossibleLogImporter(logImporters, text.getBytes());
      long duration = System.currentTimeMillis() - start;
      LOGGER.debug("Finished log format detection, it took " + duration + "ms, have found " + possibleLogImporters.getLogImporter());

      return possibleLogImporters;
    }

    @Override
    protected void done() {
      LOGGER.info("Done");
      progressBar.setIndeterminate(false);
      progressBar.setString("Log format detection completed");
      if (isCancelled()) {
        return;
      }
      try {
        final PossibleLogImporters possibleLogImporters = get();
        logImporterJComboBox.setEnabled(possibleLogImporters.getLogImporter().isPresent());
        logImporterCbxModel.removeAllElements();
        possibleLogImporters.getAvailableImporters().forEach(logImporterCbxModel::addElement);
        possibleLogImporters.getLogImporter().ifPresent(logImporterJComboBox::setSelectedItem);
        if (possibleLogImporters.getAvailableImporters().isEmpty()) {
          statusLabel.setText("Defined log parser can't parse input");
          statusLabel.setIcon(Icons.STATUS_UNKNOWN);
        } else {
          statusLabel.setText("Can parse log from clipboard");
          statusLabel.setIcon(Icons.STATUS_OK);
        }
        updateImportButtonState();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  private void updateImportButtonState() {
    final int itemCount = logParserComboBox.getItemCount();
    final boolean patternIsValid = this.patternIsValid;
    LOGGER.debug("Detected log parsers count " + itemCount + ", patter is valid: " + patternIsValid);
    importAction.setEnabled(itemCount > 0 && patternIsValid);
  }

}

final class TabWithName {
  private final String title;
  private final Optional<LogViewPanelI> logDataCollector;
  private final boolean selected;

  protected TabWithName(String title, Optional<LogViewPanelI> logDataCollector, boolean selected) {
    this.title = title;
    this.logDataCollector = logDataCollector;
    this.selected = selected;
  }

  public String getTitle() {
    return title;
  }

  public Optional<LogViewPanelI> getLogDataCollector() {
    return logDataCollector;
  }

  public boolean isSelected() {
    return selected;
  }
}
