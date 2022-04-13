package pl.otros.logview.api.services;

import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;

import java.util.Map;

public interface StatsService {
  void actionExecuted(OtrosAction action);
  void importLogsFromScheme(String protocol);
  void filesImportedIntoOneView(int count);
  void logParserUsed(LogImporter logImporter);
  void bytesRead(String protocol, long bytes);
  void logEventsImported(String scheme, long count);
  void filterUsed(String filter);
  void jumpToCodeExecuted();
  void contentReadFromIde();
  Map<String, Long> getStats();
}
