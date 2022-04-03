package pl.otros.logview.gui.browser;

import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.logview.gui.renderers.JComboBoxLogImporterRenderer;
import pl.otros.vfs.browser.listener.SelectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class LogParsableListener implements SelectionListener {

  private final JComboBoxLogImporterRenderer logParserComboBoxRenderer;
  private CardLayout layout;
  private JPanel content;
  private final String PARSING = "parsing";
  private final String OK = "ok";
  private final PluginableElementsContainer<LogImporter> logImportersContainer;
  private JComboBox<LogImporter> logParserComboBox;
  private Optional<SwingWorker<PossibleLogImporters, Void>> worker = Optional.empty();
  private Consumer<List<LogImporter>> parsableLogImporterConsumer;
  private final List<LogImporter> parsableLogImporters = new ArrayList<>();

  public LogParsableListener(PluginableElementsContainer<LogImporter> logImportersContainer) {
    this.logImportersContainer = logImportersContainer;

    layout = new CardLayout();
    content = new JPanel(layout);
    JProgressBar progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setString("Detecting logs");
    progressBar.setIndeterminate(true);
    content.add(progressBar, PARSING);

    logParserComboBox = new JComboBox<>();
    logParserComboBoxRenderer = new JComboBoxLogImporterRenderer();
    logParserComboBox.setRenderer(logParserComboBoxRenderer);
    logParserComboBox.addItemListener(this::onLogParserComboBoxEvent);
    logParserComboBox.setPrototypeDisplayValue(null);
    content.add(logParserComboBox, OK);

  }

  @Override
  public JComponent getView() {
    return content;
  }

  @Override
  public void selectedItem(FileObject... fileObjects) {
    worker.ifPresent(w -> w.cancel(false));
    logParserComboBoxRenderer.setFileNotParsable(null);
    logParserComboBox.removeAllItems();
    logParserComboBox.updateUI();//If change directory and before a not parsable file is selected the ui must be udpated
    layout.show(content, OK);
  }

  @Override
  public void selectedContentPart(final FileObject fileObject, byte[] bytes) {
    layout.show(content, PARSING);
    logParserComboBox.removeAllItems();

    worker.ifPresent(w -> w.cancel(false));
    final SwingWorker<PossibleLogImporters, Void> swingWorker = new SwingWorker<PossibleLogImporters, Void>() {

      @Override
      protected PossibleLogImporters doInBackground() {
        return Utils.detectPossibleLogImporter(logImportersContainer.getElements(), bytes);
      }

      @Override
      protected void done() {
        if (isCancelled()) {
          return;
        }
        try {
          Optional<LogImporter> parsableLogImporter = get().getLogImporter();
          final boolean logCanParse = parsableLogImporter.isPresent();
          //Only displayed if no Logimporter found
          logParserComboBoxRenderer.setFileNotParsable(fileObject.getName().getBaseName());

          parsableLogImporters.clear();
          parsableLogImporters.addAll(get().getAvailableImporters());
          logParserComboBox.removeAllItems();

          for (LogImporter availableImporter : get().getAvailableImporters()) {
            logParserComboBox.addItem(availableImporter);

          }
          if (logCanParse) {
            LogImporter logImporter = parsableLogImporter.get();
            //Put selected importer at first position. This importer will try to use at first for parsing file.
            putImporterOnFirstPosition(parsableLogImporters, logImporter);
            logParserComboBox.setSelectedItem(logImporter);
          }

          if (parsableLogImporterConsumer != null)
            parsableLogImporterConsumer.accept(parsableLogImporters);

        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
          logParserComboBoxRenderer.setFileNotParsable(null);
          logParserComboBox.removeAllItems();
        } finally {
          layout.show(content, OK);
        }

      }
    };
    swingWorker.execute();
    worker = Optional.of(swingWorker);

  }

  private void onLogParserComboBoxEvent(ItemEvent event) {
    if (event.getStateChange() == ItemEvent.SELECTED) {
      if (event.getItem() instanceof LogImporter) {
        //Put selected importer at first position. This importer will try to use at first for parsing file.
        putImporterOnFirstPosition(parsableLogImporters, (LogImporter) event.getItem());
        if (parsableLogImporterConsumer != null)
          parsableLogImporterConsumer.accept(parsableLogImporters);

      }
    }
  }

  private void putImporterOnFirstPosition(List<LogImporter> importers, LogImporter importer) {
    for (int i = 0; i < importers.size(); i++) {
      if (i > 0 && Objects.equals(importer.getName(), importers.get(i).getName())) {
        Collections.swap(importers, 0, i);
        break;
      }
    }
  }

  @Override
  public void enteredDir(FileObject fileObject) {
    worker.ifPresent(w -> w.cancel(false));
    SwingUtilities.invokeLater(() -> {
      logParserComboBox.removeAllItems();
      logParserComboBoxRenderer.setFileNotParsable(null);
      layout.show(content, OK);
    });
  }

  public void setParsableLogImporterConsumer(Consumer<List<LogImporter>> parsableLogImporterConsumer) {
    this.parsableLogImporterConsumer = parsableLogImporterConsumer;
  }
}
