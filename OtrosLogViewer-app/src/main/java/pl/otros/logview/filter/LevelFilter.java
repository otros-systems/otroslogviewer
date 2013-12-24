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
package pl.otros.logview.filter;

import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.renderers.LevelRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.logging.Level;

public class LevelFilter extends AbstractLogFilter {

  private int passLevel = Level.ALL.intValue();
  private JComboBox gui;
  private static final String NAME = "Level filter";
  private static final String DESCRIPTION = "Filtering events based on a level. It passes events with selected level or higher.";
  private static LevelRenderer renderer = new LevelRenderer();

  public LevelFilter() {
    super(NAME, DESCRIPTION);
    gui = new JComboBox(new Level[] { Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE });
    gui.setEditable(true);
    gui.setRenderer(renderer);
    gui.setOpaque(true);
    gui.setEditable(false);
    gui.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        gui.setBackground(renderer.getListCellRendererComponent(null, gui.getSelectedItem(), 0, false, false).getBackground());
        passLevel = ((Level) gui.getSelectedItem()).intValue();
        listener.valueChanged();
      }

    });
  }

  public int getPassLevel() {
    return passLevel;
  }

  public void setPassLevel(int passLevel) {
    this.passLevel = passLevel;
    if (listener != null) {
      listener.valueChanged();
    }
  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (logData.getLevel() == null)
      return true;
    return logData.getLevel().intValue() >= passLevel;
  }

  @Override
  public Component getGUI() {
    return gui;
  }

  @Override
  public void init(Properties properties, LogDataTableModel collector) {

  }

}
