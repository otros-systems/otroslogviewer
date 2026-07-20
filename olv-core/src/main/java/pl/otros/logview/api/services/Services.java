package pl.otros.logview.api.services;

public interface Services {

  TaskSchedulerService getTaskSchedulerService();

  PersistService getPersistService();

  StatsService getStatsService();

  StatsReporterService getStatsReportService();

}
