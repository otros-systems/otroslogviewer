package pl.otros.logview.gui.services;

import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.gui.services.persist.PersistService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerService;

public interface Services {

  JumpToCodeService getJumpToCodeService();
  TaskSchedulerService getTaskSchedulerService();
  PersistService getPersistService();
}
