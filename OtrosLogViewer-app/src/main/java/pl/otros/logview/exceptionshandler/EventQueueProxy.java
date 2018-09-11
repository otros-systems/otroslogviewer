/*
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
 */

package pl.otros.logview.exceptionshandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class EventQueueProxy extends EventQueue {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventQueueProxy.class.getName());

  @Override
  protected void dispatchEvent(AWTEvent arg0) {
    try {
      super.dispatchEvent(arg0);
    } catch (NullPointerException e) {
      /* On multi monitor environment the java.awt.Component#findUnderMouseInWindow method can throw a NullPointerException.
       * See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6840067
       */
      LOGGER.warn("NullPointerException on dispatchEvent " + arg0 + ". Possible awt bug.", e);
    } catch (Exception e) {
      LOGGER.error("Uncaught exception was thrown", e);
      Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
    }
  }

}
