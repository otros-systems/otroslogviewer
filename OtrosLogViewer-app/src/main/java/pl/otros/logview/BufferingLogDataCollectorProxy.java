/******************************************************************************
 Copyright 2011 Krzysztof Otrebski
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package pl.otros.logview;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.Stoppable;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.reader.ProxyLogDataCollector;

import javax.swing.*;

public class BufferingLogDataCollectorProxy implements LogDataCollector, Stoppable {

  private final LogDataCollector delegate;
  private ProxyLogDataCollector proxyLogDataCollector;
  private volatile boolean stop;

  public BufferingLogDataCollectorProxy(LogDataCollector delegate, final long sleepTime, Configuration configuration) {
    super();
    this.delegate = delegate;
    final DataConfiguration dataConfiguration = new DataConfiguration(configuration);
    proxyLogDataCollector = new ProxyLogDataCollector();
    Runnable r = () -> {
      while (!stop) {
        if (dataConfiguration.getBoolean(ConfKeys.TAILING_PANEL_PLAY)) {
          synchronized (BufferingLogDataCollectorProxy.this) {
            LogData[] logData = proxyLogDataCollector.getLogData();
            if (logData.length > 0) {
              proxyLogDataCollector = new ProxyLogDataCollector();
              addToDelegateInEDT(logData);
            }
          }
        }

        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException ignore) {
        }

      }

    };
    Thread t = new Thread(r, "BufferingLogDataCollectorProxy");
    t.setDaemon(true);
    t.start();
  }

  private void addToDelegateInEDT(final LogData[] logData) {
    SwingUtilities.invokeLater(() -> BufferingLogDataCollectorProxy.this.delegate.add(logData));
  }

  @Override
  public synchronized void add(LogData... logDatas) {
    proxyLogDataCollector.add(logDatas);
  }

  @Override
  public synchronized LogData[] getLogData() {
    return proxyLogDataCollector.getLogData();
  }

  @Override
  public int getCount() {
    return proxyLogDataCollector.getCount();
  }

  @Override
  public void stop() {
    stop = true;

  }

  @Override
  public int clear() {
    return delegate.clear();
  }

}
