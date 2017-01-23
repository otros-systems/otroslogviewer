package pl.otros.logview.gui.session;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Level;

public class FileToOpen {
  private String uri;
  private OpenMode openMode;
  private Level level;
  private Optional<String> logImporter;

  public FileToOpen(String uri, OpenMode openMode, Level level, Optional<String> logImporter) {
    this.uri = uri;
    this.openMode = openMode;
    this.level = level;
    this.logImporter = logImporter;
  }

  public String getUri() {
    return uri;
  }

  public OpenMode getOpenMode() {
    return openMode;
  }

  public Level getLevel() {
    return level;
  }

  public Optional<String> getLogImporter() {
    return logImporter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileToOpen that = (FileToOpen) o;

    if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
    if (openMode != that.openMode) return false;
    if (level != null ? !level.equals(that.level) : that.level != null) return false;
    return logImporter != null ? logImporter.equals(that.logImporter) : that.logImporter == null;
  }

  @Override
  public int hashCode() {
    int result = uri != null ? uri.hashCode() : 0;
    result = 31 * result + (openMode != null ? openMode.hashCode() : 0);
    result = 31 * result + (level != null ? level.hashCode() : 0);
    result = 31 * result + (logImporter != null ? logImporter.hashCode() : 0);
    return result;
  }

  public String host(){
    try {
      final URI uri = new URI(this.uri);
      final String host = uri.getHost();
      return Optional.ofNullable(host).orElse("local file");
    } catch (URISyntaxException e) {
      return "local file";
    }
  }

  @Override
  public String toString() {
    return "FileToOpen{" +
        "uri='" + uri + '\'' +
        ", openMode=" + openMode +
        ", level=" + level +
        ", logImporter=" + logImporter +
        '}';
  }
}
