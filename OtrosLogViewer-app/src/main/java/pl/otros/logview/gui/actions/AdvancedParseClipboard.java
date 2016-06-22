package pl.otros.logview.gui.actions;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.gui.util.DelayedSwingInvoke;
import pl.otros.logview.gui.util.DocumentInsertUpdateHandler;
import pl.otros.logview.util.UnixProcessing;
import pl.otros.vfs.browser.util.VFSUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

public class AdvancedParseClipboard extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedParseClipboard.class);
  private JComboBox<LogImporter> logParserComboBox;
  private JLabel statusLabel;

  public AdvancedParseClipboard(OtrosApplication otrosApplication) {
    super("Advanced Parse clipboard", otrosApplication);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Optional<String> maybeContent;

    maybeContent = getStringFromClipboard(systemClipboard);
    if (!maybeContent.isPresent()) {
      //TODO add warning on dialog instead of this
      JOptionPane.showMessageDialog(getOtrosApplication().getApplicationJFrame(), "Can't get clipboard content");
      return;
    }

    final String data = maybeContent.get();

    JProgressBar progressBar = new JProgressBar(0, 1);
    progressBar.setValue(1);
    progressBar.setIndeterminate(true);
    progressBar.setStringPainted(true);
    progressBar.setString("Detecting logs");
    final JDialog dialog = new JDialog(getOtrosApplication().getApplicationJFrame());
    dialog.setModal(true);
    dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

    final JPanel contentPanel = new JPanel(new MigLayout());
    contentPanel.add(new JLabel("Detecting log format"));
    contentPanel.add(progressBar, "wrap, growx");
    final JTextArea textArea = new JTextArea(data);
    textArea.setEditable(true);
    textArea.setFont(new Font(Font.MONOSPACED,textArea.getFont().getStyle(),textArea.getFont().getSize()));
    final JScrollPane contentView = new JScrollPane(textArea);
    contentView.setBorder(BorderFactory.createTitledBorder("Clipboard content"));
    contentPanel.add(contentView, "width 200:500:700, height 200:200:200, span, wrap");
    final AbstractAction refreshAction = new AbstractAction("Paste clipboard") {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateFromClipboard(systemClipboard, textArea);
      }
    };
    JXHyperlink refresh = new JXHyperlink(refreshAction);
    contentPanel.add(refresh, "wrap");

    contentPanel.add(new JLabel("cut / grep / sed"));
    final JXTextField processingPattern = new JXTextField("Unix CLI style: grep INFO | cut -c10-9999");
    contentPanel.add(processingPattern, "wrap, growx");

    final JTextArea textAreaProceed = new JTextArea(data);
    textAreaProceed.setEditable(false);
    textAreaProceed.setFont(new Font(Font.MONOSPACED,textArea.getFont().getStyle(),textArea.getFont().getSize()));
    final JScrollPane processedContentView = new JScrollPane(textAreaProceed);
    processedContentView.setBorder(BorderFactory.createTitledBorder("Processed clipboard"));
    contentPanel.add(processedContentView, "width 200:500:700, height 200:200:200, span, wrap");

    contentPanel.add(new JLabel("Select log parser"));
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
    contentPanel.add(logParserComboBox, "wrap");

    statusLabel = new JLabel(" ");
    contentPanel.add(statusLabel, "wrap, growx, span");


    final JButton importButton = new JButton("Import");
    importButton.addActionListener(x -> {
      try {
        loadLogFileAsContent(processText(textArea.getText(), processingPattern.getText()));
        dialog.dispose();
      } catch (IOException e1) {
        JOptionPane.showMessageDialog(dialog, "Can't open log");//TODO different warring
      }
    });

    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(x -> dialog.dispose());

    final JPanel buttonsPanel = new JPanel(new MigLayout());
    buttonsPanel.add(importButton, "");
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
          final ParsingWorker parsingWorker = new ParsingWorker(textAreaProceed, progressBar, importButton, logParserComboBox, logImporterCbxModel);
          worker = Optional.of(parsingWorker);
          parsingWorker.execute();
          statusLabel.setText("Processing command is OK");
          statusLabel.setIcon(Icons.STATUS_OK);

        } catch (Exception e) {
          e.printStackTrace();
          processingPattern.setBackground(Color.RED);
          statusLabel.setText("Wrong processing command");
          statusLabel.setIcon(Icons.STATUS_ERROR);
        }
      }
    };
    processingPattern.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        processingPattern.setBackground(Color.YELLOW);
        delayedSwingInvoke.performAction();
      }
    });
    textArea.getDocument().addDocumentListener(new DocumentInsertUpdateHandler() {
      @Override
      protected void documentChanged(DocumentEvent e) {
        delayedSwingInvoke.performAction();
      }
    });


    contentPanel.getActionMap().put("refresh", refreshAction);
    final KeyStroke[] keyStrokes = textArea.getInputMap(JComponent.WHEN_FOCUSED).allKeys();
    Arrays.asList(textArea.getActionMap().allKeys()).stream().forEach(System.out::println);
    Arrays.asList(keyStrokes).forEach(k -> System.out.println(k + " => " + textArea.getInputMap(JComponent.WHEN_FOCUSED).get(k)));

    contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta pressed V"),"refresh");

    delayedSwingInvoke.performAction();
    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
    dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
    dialog.setUndecorated(true);
    dialog.pack();
    dialog.setLocationRelativeTo(getOtrosApplication().getApplicationJFrame());
    dialog.setVisible(true);
    processingPattern.requestFocus();
  }

  private String processText(String text, String pattern) {
    String processed = text;
    if (pattern.trim().length() != 0) {
      processed = new UnixProcessing().processText(text, pattern);
    }
    return processed;
  }

  private void loadLogFileAsContent(String data) throws IOException {
    final String tabTitle = new SimpleDateFormat("HH:mm:ss").format(new Date());

    final LogImporter logImporter = logParserComboBox.getItemAt(logParserComboBox.getSelectedIndex());
    new TailLogActionListener(getOtrosApplication(), logImporter)
      .openFileObjectInTailMode(createFileObjectWithContent(data), "Clipboard " + tabTitle);

  }

  private void updateFromClipboard(Clipboard systemClipboard, JTextArea textArea) {
    final Optional<String> maybeContent = getStringFromClipboard(systemClipboard);
    if (maybeContent.isPresent()) {
      textArea.setText(maybeContent.get());
      textArea.setCaretPosition(0);
    } else {
      textArea.setText("");
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

  //TOOD move ot Utils
  private FileObject createFileObjectWithContent(String data) throws IOException {
    final File tempFile = File.createTempFile("olv_temp", "");
    OutputStream out = new FileOutputStream(tempFile);
    IOUtils.write(data, out, Charset.forName("UTF-8"));
    IOUtils.closeQuietly(out);
    final FileObject fileObject = VFSUtils.resolveFileObject(tempFile.toURI());
    return fileObject;
  }


  private class ParsingWorker extends SwingWorker<PossibleLogImporters, Boolean> {

    private JTextArea textArea;
    private JProgressBar progressBar;
    private JButton importButton;
    private JComboBox<LogImporter> logImporterJComboBox;
    private DefaultComboBoxModel<LogImporter> logImporterCbxModel;

    public ParsingWorker(JTextArea textArea, JProgressBar progressBar, JButton importButton, JComboBox<LogImporter> logImporterJComboBox, DefaultComboBoxModel<LogImporter> logImporterCbxModel) {
      this.textArea = textArea;
      this.progressBar = progressBar;
      this.importButton = importButton;
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
      progressBar.setString(".");
      if (isCancelled()) {
        return;
      }
      try {
        final PossibleLogImporters possibleLogImporters = get();
        importButton.setEnabled(possibleLogImporters.getLogImporter().isPresent());
        logImporterJComboBox.setEnabled(possibleLogImporters.getLogImporter().isPresent());

        logImporterCbxModel.removeAllElements();
        possibleLogImporters.getAvailableImporters().stream().forEach(logImporterCbxModel::addElement);
        possibleLogImporters.getLogImporter().ifPresent(logImporterJComboBox::setSelectedItem);
        if (possibleLogImporters.getAvailableImporters().isEmpty()) {
          statusLabel.setText("Defined log parser can't parse these log format");
          statusLabel.setIcon(Icons.STATUS_UNKNOWN);
        } else {
          statusLabel.setText("Can parse log from clipboard");
          statusLabel.setIcon(Icons.STATUS_OK);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    //TODO Move to Utils?
    private Callable<PossibleLogImporters> possibleLogImportersCallable(final String data) {
      return () -> {
        final File file = File.createTempFile("olv_clipboard", "");
        OutputStream out = new FileOutputStream(file);
        IOUtils.write(data, out, Charset.forName("UTF-8"));
        IOUtils.closeQuietly(out);
        file.delete();
        final Collection<LogImporter> logImporters = getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();
        final PossibleLogImporters possibleLogImporters = Utils.detectPossibleLogImporter(logImporters, data.getBytes());
        return possibleLogImporters;
      };
    }
  }

}
