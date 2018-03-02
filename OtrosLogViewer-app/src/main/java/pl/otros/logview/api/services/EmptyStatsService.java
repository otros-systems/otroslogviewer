package pl.otros.logview.api.services;

import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;

import java.util.Collections;
import java.util.Map;

public class EmptyStatsService implements StatsService {
  @Override
  public void actionExecuted(OtrosAction action) {
    //No action
  }

  @Override
  public void importLogsFromScheme(String protocol) {
    //No action
  }

  @Override
  public void filesImportedIntoOneView(int count) {
    //No action
  }

  @Override
  public void logParserUsed(LogImporter logImporter) {
    //No action
  }

  @Override
  public void bytesRead(String protocol, long bytes) {
    //No action
  }

  @Override
  public void logEventsImported(String scheme, long count) {
    //No action
  }

  @Override
  public Map<String, Long> getStats() {
    return Collections.emptyMap();
  }
}
