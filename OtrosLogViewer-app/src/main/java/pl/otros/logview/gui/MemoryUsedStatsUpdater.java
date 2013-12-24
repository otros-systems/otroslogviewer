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
package pl.otros.logview.gui;

import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.ease.Sine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

public class MemoryUsedStatsUpdater implements Runnable {

  private JProgressBar bar;
  private long refreshTime = 10000;
  private NumberFormat nf = NumberFormat.getInstance();

  public MemoryUsedStatsUpdater(JProgressBar bar, long refreshTime) {
    super();
    this.bar = bar;
    this.refreshTime = refreshTime;
    bar.setMaximum(100);
    bar.setMinimum(0);
    bar.setStringPainted(true);
    bar.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent arg0) {
        System.gc();
      }

    });
    nf.setMaximumFractionDigits(1);
    nf.setMinimumFractionDigits(1);
  }

  @Override
  public void run() {
    while (true) {
      long heapMaxSize = Runtime.getRuntime().maxMemory();
      long heapSize = Runtime.getRuntime().totalMemory();
      long free = Runtime.getRuntime().freeMemory();
      final float percentUsed = 100 * ((heapSize - free) / (float) heapSize);
      long percentOfTotalUsed = 100 * (heapSize - free) / heapMaxSize;
      Color newColor = Color.GREEN;
      if (percentOfTotalUsed > 93) {
        newColor = Color.RED;
      } else if (percentOfTotalUsed > 83) {
        newColor = Color.ORANGE;
      } else if (percentOfTotalUsed > 75) {
        newColor = Color.YELLOW;
      }
      final String message = String.format("Used %sMB of %sMB", nf.format((percentUsed * heapSize / (100 * 1024 * 1024))), nf.format(heapSize / (1024 * 1024)));
      final String toolTip = message
          + String.format(". Total available for VM %sMB. Double click to invoke System.gc()", nf.format(heapMaxSize / (1024 * 1024)));
      Timeline timeline = new Timeline(bar);
      timeline.addPropertyToInterpolate("value", bar.getValue(), (int) percentUsed);
      timeline.addPropertyToInterpolate("foreground", bar.getForeground(), newColor);
      timeline.setEase(new Sine());
      timeline.setDuration(1200);
      timeline.play();
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          bar.setString(message);
          bar.setToolTipText(toolTip);
        }
      });

      try {
        Thread.sleep(refreshTime);
      } catch (InterruptedException ignore) {
      }
    }
  }

}
