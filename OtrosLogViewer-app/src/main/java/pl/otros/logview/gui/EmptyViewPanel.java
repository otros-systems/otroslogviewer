/*
 * Copyright 2013 Krzysztof Otrebski
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

package pl.otros.logview.gui;

import org.jdesktop.swingx.JXHyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.gui.actions.*;
import pl.otros.logview.reader.SocketLogReader;
import pl.otros.swing.OtrosSwingUtils;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.List;

class EmptyViewPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyViewPanel.class.getName());
  private final OtrosApplication otrosApplication;
  private final JAnimatedLogo jLabel;
  private List<SocketLogReader> logReaders;

  EmptyViewPanel(OtrosApplication otrosApplication, List<SocketLogReader> logReaders) {
    super();
    this.logReaders = logReaders;
    this.setName("WelcomeScreen");
    this.otrosApplication = otrosApplication;
    jLabel = new JAnimatedLogo("Welcome to OtrosLogViewer", SwingConstants.LEFT);
    jLabel.setFont(jLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    initGui();
    jLabel.start();

    this.addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(AncestorEvent event) {
        LOGGER.trace("Starting logo icon animation");
        jLabel.start();
      }

      @Override
      public void ancestorRemoved(AncestorEvent event) {
        LOGGER.trace("Stopping logo icon animation");
        jLabel.stop();
      }

      @Override
      public void ancestorMoved(AncestorEvent event) {
      }
    });
  }


  private void initGui() {
    GridBagLayout bagLayout = new GridBagLayout();
    GridBagConstraints bagConstraints = new GridBagConstraints();
    bagConstraints.anchor = GridBagConstraints.EAST;
    bagConstraints.gridwidth = 3;
    bagConstraints.ipadx = 10;
    bagConstraints.ipady = 10;
    bagConstraints.gridy = 0;
    bagConstraints.fill = GridBagConstraints.NONE;
    this.setLayout(bagLayout);

    bagConstraints.insets = new Insets(5, 0, 0, 0);

    bagConstraints.insets = new Insets(2, 5, 1, 5);
    bagConstraints.anchor = GridBagConstraints.CENTER;
    bagConstraints.gridy++;

    this.add(jLabel, bagConstraints);
    bagConstraints.fill = GridBagConstraints.HORIZONTAL;
    bagConstraints.gridy++;

    final Action tailAction = new TailLogWithAutoDetectActionListener(otrosApplication);
    final JButton tailButton = new JButton(tailAction);
    tailButton.setName("Open log files");
    OtrosSwingUtils.fontSize2(tailButton);
    tailButton.setIcon(Icons.FOLDER_OPEN);
    this.add(tailButton, bagConstraints);
    bagConstraints.gridy++;

    final AdvanceOpenAction advanceOpenAction = new AdvanceOpenAction(otrosApplication);
    final JButton advanceOpenButton = new JButton(advanceOpenAction);
    advanceOpenButton.setName("Merge log files");
    OtrosSwingUtils.fontSize2(advanceOpenButton);
    advanceOpenButton.setIcon(Icons.ARROW_JOIN_24);
    this.add(advanceOpenButton, bagConstraints);
    bagConstraints.gridy++;

    OpenLogInvestigationAction openLogInvestigationAction = new OpenLogInvestigationAction(otrosApplication);
    JButton jb2 = new JButton("Open log investigation", Icons.IMPORT_24);
    OtrosSwingUtils.fontSize2(jb2);
    jb2.addActionListener(openLogInvestigationAction);
    this.add(jb2, bagConstraints);
    bagConstraints.gridy++;


    this.add(OtrosSwingUtils.fontSize2(new JButton(new ParseClipboard(otrosApplication))), bagConstraints);
    bagConstraints.gridy++;

    this.add(OtrosSwingUtils.fontSize2(new JButton(new StartSocketListener(otrosApplication, logReaders))), bagConstraints);
    bagConstraints.gridy++;

    bagConstraints.insets = new Insets(20, 5, 20, 5);
    this.add(new JSeparator(SwingConstants.HORIZONTAL), bagConstraints);
    bagConstraints.insets = new Insets(2, 5, 0, 5);
    bagConstraints.gridy++;

    final JButton convertPatterButton = OtrosSwingUtils.fontSize2(new JButton(new ConvertLogbackLog4jPatternAction(otrosApplication)));
    this.add(convertPatterButton, bagConstraints);
    bagConstraints.gridy++;

    bagConstraints.gridy++;
    JTextArea visitTf = new JTextArea(
      "Have a different log format? Go to https://github.com/otros-systems/otroslogviewer/wiki/Log4jPatternLayout\nto check how to create a log parser based on the log4j PatternLayout.");
    visitTf.setEditable(false);
    visitTf.setBackground(new JLabel().getBackground());
    visitTf.setBorder(null);
    bagConstraints.gridwidth = 2;
    bagConstraints.gridx = 0;
    this.add(visitTf, bagConstraints);

    GoToDonatePageAction goToDonatePageAction = new GoToDonatePageAction(otrosApplication);
    JXHyperlink jxHyperlink = new JXHyperlink(goToDonatePageAction);
    bagConstraints.gridy++;
    bagConstraints.gridwidth = 2;
    bagConstraints.anchor = GridBagConstraints.EAST;
    bagConstraints.fill = GridBagConstraints.NONE;

    this.add(jxHyperlink, bagConstraints);
  }

}
