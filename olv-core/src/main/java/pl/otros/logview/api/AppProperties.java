package pl.otros.logview.api;

public class AppProperties {
  private final String currentDir;

  public AppProperties() {
    currentDir = System.getProperty("user.dir", "");
  }

  public String getCurrentDir() {
    return currentDir;
  }
}
