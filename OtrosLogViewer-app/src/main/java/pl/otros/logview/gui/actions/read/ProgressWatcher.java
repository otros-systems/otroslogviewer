/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.actions.read;

import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.gui.LogImportStats;
import pl.otros.logview.gui.LogViewPanelWrapper;
import pl.otros.logview.io.ObservableInputStreamImpl;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;

public class ProgressWatcher implements Runnable {

  private final ObservableInputStreamImpl in;
  private final JProgressBar progressBar;
  private boolean refreshProgress = true;
  private final LogViewPanelWrapper frame;
  private final FileObject fileName;
  private final NumberFormat npf = NumberFormat.getPercentInstance();
  private final NumberFormat nbf = NumberFormat.getIntegerInstance();
  private final LogImportStats importStats;

  public ProgressWatcher(ObservableInputStreamImpl in, LogViewPanelWrapper frame, FileObject fileName, LogImportStats importStats) {
    super();
    this.in = in;
    this.frame = frame;
    this.fileName = fileName;
    this.progressBar = frame.getLoadingProgressBar();
    this.importStats = importStats;
    npf.setMaximumFractionDigits(0);
    nbf.setGroupingUsed(true);
  }

  @Override
  public void run() {
    while (refreshProgress) {
      try {
        Thread.sleep(100);
        long max = fileName.getContent().getSize();
        if (max <= 0) {
          updateNotDetermined("Loading");
        } else {
          long current = in.getCurrentRead();
          float percent = (float) current / max;
          long currentInKb = current / 1024;
          long maxInKb = max / 1024;
          importStats.updateStats(System.currentTimeMillis(), currentInKb, maxInKb);
          String message = "Loading " + fileName.getName().getBaseName() + " ... " + npf.format(percent) + "[" + nbf.format(currentInKb) + "kb of "
              + nbf.format(maxInKb) + "kb]";
          updateProgress(message, (int) current, 0, (int) max);

        }
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // waiting interrupted, nothing serious.

      } catch (IOException e) {
        // e.printStackTrace();
        // stream closed - all loaded
        break;
      }
    }
    updateFinish("Loaded");

  }

  public void updateNotDetermined(final String message) {
    Runnable r = () -> {
      progressBar.setIndeterminate(true);
      progressBar.setString(message);
    };

    SwingUtilities.invokeLater(r);
  }

  public void updateProgress(final String message, final int current, final int min, final int max) {
    Runnable r = () -> {
      progressBar.setIndeterminate(false);
      progressBar.setMaximum(max);
      progressBar.setMinimum(min);
      progressBar.setValue(current);
      progressBar.setString(message);
      // refreshProgress = false;

    };
    try {
      SwingUtilities.invokeAndWait(r);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

  }

  public void updateFinish(final String message) {
    Runnable r = () -> {
      progressBar.setIndeterminate(false);

      progressBar.setMaximum(1);
      progressBar.setMinimum(0);
      progressBar.setValue(1);
      progressBar.setString(message);
      frame.switchToContentView();
      refreshProgress = false;
    };
    SwingUtilities.invokeLater(r);

  }

}
