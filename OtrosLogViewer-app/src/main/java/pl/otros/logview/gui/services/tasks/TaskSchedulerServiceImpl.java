package pl.otros.logview.gui.services.tasks;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

public class TaskSchedulerServiceImpl implements TaskSchedulerService {
  private final ListeningScheduledExecutorService listeningScheduledExecutorService;

  public TaskSchedulerServiceImpl(){
    listeningScheduledExecutorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(4));
  }

  @Override
  public ListeningScheduledExecutorService getListeningScheduledExecutorService() {
    return listeningScheduledExecutorService;
  }
}
