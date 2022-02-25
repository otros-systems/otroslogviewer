package pl.otros.vfs.browser.actions;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The common SwingWorker is marked as cancelled if you run cancel() while the doInBackground() method is running.
 * But if the SwingWorker already ended doInBackground() method, but done() method is not started (queued into AWT Thread) and at this moment cancel() will be executed
 * the isCancelled() method return false.
 * <br>
 * The isCancelledBeforeDoneEnded() method returns independently true if the Worker canceled before doInBackground() or after.
 */
public abstract class CancelableSwingWorker extends SwingWorker<Void, Void> {
  private final AtomicBoolean cancel = new AtomicBoolean(false);

  public void doCancel() {
    this.cancel.set(true);
    super.cancel(false);
  }

  public boolean isCancelledBeforeDoneEnded() {
    return cancel.get();
  }
}
