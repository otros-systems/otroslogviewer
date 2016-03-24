package pl.otros.logview.api;

import pl.otros.logview.api.services.JumpToCodeService;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.TaskSchedulerService;

public interface Services {

  JumpToCodeService getJumpToCodeService();
  TaskSchedulerService getTaskSchedulerService();
  PersistService getPersistService();
}
