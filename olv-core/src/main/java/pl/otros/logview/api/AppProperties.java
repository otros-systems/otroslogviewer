package pl.otros.logview.api;

public class AppProperties {
  private String currentDir;

  public AppProperties() {
    currentDir = System.getProperty("CURRENT_DIR", "");
  }

  public String getCurrentDir() {
    return currentDir;
  }

  public void setCurrentDir(String currentDir) {
    this.currentDir = currentDir;
  }
}
