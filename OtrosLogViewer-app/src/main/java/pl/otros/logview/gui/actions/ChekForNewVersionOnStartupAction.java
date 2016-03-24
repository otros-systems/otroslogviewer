/*
 * Copyright 2013 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.otros.logview.gui.actions;

import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.gui.GuiUtils;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChekForNewVersionOnStartupAction extends CheckForNewVersionAbstract {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckForNewVersionAction.class.getName());

  public ChekForNewVersionOnStartupAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
  }

  @Override
  protected void handleError(Exception e) {
    LOGGER.warn("Error when checking new version" + e.getMessage());

  }

  @Override
  protected void handleNewVersionIsAvailable(final String current, String running) {
    LOGGER.info(String.format("Running version is %s, current is %s", running, current));
		DataConfiguration configuration = getOtrosApplication().getConfiguration();
		String doNotNotifyThisVersion = configuration.getString(ConfKeys.VERSION_CHECK_SKIP_NOTIFICATION_FOR_VERSION, "2000-01-01");
    if (current != null && doNotNotifyThisVersion.compareTo(current) > 0) {
      return;
    }
    JPanel message = new JPanel(new GridLayout(4, 1, 4, 4));
    message.add(new JLabel(String.format("New version %s is available", current)));
    JButton button = new JButton("Open download page");
    button.addActionListener(e -> {
      try {
        Desktop.getDesktop().browse(new URI("https://sourceforge.net/projects/otroslogviewer/files/?source=app"));
      } catch (Exception e1) {
        String msg = "Can't open browser with download page: " + e1.getMessage();
        LOGGER.error(msg);
        getOtrosApplication().getStatusObserver().updateStatus(msg, StatusObserver.LEVEL_ERROR);
      }
    });
    message.add(button);

    final JCheckBox chboxDoNotNotifyMeAboutVersion = new JCheckBox("Do not notify me about version " + current);
    message.add(chboxDoNotNotifyMeAboutVersion);
    final JCheckBox chboxDoNotCheckVersionOnStart = new JCheckBox("Do not check for new version on startup");
    message.add(chboxDoNotCheckVersionOnStart);

    final JDialog dialog = new JDialog((Frame) null, "New version is available");
    dialog.getContentPane().setLayout(new BorderLayout(5, 5));
    dialog.getContentPane().add(message);

    JPanel jp = new JPanel(new FlowLayout(FlowLayout.CENTER));
    jp.add(new JButton(new AbstractAction("Ok") {

      /**
       * 
       */
      private static final long serialVersionUID = 7930093775785431184L;

      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        dialog.dispose();
        if (chboxDoNotNotifyMeAboutVersion.isSelected()) {
          LOGGER.debug("Disabling new version notificiation for " + current);
          getOtrosApplication().getConfiguration().setProperty(ConfKeys.VERSION_CHECK_SKIP_NOTIFICATION_FOR_VERSION, current);
        }
        if (chboxDoNotCheckVersionOnStart.isSelected()) {
          LOGGER.debug("Disabling new version check on start");
          getOtrosApplication().getConfiguration().setProperty(ConfKeys.VERSION_CHECK_ON_STARTUP, false);
        }
      }
    }));
    dialog.getContentPane().add(jp, BorderLayout.SOUTH);
    dialog.pack();
    dialog.setResizable(false);
    GuiUtils.centerOnScreen(dialog);
    dialog.setVisible(true);

  }

  @Override
  protected void handleVersionIsUpToDate(String current) {
    LOGGER.info("Version is up to date: " + current);

  }

}
