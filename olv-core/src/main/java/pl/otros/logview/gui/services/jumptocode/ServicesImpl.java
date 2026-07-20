package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.services.*;
import pl.otros.logview.gui.services.persist.SerializePersisService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerServiceImpl;

public class ServicesImpl implements Services {
  private JumpToCodeService jumpToCodeService;
  private TaskSchedulerServiceImpl taskSchedulerService;
  private PersistService persistService;
  private StatsService statsService;
  private StatsReporterService statsReporterService;

  public ServicesImpl(OtrosApplication otrosApplication) {
    taskSchedulerService = new TaskSchedulerServiceImpl();
    persistService = new SerializePersisService();
    statsService = new StatsService.NoOpStatsService();
    statsReporterService = new StatsReporterService.NoOpStatsReporterService();
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
