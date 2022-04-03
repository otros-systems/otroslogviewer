package pl.otros.logview.gui.open;

import org.apache.commons.configuration.Configuration;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.gui.browser.LogParsableListener;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;

import java.util.ArrayList;
import java.util.List;

public class LogVfsBrowserDialog extends JOtrosVfsBrowserDialog {

  private final List<LogImporter> parsableLogImporters = new ArrayList<>();

  public LogVfsBrowserDialog(LogParsableListener logParsableListener, Configuration configuration) {
    super(configuration, logParsableListener);
    logParsableListener.setParsableLogImporterConsumer(this::clearAndAddAllParsableLogImporters);
  }

  public List<LogImporter> getParsableLogImporters() {
    return parsableLogImporters;
  }

  private void clearAndAddAllParsableLogImporters(List<LogImporter> parsableLogImporters) {
    this.parsableLogImporters.clear();
    this.parsableLogImporters.addAll(parsableLogImporters);
  }
}
