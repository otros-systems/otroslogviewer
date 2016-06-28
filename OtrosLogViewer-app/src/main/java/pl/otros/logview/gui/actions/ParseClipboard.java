package pl.otros.logview.gui.actions;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
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
import pl.otros.swing.functional.StringListCellRenderer;
import pl.otros.swing.suggest.SuggestDecorator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

public class ParseClipboard extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParseClipboard.class);
  private final PersistentSuggestionSource<SearchSuggestion> suggestionSource;
  private JComboBox<LogImporter> logParserComboBox;
  private JLabel statusLabel;
  private boolean patternIsValid = true;
  private JButton importButton;
  private JComboBox<TabWithName> viewCombobox;

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
  public void actionPerformed(ActionEvent e) {
    final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();


    JProgressBar progressBar = new JProgressBar(0, 1);
    progressBar.setValue(1);
    progressBar.setIndeterminate(true);
    progressBar.setStringPainted(true);
    progressBar.setString("Detecting logs");
    final JDialog dialog = new JDialog(getOtrosApplication().getApplicationJFrame());
    dialog.setModal(true);
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

    final JPanel contentPanel = new JPanel(new MigLayout(
      new LC().fill().width("100%")
    ));

    final JTextArea textArea = new JTextArea(10, 100);
    textArea.setEditable(true);
    textArea.setFont(new Font(Font.MONOSPACED, textArea.getFont().getStyle(), textArea.getFont().getSize()));
    final JScrollPane contentView = new JScrollPane(textArea);
    contentView.setBorder(BorderFactory.createTitledBorder("Clipboard content"));

    final AbstractAction refreshAction = new AbstractAction("Paste clipboard") {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateFromClipboard(systemClipboard, textArea);
      }
    };
    JXHyperlink refresh = new JXHyperlink(refreshAction);

    final JLabel labelCutGrepSed = new JLabel("cut  | grep | sed");
    final JXTextField processingPattern = new JXTextField("Unix CLI style: grep INFO | cut -c10-9999");
    labelCutGrepSed.setDisplayedMnemonic('c');
    labelCutGrepSed.setLabelFor(processingPattern);


    SuggestDecorator.decorate(
      processingPattern,
      suggestionSource,
      new SearchSuggestionRenderer(),
      s->processingPattern.setText(s.getValue().getFullContent()));

    final JTextArea textAreaProceed = new JTextArea("");
    textAreaProceed.setEditable(false);
    textAreaProceed.setFont(new Font(Font.MONOSPACED, textArea.getFont().getStyle(), textArea.getFont().getSize()));
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
    viewCombobox = new JComboBox<>(getTabsWithName(getOtrosApplication().getJTabbedPane()).toArray(new TabWithName[0]));
    labelView.setLabelFor(viewCombobox);
    viewCombobox.setRenderer(new StringListCellRenderer<>(tabWithName -> tabWithName.getTitle()));


    statusLabel = new JLabel(" ");


    importButton = new JButton("Import");
    importButton.addActionListener(x -> {
      try {
        final String processingPatternText = processingPattern.getText();
        final TabWithName target = viewCombobox.getItemAt(viewCombobox.getSelectedIndex());
        loadLogFileAsContent(processText(textArea.getText(), processingPatternText),target);
        dialog.dispose();
        if (StringUtils.isNotBlank(processingPatternText)){
          suggestionSource.add(new SearchSuggestion(processingPatternText.trim(),processingPatternText.trim()));
        }
        try {
          getOtrosApplication().getServices().getPersistService().persist("grep history", "list of ...");
        } catch (Exception e1) {
          LOGGER.error("Can't save suggestions for processing logs from clipboard");
        }
      } catch (IOException e1) {
        statusLabel.setIcon(Icons.STATUS_ERROR);
        statusLabel.setText("Can't import logs: " + e1.getMessage());
        LOGGER.error("Can't import logs",e1);
      }
    });

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
          final String text = textArea.getText();
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
    textArea.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        LOGGER.debug("Text area with source log changed, firing delayed action");
        delayedSwingInvoke.performAction();
      }
    });


    contentPanel.getActionMap().put("refresh", refreshAction);
    //pressed PASTE => paste-from-clipboard
    contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed PASTE"), "refresh");


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
    updateFromClipboard(systemClipboard, textArea);
    dialog.pack();
    dialog.setLocationRelativeTo(getOtrosApplication().getApplicationJFrame());
    dialog.setVisible(true);
    processingPattern.requestFocus();
  }

  private List<TabWithName> getTabsWithName(JTabbedPane jTabbedPane) {
    final ArrayList<TabWithName> tabs = new ArrayList<>();
    tabs.add(0,new TabWithName("New view",Optional.empty()));
    for (int i=0; i< jTabbedPane.getTabCount(); i++){
      final JComponent tabComponentAt = (JComponent) jTabbedPane.getComponentAt(i);
      if (tabComponentAt instanceof LogViewPanelWrapper){
        final LogViewPanelWrapper logViewPanelWrapper = (LogViewPanelWrapper) tabComponentAt;
        final LogViewPanelI collector = logViewPanelWrapper.getLogViewPanel();
        tabs.add(new TabWithName(jTabbedPane.getTitleAt(i),Optional.of(collector)));
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

    final FileObject fileObject = Utils.createFileObjectWithContent(data);
    final LogImporter logImporter = logParserComboBox.getItemAt(logParserComboBox.getSelectedIndex());
    if (target.getLogDataCollector().isPresent()){
      final LogViewPanelI logViewPanelI = target.getLogDataCollector().get();
      getOtrosApplication().getLogLoader().startLoading(new VfsSource(fileObject),logImporter,logViewPanelI);
    } else {
      final String tabTitle = new SimpleDateFormat("HH:mm:ss").format(new Date());
      new TailLogActionListener(getOtrosApplication(), logImporter)
        .openFileObjectInTailMode(fileObject, "Clipboard " + tabTitle);
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
    Optional<String> data;
    try {
      data = Optional.of((String) systemClipboard.getData(DataFlavor.stringFlavor));
    } catch (UnsupportedFlavorException | IOException e1) {
      data = Optional.empty();
    }
    return data;
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
      String text = textArea.getText();
      final long start = System.currentTimeMillis();
      final PossibleLogImporters call = possibleLogImportersCallable(text).call();
      long duration = System.currentTimeMillis() - start;
      LOGGER.debug("Finished log format detection, it took " + duration + "ms, have found " + call.getLogImporter());

      return call;
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
        possibleLogImporters.getAvailableImporters().stream().forEach(logImporterCbxModel::addElement);
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

    //TODO Move to Utils?
    private Callable<PossibleLogImporters> possibleLogImportersCallable(final String data) {
      return () -> {
        final Collection<LogImporter> logImporters = getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();
        return Utils.detectPossibleLogImporter(logImporters, data.getBytes());
      };
    }
  }

  private void updateImportButtonState() {
    final int itemCount = logParserComboBox.getItemCount();
    final boolean patternIsValid = this.patternIsValid;
    LOGGER.debug("Detected log parsers count " + itemCount + ", patter is valid: " + patternIsValid);
    importButton.setEnabled(itemCount > 0 && patternIsValid);
  }

}
final class TabWithName {
  private String title;
  private Optional<LogViewPanelI> logDataCollector;

  public TabWithName(String title, Optional<LogViewPanelI> logDataCollector) {
    this.title = title;
    this.logDataCollector = logDataCollector;
  }

  public String getTitle() {
    return title;
  }

  public Optional<LogViewPanelI> getLogDataCollector() {
    return logDataCollector;
  }
}
