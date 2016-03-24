package pl.otros.logview.api;

public class AppProperties {
  private String appDir;
  private String currentDir;

  public AppProperties() {
    appDir = System.getProperty("OLV_HOME","");
    currentDir = System.getProperty("CURRENT_DIR","");
  }

  public String getCurrentDir() {
    return currentDir;
  }

  public void setCurrentDir(String currentDir) {
    this.currentDir = currentDir;
  }

  public String getAppDir() {
    return appDir;
  }

  public void setAppDir(String appDir) {
    this.appDir = appDir;
  }
}
