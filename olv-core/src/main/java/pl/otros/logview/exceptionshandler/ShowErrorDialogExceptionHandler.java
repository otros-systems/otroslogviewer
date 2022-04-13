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

import com.google.common.base.Throwables;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.DataConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.exceptionshandler.errrorreport.*;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;

public class ShowErrorDialogExceptionHandler extends
  AbstractSwingUncaughtExceptionHandler implements
  UncaughtExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShowErrorDialogExceptionHandler.class.getName());
  private final OtrosApplication otrosApplication;
  private JTextArea commentTextArea;
  private JCheckBox checkBoxUseProxy;
  private JTextField proxyTf;
  private SpinnerNumberModel proxyPortModel;
  private JTextField proxyUser;
  private JPasswordField proxyPasswordField;
  private JSpinner proxySpinner;
  private JLabel labelProxyHost;
  private JLabel labelProxyPort;
  private JLabel labelProxyUser;
  private JLabel labelProxyPassword;
  private final Set<String> caughtStackTraces;

  public ShowErrorDialogExceptionHandler(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
    caughtStackTraces = new HashSet<>();
  }

  @Override
  protected void uncaughtExceptionInSwingEDT(Thread thread,
                                             Throwable throwable) {
    String stackTraceAsString = Throwables.getStackTraceAsString(throwable);
    if (caughtStackTraces.contains(stackTraceAsString)) {
      LOGGER.info("Not sending the same error report twice");
      return;
    }
    caughtStackTraces.add(stackTraceAsString);
    JPanel message = new JPanel(new BorderLayout());
    message.add(new JLabel("Error in thread " + thread.getName()), BorderLayout.NORTH);

    String stackTrace = getStackTrace(throwable);
    JTextArea textArea = new JTextArea(10, 70);
    textArea.setText(stackTrace);
    textArea.setCaretPosition(0);
    message.add(new JScrollPane(textArea));

    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);

    Map<String, String> errorReportData = generateReportData(thread, throwable, otrosApplication);
    JComponent jComponent = createDialogView();
    String[] options = {"Send", "Do not send"};
    int sendReport = JOptionPane.showOptionDialog(
      otrosApplication.getApplicationJFrame(),
      jComponent,
      "Send error report confirmation",
      JOptionPane.YES_NO_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      Icons.MEGAPHONE_24,
      options, options[0]);
    errorReportData.put("USER:comment", commentTextArea.getText());

    if (sendReport == JOptionPane.YES_OPTION) {
      DataConfiguration c = otrosApplication.getConfiguration();
      c.setProperty(ConfKeys.HTTP_PROXY_USE, checkBoxUseProxy.isSelected());
      c.setProperty(ConfKeys.HTTP_PROXY_HOST, proxyTf.getText());
      c.setProperty(ConfKeys.HTTP_PROXY_PORT, proxyPortModel.getNumber().intValue());
      c.setProperty(ConfKeys.HTTP_PROXY_USER, proxyUser.getText());
      sendReportInNewBackground(errorReportData);
    } else {
      LOGGER.info("Not sending error report");
    }

    logErrorReport(errorReportData);

  }

  private void logErrorReport(Map<String, String> errorReportData) {
    LOGGER.info("Dumping information about running application. Have " + errorReportData.size() + " properties");
    TreeSet<String> keys = new TreeSet<>(errorReportData.keySet());
    for (String key : keys) {
      LOGGER.info(String.format("%s: %s", key, errorReportData.get(key)));
    }
  }

  private void sendReportInNewBackground(final Map<String, String> stringStringMap) {
    Runnable r = () -> {
      ErrorReportSender errorReportSender = new ErrorReportSender();
      if (checkBoxUseProxy.isSelected()) {
        errorReportSender.setProxy(proxyTf.getText());
        errorReportSender.setPassword(new String(proxyPasswordField.getPassword()));
        errorReportSender.setUser(proxyUser.getText());
        errorReportSender.setProxyPort(proxyPortModel.getNumber().intValue());
      }
      try {
        errorReportSender.sendReport(stringStringMap);
      } catch (IOException e) {
        LOGGER.error("Cant send error report", e);
      }
    };
    new Thread(r, "Error sending thread").start();
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
      } catch (Throwable t) {
        LOGGER.error("Error during collecting diagnostic data", t);
      }
    }
    return map;
  }

  private String getStackTrace(Throwable throwable) {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    throwable.printStackTrace(new PrintStream(bout));
    return new String(bout.toByteArray());
  }

  private JComponent createDialogView() {
    JPanel jPanel = new JPanel(new MigLayout());
    JLabel label = new JLabel("Do you want to send error report?");
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    jPanel.add(label, "span 4, wrap, center");
    jPanel.add(new JLabel("Comment:"));
    commentTextArea = new JTextArea(10, 30);
    commentTextArea.setWrapStyleWord(true);
    commentTextArea.setLineWrap(true);
    JScrollPane jScrollPane = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jPanel.add(jScrollPane, "span 3, wrap");

    jPanel.add(new JSeparator(), "span 4, wrap, grow");
    checkBoxUseProxy = new JCheckBox("Use HTTP proxy");
    proxyTf = new JTextField();
    proxyPortModel = new SpinnerNumberModel(80, 1, 256 * 256 - 1, 1);
    proxyUser = new JTextField();
    proxyPasswordField = new JPasswordField();
    proxySpinner = new JSpinner(proxyPortModel);

    jPanel.add(checkBoxUseProxy, "wrap");
    labelProxyHost = new JLabel("Proxy address");
    jPanel.add(labelProxyHost);
    jPanel.add(proxyTf, "wrap, span 3, grow");
    labelProxyPort = new JLabel("Proxy port");
    jPanel.add(labelProxyPort);
    jPanel.add(proxySpinner, "wrap");
    labelProxyUser = new JLabel("User");
    jPanel.add(labelProxyUser);
    jPanel.add(proxyUser, "grow");
    labelProxyPassword = new JLabel("Password");
    jPanel.add(labelProxyPassword);
    jPanel.add(proxyPasswordField, "grow");

    checkBoxUseProxy.addChangeListener(e -> setProxyEnabled(checkBoxUseProxy.isSelected()));
    DataConfiguration c = otrosApplication.getConfiguration();
    proxyTf.setText(c.getString(ConfKeys.HTTP_PROXY_HOST, ""));
    proxyUser.setText(c.getString(ConfKeys.HTTP_PROXY_USER, ""));
    proxyPortModel.setValue(c.getInt(ConfKeys.HTTP_PROXY_PORT, 80));
    boolean useProxy = c.getBoolean(ConfKeys.HTTP_PROXY_USE, false);
    checkBoxUseProxy.setSelected(useProxy);
    setProxyEnabled(useProxy);

    return jPanel;
  }

  private void setProxyEnabled(boolean enabled) {
    proxyTf.setEnabled(enabled);
    proxySpinner.setEnabled(enabled);
    proxyUser.setEnabled(enabled);
    proxyPasswordField.setEnabled(enabled);
    labelProxyHost.setEnabled(enabled);
    labelProxyPassword.setEnabled(enabled);
    labelProxyPort.setEnabled(enabled);
    labelProxyUser.setEnabled(enabled);
  }
}
