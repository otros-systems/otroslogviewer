package pl.otros.logview.gui;

import com.google.common.base.Function;
import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.gui.firstuse.InitialConfiguration;
import pl.otros.logview.gui.firstuse.LogPattern;
import pl.otros.logview.util.LoggerConfigUtil;

import java.util.Collection;

class InitialConfigurationProcessing implements Function<InitialConfiguration, Void> {

  private final OtrosApplication otrosApplication;

  InitialConfigurationProcessing(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
  }

  @Override
  public Void apply(InitialConfiguration initialConfig) {
    final DataConfiguration cfg = otrosApplication.getConfiguration();
    cfg.setProperty(ConfKeys.FIRST_USE, false);
    cfg.setProperty(ConfKeys.APPEARANCE_LOOK_AND_FEEL, initialConfig.getLookAndFeelClassname());
    cfg.setProperty(ConfKeys.VERSION_CHECK_ON_STARTUP, initialConfig.isCheckForNewVersion());
    cfg.setProperty(ConfKeys.SEND_STATS, initialConfig.isSendingStats());
    cfg.setProperty(ConfKeys.SEND_STATS_NOTIFY, initialConfig.isNotifySendingStats());
    cfg.setProperty(ConfKeys.JUMP_TO_CODE_HOST, initialConfig.getIdeConfiguration().getIdeHost());
    cfg.setProperty(ConfKeys.JUMP_TO_CODE_PORT, initialConfig.getIdeConfiguration().getIdePort());
    final Collection<LogPattern> logPatterns = initialConfig.getLogPatterns();
    logPatterns
      .stream()
      .filter(LogPattern::isValid)
      .forEach(lp -> LoggerConfigUtil.addLog4jParser(otrosApplication, java.util.UUID.randomUUID().toString(), lp.getPattern(), lp.getProperties()));
    return null;
  }
}
