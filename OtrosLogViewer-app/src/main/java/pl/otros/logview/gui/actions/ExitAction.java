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

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ExitAction extends OtrosAction implements WindowListener {

  private final JFrame frame;

  public ExitAction(OtrosApplication otrosApplication) {
    super("Quit", otrosApplication);
    this.frame = otrosApplication.getApplicationJFrame();
  }


  @Override
  protected void actionPerformedHook(ActionEvent e) {
    askAndExit();
  }

  @Override
  public void windowClosing(WindowEvent e) {
    askAndExit();
  }

  protected void askAndExit() {
    final DataConfiguration configuration = getOtrosApplication().getConfiguration();
    boolean doConfirm = configuration.getBoolean(ConfKeys.CONFIRM_QUIT, true);
    JPanel panel = new JPanel(new MigLayout("left"));
    panel.add(new JLabel("Do you want to exit OtrosLogViewer and parse logs with 'grep'?"), "growx, wrap");
    getOtrosApplication().getConfiguration().getBoolean(ConfKeys.CONFIRM_QUIT, true);
    final JCheckBox box = new JCheckBox("Always ask before exit", doConfirm);
    box.addActionListener(e -> configuration.setProperty(ConfKeys.CONFIRM_QUIT, box.isSelected()));
    panel.add(box, "growx, wrap");

    if (!doConfirm || JOptionPane.showConfirmDialog(frame, panel, "Are you sure?", JOptionPane.YES_NO_OPTION)
      == JOptionPane.YES_OPTION) {
      frame.setVisible(false);
      frame.dispose();
      System.exit(0);
    }
  }

  @Override
  public void windowOpened(WindowEvent e) {
    //nothing
  }

  @Override
  public void windowClosed(WindowEvent e) {
//nothing
  }

  @Override
  public void windowIconified(WindowEvent e) {
//nothing
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
//nothing
  }

  @Override
  public void windowActivated(WindowEvent e) {
//nothing
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
//nothing
  }

}
