package pl.otros.logview.gui.browser;

import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.vfs.browser.listener.SelectionListener;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class LogParsableListener implements SelectionListener {

  private CardLayout layout;
  private JPanel content;
  private final String PARSING = "parsing";
  private final String OK = "ok";
  private final PluginableElementsContainer<LogImporter> logImportersContainer;
  private JLabel label;
  private Optional<SwingWorker<PossibleLogImporters, Void>> worker = Optional.empty();

  public LogParsableListener(PluginableElementsContainer<LogImporter> logImportersContainer) {
    this.logImportersContainer = logImportersContainer;

    layout = new CardLayout();
    content = new JPanel(layout);
    JProgressBar progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setString("Detecting logs");
    progressBar.setIndeterminate(true);
    label = new JLabel();
    content.add(progressBar, PARSING);
    content.add(label, OK);
  }

  @Override
  public JComponent getView() {
    return content;
  }

  @Override
  public void selectedItem(FileObject... fileObjects) {
    worker.ifPresent(w -> w.cancel(false));
    label.setText("");
    label.setIcon(null);
    layout.show(content, OK);
  }

  @Override
  public void selectedContentPart(final FileObject fileObject, byte[] bytes) {
    layout.show(content, PARSING);
    worker.ifPresent(w -> w.cancel(false));
    final SwingWorker<PossibleLogImporters, Void> swingWorker = new SwingWorker<PossibleLogImporters, Void>() {

      @Override
      protected PossibleLogImporters doInBackground() throws Exception {
        return Utils.detectPossibleLogImporter(logImportersContainer.getElements(), bytes);
      }

      @Override
      protected void done() {
        if (isCancelled()) {
          return;
        }
        try {
          final boolean present = get().getLogImporter().isPresent();
          label.setText((present?"Can parse ":"Can't parse ") + fileObject.getName().getBaseName());
          label.setIcon(present ? Icons.STATUS_OK : Icons.STATUS_ERROR);
        } catch (InterruptedException | ExecutionException e) {
          label.setText("?");
        } finally {
          layout.show(content, OK);
        }

      }
    };
    swingWorker.execute();
    worker = Optional.of(swingWorker);

  }

  @Override
  public void enteredDir(FileObject fileObject) {
    worker.ifPresent(w -> w.cancel(false));
    label.setText("");
    label.setIcon(null);
    layout.show(content, OK);
  }
}
