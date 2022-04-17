/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.updater.VersionUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;

import static pl.otros.logview.api.ConfKeys.*;

public abstract class CheckForNewVersionAbstract extends OtrosAction {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckForNewVersionAbstract.class.getName());
  private final VersionUtil versionUtil = new VersionUtil();

  private SwingWorker<Optional<String>, Void> versionChecker = new SwingWorker<Optional<String>, Void>() {
    @Override
    protected Optional<String> doInBackground() {
      String running;
      Optional<String> current = Optional.empty();
      final DataConfiguration c = getOtrosApplication().getConfiguration();

      Proxy proxy = Proxy.NO_PROXY;
      if (c.getBoolean(HTTP_PROXY_USE, false)) {
        Proxy.Type proxyType = Proxy.Type.HTTP;
        final InetSocketAddress proxySocketAddress = new InetSocketAddress(c.getString(HTTP_PROXY_HOST, ""), c.getInt(HTTP_PROXY_PORT, 80));
        proxy = new Proxy(proxyType, proxySocketAddress);
      }
      try {
        LOGGER.info("Checking current and running versions");
        current = versionUtil.getCurrentVersion(proxy);
      } catch (FileNotFoundException e) {
        LOGGER.error("No version Info added to release page! Cannot check version.");
      } catch (Exception e) {
        LOGGER.error("Error checking version: " + e.getMessage());
      }
      return current;
    }

    @Override
    protected void done() {
      try {
        Optional<String> maybeCurrent = get();
        String running = versionUtil.getRunningVersion();
        maybeCurrent.ifPresent(current -> {
          if (StringUtils.isNotBlank(running)) {
            if (current.compareTo(running) > 0) {
              handleNewVersionIsAvailable(current, running);
            } else {
              handleVersionIsUpToDate(current);
            }
          } else {
            LOGGER.warn(String.format("Current version is %s, running version is %s", current, running));
          }
        });
        if (!maybeCurrent.isPresent()){
          handleError(new Exception("Unable to check latest version"));
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
  protected void actionPerformedHook(ActionEvent arg0) {
    versionChecker.execute();
  }

  protected abstract void handleError(Exception e);

  protected abstract void handleNewVersionIsAvailable(String current, String running);

  protected abstract void handleVersionIsUpToDate(String current);
}
