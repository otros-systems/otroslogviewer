package pl.otros.logview.gui.services.tasks;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;

public interface TaskSchedulerService {
  ListeningScheduledExecutorService getListeningScheduledExecutorService();
}
