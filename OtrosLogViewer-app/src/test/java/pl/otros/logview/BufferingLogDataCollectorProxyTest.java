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
package pl.otros.logview;

import org.apache.commons.configuration.BaseConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.reader.ProxyLogDataCollector;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class BufferingLogDataCollectorProxyTest {

  BaseConfiguration configuration;
  BufferingLogDataCollectorProxy bufferingLogDataCollectorProxy;
  long sleepTime = 100;
  ProxyLogDataCollector delegate;

  @Before
  public void initTest() throws InterruptedException, InvocationTargetException {
    configuration = new BaseConfiguration();
    configuration.setProperty(ConfKeys.TAILING_PANEL_PLAY, true);

    delegate = new ProxyLogDataCollector();
    bufferingLogDataCollectorProxy = new BufferingLogDataCollectorProxy(delegate, sleepTime, configuration);
    // Initialize swing thread
    SwingUtilities.invokeAndWait(new Runnable() {

      @Override
      public void run() {

      }
    });
  }

  @Test
  public void testAddLogDataArray() throws InterruptedException {
    LogData data1 = new LogData();
    data1.setId(1);
    LogData data2 = new LogData();
    data2.setId(2);
    LogData[] toAdd = new LogData[] { data1, data2 };
    bufferingLogDataCollectorProxy.add(toAdd);

    Assert.assertArrayEquals(toAdd, bufferingLogDataCollectorProxy.getLogData());
    Thread.sleep(2 * sleepTime);
    Assert.assertArrayEquals(toAdd, delegate.getLogData());

  }

  @Test
  public void testAddLogData() throws Exception {
    LogData data = new LogData();
    Assert.assertEquals(0, delegate.getLogData().length);
    bufferingLogDataCollectorProxy.add(data);
    Thread.sleep(sleepTime * 2);
    Assert.assertEquals(1, delegate.getLogData().length);
    bufferingLogDataCollectorProxy.add(data);
    bufferingLogDataCollectorProxy.add(data);
    Thread.sleep(sleepTime * 2);
    Assert.assertEquals(3, delegate.getLogData().length);

  }

  @Test
  public void testGetLogData() {
    LogData data1 = new LogData();
    data1.setId(1);
    LogData data2 = new LogData();
    data2.setId(2);
    Assert.assertEquals(0, delegate.getLogData().length);
    bufferingLogDataCollectorProxy.add(data1);
    bufferingLogDataCollectorProxy.add(data2);
    LogData[] logData = bufferingLogDataCollectorProxy.getLogData();
    Assert.assertEquals(2, logData.length);
    Assert.assertArrayEquals(new LogData[] { data1, data2 }, logData);

  }

  @Test
  public void testStop() throws InterruptedException {
    LogData data = new LogData();
    Assert.assertEquals(0, delegate.getLogData().length);
    bufferingLogDataCollectorProxy.add(data);
    Thread.sleep(sleepTime * 2);

    Assert.assertEquals(1, delegate.getLogData().length);
    bufferingLogDataCollectorProxy.stop();
    bufferingLogDataCollectorProxy.add(data);
    bufferingLogDataCollectorProxy.add(data);
    Thread.sleep(sleepTime * 2);
    Assert.assertEquals(1, delegate.getLogData().length);
    Assert.assertEquals(2, bufferingLogDataCollectorProxy.getLogData().length);

  }

}
