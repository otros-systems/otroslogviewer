package pl.otros.logview.api.services;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;

public interface TaskSchedulerService {
  ListeningScheduledExecutorService getListeningScheduledExecutorService();
}
