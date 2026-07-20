package pl.otros.logview.api.services;

import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;

import java.util.Collections;
import java.util.Map;

public interface StatsService {
  void actionExecuted(OtrosAction action);
  void importLogsFromScheme(String protocol);
  void filesImportedIntoOneView(int count);
  void logParserUsed(LogImporter logImporter);
  void bytesRead(String protocol, long bytes);
  void logEventsImported(String scheme, long count);
  void filterUsed(String filter);
  Map<String, Long> getStats();

  class NoOpStatsService implements StatsService {
    @Override public void actionExecuted(OtrosAction action) {}
    @Override public void importLogsFromScheme(String protocol) {}
    @Override public void filesImportedIntoOneView(int count) {}
    @Override public void logParserUsed(LogImporter logImporter) {}
    @Override public void bytesRead(String protocol, long bytes) {}
    @Override public void logEventsImported(String scheme, long count) {}
    @Override public void filterUsed(String filter) {}
    @Override public Map<String, Long> getStats() { return Collections.emptyMap(); }
  }
}
