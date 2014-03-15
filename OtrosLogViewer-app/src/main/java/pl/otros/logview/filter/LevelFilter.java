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

import net.miginfocom.swing.MigLayout;
import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.renderers.LevelRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;
import java.util.logging.Level;

public class LevelFilter extends AbstractLogFilter {
  private int passLevel = Level.ALL.intValue();
  private JComboBox levelJCombo;
  private JComboBox modeJCombo;
  private static final String NAME = "Level filter";
  private static final String DESCRIPTION = "Filtering events based on a level. It passes events with selected level or higher.";
  private static LevelRenderer renderer = new LevelRenderer();
  private JPanel gui;
  private FilterMode filterMode = FilterMode.HIGHER_OR_EQUAL;

  private enum FilterMode {
    LOWER_OR_EQUAL("Lower or equal (<=)"),
    EQUAL("Level equal (==)"),
    HIGHER_OR_EQUAL("Higher or equal (>=)");
    private String toDisplay;

    FilterMode(String toDisplay) {
      this.toDisplay = toDisplay;
    }

    @Override
    public String toString() {
      return toDisplay;
    }
  }

  public LevelFilter() {
    super(NAME, DESCRIPTION);
    levelJCombo = new JComboBox(new Level[]{Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE});
    levelJCombo.setRenderer(renderer);
    levelJCombo.setOpaque(true);
    levelJCombo.setEditable(false);

    modeJCombo = new JComboBox(new FilterMode[]{FilterMode.LOWER_OR_EQUAL, FilterMode.EQUAL, FilterMode.HIGHER_OR_EQUAL});
    modeJCombo.setSelectedItem(filterMode);
    modeJCombo.setEditable(false);

    ItemListener itemListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        System.out.println(e.getStateChange());
        if ((e.getStateChange() == ItemEvent.SELECTED)) {
          levelJCombo.setBackground(renderer.getListCellRendererComponent(null, levelJCombo.getSelectedItem(), 0, false, false).getBackground());
          passLevel = ((Level) levelJCombo.getSelectedItem()).intValue();
          filterMode = (FilterMode) modeJCombo.getSelectedItem();
          listener.valueChanged();
        }
      }
    };

    levelJCombo.addItemListener(itemListener);
    modeJCombo.addItemListener(itemListener);

    JLabel levelLabel = new JLabel("Level:");
    levelLabel.setDisplayedMnemonic('l');
    levelLabel.setLabelFor(levelJCombo);
    JLabel modeLabel = new JLabel("Mode:");
    modeLabel.setDisplayedMnemonic('m');
    modeLabel.setLabelFor(modeJCombo);

    gui = new JPanel(new MigLayout());
    gui.add(levelLabel,"wrap, growx");
    gui.add(levelJCombo,"right, wrap, growx");
    gui.add(modeLabel,"wrap, growx");
    gui.add(modeJCombo,"right, wrap, growx");

  }

  public void setPassLevel(int passLevel) {
    this.passLevel = passLevel;
    if (listener != null) {
      listener.valueChanged();
    }
  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (logData.getLevel() == null) {
      return true;
    }
    switch (filterMode){
      case LOWER_OR_EQUAL:
        return logData.getLevel().intValue() <= passLevel;
      case EQUAL:
        return logData.getLevel().intValue() == passLevel;
      case HIGHER_OR_EQUAL:
        return logData.getLevel().intValue() >= passLevel;
    }
    return false;
  }

  @Override
  public Component getGUI() {
    return gui;
  }

  @Override
  public void init(Properties properties, LogDataTableModel collector) {
  }
}
