/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.VersionUtil;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.JAnimatedLogo;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class AboutAction extends OtrosAction {

  private String build = "?";
  private static final Logger LOGGER = LoggerFactory.getLogger(AboutAction.class.getName());
  private final JTextPane textArea;

  private Style mainStyle = null;
  private Style licenceStyle = null;
  private Style titleStyle = null;
  private final JAnimatedLogo animatedLogo;
  private final JPanel panel;

  public AboutAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
    this.putValue(SMALL_ICON, Icons.LOGO_OTROS_16);
    try {
      build = VersionUtil.getRunningVersion();
    } catch (IOException e) {
      LOGGER.error("Problem with checking running version: " + e.getMessage());
    }

    animatedLogo = new JAnimatedLogo();
    textArea = new JTextPane();
    textArea.setText("");
    textArea.setEditable(false);
    textArea.setBackground(new JLabel().getBackground());

    StyleContext sc = new StyleContext();
    Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
    mainStyle = sc.addStyle("MainStyle", defaultStyle);
    StyleConstants.setFontSize(mainStyle, 12);
    StyleConstants.setForeground(mainStyle, Color.BLACK);

    licenceStyle = sc.addStyle("classMethod", null);
    StyleConstants.setFontFamily(licenceStyle, "monospaced");
    StyleConstants.setForeground(licenceStyle, Color.BLUE);
    titleStyle = sc.addStyle("note", mainStyle);
    StyleConstants.setFontFamily(titleStyle, "arial");
    StyleConstants.setBold(titleStyle, true);
    StyleConstants.setFontSize(titleStyle, 20);

    try {
      fillText();
    } catch (BadLocationException e) {
      LOGGER.error("Bad location when filling text.", e);
    }
    panel = new JPanel(new FlowLayout());
    panel.add(animatedLogo);
    panel.add(textArea);
  }

  private void fillText() throws BadLocationException {
    textArea.setText("");
    StyledDocument sd = textArea.getStyledDocument();
    sd.insertString(0, "OtrosLogViewer\n", titleStyle);
    sd.insertString(sd.getLength(), "Build: " + build + "\n", mainStyle);
    sd.insertString(sd.getLength(), "Project web page: http://code.google.com/p/otroslogviewer/\n", mainStyle);
    sd.insertString(sd.getLength(), "Program documentation: https://github.com/otros-systems/otroslogviewer/wiki/Introduction?tm=6 \n", mainStyle);
    sd.insertString(sd.getLength(), "License: Apache Commons 2.0", licenceStyle);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    animatedLogo.start();
    OtrosApplication otrosApplication = getOtrosApplication();
    JFrame applicationJFrame = otrosApplication.getApplicationJFrame();
    JOptionPane.showMessageDialog(applicationJFrame, panel, "About", JOptionPane.INFORMATION_MESSAGE, new EmptyIcon());
    animatedLogo.stop();

  }

}
