package pl.otros.logview.gui.services;

import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.Services;
import pl.otros.logview.api.services.TaskSchedulerService;
import pl.otros.logview.gui.services.persist.SerializePersisService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerServiceImpl;

public class ServicesImpl implements Services {
  private final TaskSchedulerServiceImpl taskSchedulerService;
  private final PersistService persistService;

  public ServicesImpl() {
    taskSchedulerService = new TaskSchedulerServiceImpl();
    persistService = new SerializePersisService();
  }

  @Override
  public TaskSchedulerService getTaskSchedulerService() {
    return taskSchedulerService;
  }

  @Override
  public PersistService getPersistService() {
    return persistService;
  }
}
