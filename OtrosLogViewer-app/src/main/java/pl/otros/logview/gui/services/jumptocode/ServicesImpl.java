package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.Services;
import pl.otros.logview.api.services.JumpToCodeService;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.gui.services.persist.SerializePersisService;
import pl.otros.logview.api.services.TaskSchedulerService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerServiceImpl;

public class ServicesImpl implements Services {
  private JumpToCodeService jumpToCodeService;
  private TaskSchedulerServiceImpl taskSchedulerService;
  private PersistService persistService;

  public ServicesImpl(OtrosApplication otrosApplication) {
    jumpToCodeService = new JumpToCodeServiceImpl(otrosApplication.getConfiguration());
    taskSchedulerService = new TaskSchedulerServiceImpl();
    persistService = new SerializePersisService();
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
}
