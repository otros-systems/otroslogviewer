/*
 * Copyright 2012 Krzysztof Otrebski
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
package pl.otros.logview.logging;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.gui.message.update.FormatMessageDialogWorker;
import pl.otros.logview.gui.message.update.LogDataFormatter;
import pl.otros.logview.gui.message.update.MessageDetailListener;
import pl.otros.logview.gui.message.update.MessageUpdateUtils;
import pl.otros.logview.gui.renderers.TableMarkDecoratorRenderer;
import pl.otros.logview.store.AbstractMemoryLogStore;
import pl.otros.logview.store.CachedLogStore;
import pl.otros.logview.store.MemoryLogDataStore;
import pl.otros.logview.store.SynchronizedLogDataStore;
import pl.otros.logview.store.file.FileLogDataStore;

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class GuiJulHandler extends Handler {

  private static BufferingLogDataCollectorProxy bufferingLogDataCollectorProxy;
  private static final String[] ignoreClassesList = new String[]{//
      MemoryLogDataStore.class.getName(),//
      FileLogDataStore.class.getName(),//
      AbstractMemoryLogStore.class.getName(),//
      CachedLogStore.class.getName(),//
      SynchronizedLogDataStore.class.getName(),//
      MessageDetailListener.class.getName(),//
      TableMarkDecoratorRenderer.class.getName(),//
      FormatMessageDialogWorker.class.getName(),//
      LogDataFormatter.class.getName(),//
      MessageUpdateUtils.class.getName()//
  };

  public GuiJulHandler() {
  }

  protected boolean isIgnoringLogRecord(LogRecord record) {
    if (record.getLevel().intValue() < Level.INFO.intValue()) {
      for (String ignoreClass : ignoreClassesList) {
        if (StringUtils.equals(ignoreClass, record.getSourceClassName())) {
          return true;
        }
      }
    }

    return false;
  }

  public static void start(LogDataCollector dataCollector, DataConfiguration configuration) {
    bufferingLogDataCollectorProxy = new BufferingLogDataCollectorProxy(dataCollector, 5000, configuration);
  }

  public static void stop() {
    bufferingLogDataCollectorProxy.stop();
    bufferingLogDataCollectorProxy = null;
  }

  @Override
  public void close() throws SecurityException {
    bufferingLogDataCollectorProxy.stop();
  }

  @Override
  public void flush() {

  }

  @Override
  public void publish(LogRecord lr) {
    if (isIgnoringLogRecord(lr)) {
      return;
    }

    LogData ld = new LogData();
    ld.setMessage(lr.getMessage());
    ld.setDate(new Date(lr.getMillis()));
    ld.setFile("Olv-internal");
    ld.setClazz(lr.getSourceClassName());
    ld.setMethod(lr.getSourceMethodName());
    ld.setThread(Integer.toString(lr.getThreadID()));
    ld.setLevel(lr.getLevel());
    if (bufferingLogDataCollectorProxy != null) {
      bufferingLogDataCollectorProxy.add(ld);
    }
  }

}
