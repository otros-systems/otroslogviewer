package pl.otros.logview.gui.firstuse;

import java.util.Collection;

public class InitialConfiguration {
  private final String lookAndFeelClassname;
  private final Collection<LogPattern> logPatterns;

  private boolean checkForNewVersion;


  InitialConfiguration(
    String lookAndFeelClassname,
    Collection<LogPattern> logPatterns,
    boolean checkForNewVersion) {
    this.lookAndFeelClassname = lookAndFeelClassname;
    this.logPatterns = logPatterns;
    this.checkForNewVersion = checkForNewVersion;
  }


  public boolean isCheckForNewVersion() {
    return checkForNewVersion;
  }

  public String getLookAndFeelClassname() {
    return lookAndFeelClassname;
  }

  public Collection<LogPattern> getLogPatterns() {
    return logPatterns;
  }

  @Override
  public String toString() {
    return "InitialConfiguration{" +
      "lookAndFeelClassname='" + lookAndFeelClassname + '\'' +
      ", logPatterns=" + logPatterns +
      ", checkForNewVersion=" + checkForNewVersion +
      '}';
  }
}
