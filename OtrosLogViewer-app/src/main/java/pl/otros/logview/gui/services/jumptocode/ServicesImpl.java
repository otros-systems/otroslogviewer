package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.services.*;
import pl.otros.logview.gui.services.persist.SerializePersisService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerServiceImpl;
import pl.otros.logview.stats.DropwizardStatsService;
import pl.otros.logview.stats.HttpStatsReporterService;

public class ServicesImpl implements Services {
  private JumpToCodeService jumpToCodeService;
  private TaskSchedulerServiceImpl taskSchedulerService;
  private PersistService persistService;
  private StatsService statsService;
  private StatsReporterService statsReporterService;

  public ServicesImpl(OtrosApplication otrosApplication) {
    taskSchedulerService = new TaskSchedulerServiceImpl();
    persistService = new SerializePersisService();
    statsService = new DropwizardStatsService(persistService);
    statsReporterService = new HttpStatsReporterService();
    jumpToCodeService = new JumpToCodeServiceStatsWrapper(new JumpToCodeServiceImpl(otrosApplication.getConfiguration()),statsService);
  }

  @Override
  public JumpToCodeService getJumpToCodeService() {
    return jumpToCodeService;
  }

  @Override
  public TaskSchedulerService getTaskSchedulerService() {
    return taskSchedulerService;
  }

  @Override
  public PersistService getPersistService() {
    return persistService;
  }

  @Override
  public StatsService getStatsService() {
    return statsService;
  }

  @Override
  public StatsReporterService getStatsReportService() {
    return statsReporterService;
  }
}
