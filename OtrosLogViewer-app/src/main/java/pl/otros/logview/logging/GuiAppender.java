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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.gui.message.update.FormatMessageDialogWorker;
import pl.otros.logview.gui.message.update.LogDataFormatter;
import pl.otros.logview.gui.message.update.MessageDetailListener;
import pl.otros.logview.gui.message.update.MessageUpdateUtils;
import pl.otros.logview.gui.renderers.TableMarkDecoratorRenderer;
import pl.otros.logview.importer.logback.LogbackUtil;
import pl.otros.logview.store.AbstractMemoryLogStore;
import pl.otros.logview.store.CachedLogStore;
import pl.otros.logview.store.MemoryLogDataStore;
import pl.otros.logview.store.SynchronizedLogDataStore;
import pl.otros.logview.store.file.FileLogDataStore;

public class GuiAppender extends AppenderBase<ILoggingEvent> {

  private static BufferingLogDataCollectorProxy bufferingLogDataCollectorProxy;
  private static final String[] ignoreClassesList = {//
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

  public GuiAppender() {
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    if (bufferingLogDataCollectorProxy != null && !isIgnoringLogRecord(eventObject)) {
      final LogDataBuilder builder = LogbackUtil.translate(eventObject);
      builder.withFile("Olv-internal");
      bufferingLogDataCollectorProxy.add(builder.build());
    }
  }

  protected boolean isIgnoringLogRecord(ILoggingEvent event) {
    if (event.getLevel().levelInt < Level.INFO.levelInt) {
      if (event.hasCallerData()
        && event.getCallerData().length > 0) {
        for (String ignoreClass : ignoreClassesList) {
          if (StringUtils.equals(ignoreClass, event.getCallerData()[0].getClassName())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static void start(LogDataCollector dataCollector, DataConfiguration configuration) {
    bufferingLogDataCollectorProxy = new BufferingLogDataCollectorProxy(dataCollector, 5000, configuration);
  }

  public static void stopAppender() {
    bufferingLogDataCollectorProxy.stop();
    bufferingLogDataCollectorProxy = null;
  }

}
