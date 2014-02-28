package pl.otros.logview.gui.services;

import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerService;

public interface Services {

  public JumpToCodeService getJumpToCodeService();
  public TaskSchedulerService getTaskSchedulerService();
}
