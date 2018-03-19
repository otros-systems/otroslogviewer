package pl.otros.logview.gui.firstuse;

import java.util.Collection;

public class InitialConfiguration {
  private final String lookAndFeelClassname;
  private final Collection<LogPattern> logPatterns;
  private final IdeConfiguration ideConfiguration;

  private final boolean sendStats;
  private boolean checkForNewVersion;

  InitialConfiguration(
    String lookAndFeelClassname,
    Collection<LogPattern> logPatterns,
    IdeConfiguration ideConfiguration,
    boolean collectStats,
    boolean checkForNewVersion
  ) {
    this.lookAndFeelClassname = lookAndFeelClassname;
    this.logPatterns = logPatterns;
    this.ideConfiguration = ideConfiguration;
    this.sendStats = collectStats;
    this.checkForNewVersion = checkForNewVersion;
  }


  public boolean isCheckForNewVersion() {
    return checkForNewVersion;
  }

  public boolean isSendingStats() {
    return sendStats;
  }

  public String getLookAndFeelClassname() {
    return lookAndFeelClassname;
  }

  public Collection<LogPattern> getLogPatterns() {
    return logPatterns;
  }

  public IdeConfiguration getIdeConfiguration() {
    return ideConfiguration;
  }

  @Override
  public String toString() {
    return "InitialConfiguration{" +
      "lookAndFeelClassname='" + lookAndFeelClassname + '\'' +
      ", logPatterns=" + logPatterns +
      ", ideConfiguration=" + ideConfiguration +
      ", sendStats=" + sendStats +
      ", checkForNewVersion=" + checkForNewVersion +
      '}';
  }
}
