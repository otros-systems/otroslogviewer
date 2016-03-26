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
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.pluginable.PluginableElementEventListener;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.logview.gui.actions.*;
import pl.otros.logview.gui.actions.read.ImportLogWithAutoDetectedImporterActionListener;
import pl.otros.logview.gui.actions.read.ImportLogWithGivenImporterActionListener;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.TreeSet;

public class EmptyViewPanel extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyViewPanel.class.getName());
  private final OtrosApplication otrosApplication;
  private final JAnimatedLogo jLabel;

  public EmptyViewPanel(OtrosApplication otrosApplication) {
    super();
    this.otrosApplication = otrosApplication;
    jLabel = new JAnimatedLogo("Welcome to OtrosLogViewer", SwingConstants.LEFT);
    jLabel.setFont(jLabel.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    initGui();

    PluginableElementsContainer<LogImporter> logImportersContainer = otrosApplication.getAllPluginables().getLogImportersContainer();
    logImportersContainer.addListener(new PluginableElementEventListener<LogImporter>() {
      @Override
      public void elementAdded(LogImporter element) {
        LOGGER.debug("Plugins updated, updating GUI");
        initIntEDT();
      }

      @Override
      public void elementRemoved(LogImporter element) {
        LOGGER.debug("Plugins updated, updating GUI");
        initIntEDT();
      }

      @Override
      public void elementChanged(LogImporter element) {
        LOGGER.debug("Plugins updated, updating GUI");
        initIntEDT();
      }

      private void initIntEDT() {
        GuiUtils.runLaterInEdt(EmptyViewPanel.this::initGui);
      }
    });
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
    this.removeAll();
    TreeSet<LogImporter> logImportersList = new TreeSet<>((o1, o2) -> {
      return o1.getName().compareTo(o2.getName());
    });
    logImportersList.addAll(otrosApplication.getAllPluginables().getLogImportersContainer().getElements());
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
    ImportLogWithAutoDetectedImporterActionListener autoDetectActionListener = new ImportLogWithAutoDetectedImporterActionListener(otrosApplication);
    JButton jb = new JButton("Open log with type autodetection", Icons.WIZARD);
    jb.addActionListener(autoDetectActionListener);
    this.add(jb, bagConstraints);
    bagConstraints.gridy++;

    TailLogWithAutoDetectActionListener tailLogActionListener = new TailLogWithAutoDetectActionListener(otrosApplication);
    JButton jbTailAutoDetect = new JButton("Tail log with type autodetection", Icons.ARROW_REPEAT);
    jbTailAutoDetect.addActionListener(tailLogActionListener);
    this.add(jbTailAutoDetect, bagConstraints);
    bagConstraints.gridy++;

    this.add(new JButton(new TailMultipleFilesIntoOneView(otrosApplication)), bagConstraints);
    bagConstraints.gridy++;

    OpenLogInvestigationAction openLogInvestigationAction = new OpenLogInvestigationAction(otrosApplication);
    JButton jb2 = new JButton("Open log investigation", Icons.IMPORT_24);
    jb2.addActionListener(openLogInvestigationAction);
    this.add(jb2, bagConstraints);
    bagConstraints.gridy++;

    bagConstraints.insets = new Insets(15, 15, 3, 15);
    this.add(new JSeparator(SwingConstants.HORIZONTAL), bagConstraints);
    bagConstraints.insets = new Insets(2, 5, 0, 5);
    bagConstraints.gridy++;

    int startY = bagConstraints.gridy;
    bagConstraints.gridwidth = 1;

    JLabel openLabel = new JLabel("Open log", Icons.FOLDER_OPEN, SwingConstants.CENTER);
    this.add(openLabel, bagConstraints);
    bagConstraints.gridy++;
    for (LogImporter logImporter : logImportersList) {
      ImportLogWithGivenImporterActionListener importLogActionListener = new ImportLogWithGivenImporterActionListener(otrosApplication, logImporter);
      JButton b = new JButton("Open " + logImporter.getName(), logImporter.getIcon());
      b.setHorizontalAlignment(SwingConstants.LEFT);
      b.addActionListener(importLogActionListener);
      this.add(b, bagConstraints);
      bagConstraints.gridy++;
    }

    bagConstraints.gridy = startY;
    JLabel tailLabel = new JLabel("Tail log [from begging of file]", Icons.ARROW_REPEAT, SwingConstants.CENTER);
    this.add(tailLabel, bagConstraints);
    bagConstraints.gridy++;
    for (LogImporter logImporter : logImportersList) {
      TailLogActionListener importLogActionListener = new TailLogActionListener(otrosApplication, logImporter);
      JButton b = new JButton("Tail " + logImporter.getName(), logImporter.getIcon());
      b.addActionListener(importLogActionListener);
      b.setHorizontalAlignment(SwingConstants.LEFT);
      this.add(b, bagConstraints);
      bagConstraints.gridy++;
    }

    JTextArea visitTf = new JTextArea(
      "Have a different log format? Go to https://github.com/otros-systems/otroslogviewer/wiki/Log4jPatternLayout\nto check how to create a log parser based on the log4j PatternLayout.");
    visitTf.setEditable(false);
    visitTf.setBackground(tailLabel.getBackground());
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
