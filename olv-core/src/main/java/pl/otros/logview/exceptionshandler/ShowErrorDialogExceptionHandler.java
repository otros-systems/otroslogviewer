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
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.exceptionshandler.errrorreport.*;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class ShowErrorDialogExceptionHandler extends
  AbstractSwingUncaughtExceptionHandler implements
  UncaughtExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShowErrorDialogExceptionHandler.class.getName());
  private final OtrosApplication otrosApplication;

  public ShowErrorDialogExceptionHandler(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
  }

  @Override
  protected void uncaughtExceptionInSwingEDT(Thread thread,
                                             Throwable throwable) {
    JPanel message = new JPanel(new BorderLayout());
    message.add(new JLabel("Error in thread " + thread.getName()), BorderLayout.NORTH);

    String stackTrace = getStackTrace(throwable);
    JTextArea textArea = new JTextArea(10, 70);
    textArea.setText(stackTrace);
    textArea.setCaretPosition(0);
    message.add(new JScrollPane(textArea));

    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);

    Map<String, String> errorReportData = generateReportData(thread, throwable, otrosApplication);
    logErrorReport(errorReportData);

  }

  private void logErrorReport(Map<String, String> errorReportData) {
    LOGGER.info("Dumping information about running application. Have {} properties", errorReportData.size());
    TreeSet<String> keys = new TreeSet<>(errorReportData.keySet());
    for (String key : keys) {
      LOGGER.info("{}: {}", key, errorReportData.get(key));
    }
  }


  private Map<String, String> generateReportData(Thread thread,
                                                 Throwable throwable, OtrosApplication otrosApplication) {
    ErrorReportCollectingContext ctx = new ErrorReportCollectingContext();
    ctx.setThread(thread);
    ctx.setThrowable(throwable);
    ctx.setOtrosApplication(otrosApplication);
    ArrayList<ErrorReportDataCollector> collectors = new ArrayList<>();
    collectors.add(new RuntimeInfoERDC());
    collectors.add(new SystemPropertiesERDC());
    collectors.add(new DesktopERDC());
    collectors.add(new ExceptionERDC());
    collectors.add(new OtrosAppERDC());
    collectors.add(new UuidERDC());

    HashMap<String, String> map = new HashMap<>();
    for (ErrorReportDataCollector errorReportDataCollector : collectors) {
      try {
        map.putAll(errorReportDataCollector.collect(ctx));
      } catch (RuntimeException t) {
        LOGGER.error("Error during collecting diagnostic data", t);
      }
    }
    return map;
  }

  private String getStackTrace(Throwable throwable) {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    throwable.printStackTrace(new PrintStream(bout));
    return bout.toString();
  }

}
