/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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
package pl.otros.logview.gui.actions;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.VersionUtil;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.OtrosApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static pl.otros.logview.api.ConfKeys.*;

public abstract class CheckForNewVersionAbstract extends OtrosAction {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckForNewVersionAction.class.getName());
  private SwingWorker<String, String> versionChecker = new SwingWorker<String, String>() {
    @Override
    protected String doInBackground() throws Exception {
      String running;
      String current = null;
      final DataConfiguration c = getOtrosApplication().getConfiguration();

      Proxy proxy = Proxy.NO_PROXY;
      if (c.getBoolean(HTTP_PROXY_USE, false)) {
        Proxy.Type proxyType = Proxy.Type.HTTP;
        final InetSocketAddress proxySocketAddress = new InetSocketAddress(c.getString(HTTP_PROXY_HOST, ""), c.getInt(HTTP_PROXY_PORT, 80));
        proxy = new Proxy(proxyType, proxySocketAddress);
      }
      try {
        running = VersionUtil.getRunningVersion();
        current = VersionUtil.getCurrentVersion(running, proxy, getOtrosApplication());
      } catch (Exception e) {
        LOGGER.error("Error checking version: " + e.getMessage());
      }
      return current;
    }

    @Override
    protected void done() {
      try {
        String current = get();
        String running = VersionUtil.getRunningVersion();
        if (current != null && StringUtils.isNotBlank(running)) {
          if (current.compareTo(running) > 0) {
            handleNewVersionIsAvailable(current, running);
          } else {
            handleVersionIsUpToDate(current);
          }
        } else {
          LOGGER.warn(String.format("Current version is %s, running version is %s", current, running));
        }
      } catch (Exception e) {
        handleError(e);
      }
    }
  };

  public CheckForNewVersionAbstract(OtrosApplication otrosApplication) {
    super(otrosApplication);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    versionChecker.execute();
  }

  protected abstract void handleError(Exception e);

  protected abstract void handleNewVersionIsAvailable(String current, String running);

  protected abstract void handleVersionIsUpToDate(String current);
}
