package utils;

import pl.otros.logview.api.importer.LogImporter;

public class LogImporterAndFile {
  private LogImporter logImporter;
  private String file;

  LogImporterAndFile(LogImporter logImporter, String file) {
    this.logImporter = logImporter;
    this.file = file;
  }

  public LogImporter getLogImporter() {
    return logImporter;
  }

  public String getFile() {
    return file;
  }
}
