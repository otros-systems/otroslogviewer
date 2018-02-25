package pl.otros.logview.api.services;

public interface Services {

  JumpToCodeService getJumpToCodeService();

  TaskSchedulerService getTaskSchedulerService();

  PersistService getPersistService();

  StatsService getStatsService();
}
