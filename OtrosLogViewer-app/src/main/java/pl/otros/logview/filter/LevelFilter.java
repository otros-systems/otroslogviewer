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
package pl.otros.logview.filter;

import net.miginfocom.swing.MigLayout;
import pl.otros.logview.RenamedLevel;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.LogFilterValueChangeListener;
import pl.otros.logview.gui.renderers.LevelRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;
import java.util.logging.Level;

public class LevelFilter extends AbstractLogFilter {
  private int passLevel = Level.ALL.intValue();
  //  private final JComboBox levelJCombo;
  private static final String NAME = "Level filter";
  private static final String DESCRIPTION = "Filtering events based on a level. It passes events with selected level or higher.";
  private static final LevelRenderer renderer = new LevelRenderer();
  private final JPanel gui;
  private FilterMode filterMode = FilterMode.HIGHER_OR_EQUAL;

  private enum FilterMode {
    LOWER_OR_EQUAL("Lower or equal (<=)"),
    EQUAL("Level equal (==)"),
    HIGHER_OR_EQUAL("Higher or equal (>=)");
    private final String toDisplay;

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
    Level[] levels = {
      RenamedLevel.FINEST_TRACE,
      RenamedLevel.FINER,
      RenamedLevel.FINE_DEBUG,
      RenamedLevel.CONFIG,
      RenamedLevel.INFO,
      RenamedLevel.WARNING_WARN,
      RenamedLevel.SEVERE_ERROR_FATAL
    };
    JComboBox<Level> levelJCombo = new JComboBox<>(levels);
    levelJCombo.setRenderer(renderer);
    levelJCombo.setOpaque(true);
    levelJCombo.setEditable(false);

    JComboBox modeJCombo = new JComboBox(new FilterMode[]{FilterMode.LOWER_OR_EQUAL, FilterMode.EQUAL, FilterMode.HIGHER_OR_EQUAL});
    modeJCombo.setSelectedItem(filterMode);
    modeJCombo.setEditable(false);

    ItemListener itemListener = e -> {
      System.out.println(e.getStateChange());
      if ((e.getStateChange() == ItemEvent.SELECTED)) {
        levelJCombo.setBackground(renderer.getListCellRendererComponent(null, levelJCombo.getSelectedItem(), 0, false, false).getBackground());
        passLevel = ((Level) levelJCombo.getSelectedItem()).intValue();
        filterMode = (FilterMode) modeJCombo.getSelectedItem();
        listener.ifPresent(LogFilterValueChangeListener::valueChanged);
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
    gui.add(levelLabel, "wrap, growx");
    gui.add(levelJCombo, "right, wrap, growx");
    gui.add(modeLabel, "wrap, growx");
    gui.add(modeJCombo, "right, wrap, growx");

  }

  public void setPassLevel(int passLevel) {
    this.passLevel = passLevel;
    if (listener != null) {
      listener.ifPresent(LogFilterValueChangeListener::valueChanged);
    }
  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (logData.getLevel() == null) {
      return true;
    }
    switch (filterMode) {
      case LOWER_OR_EQUAL:
        return logData.getLevel().intValue() <= passLevel;
      case EQUAL:
        return logData.getLevel().intValue() == passLevel;
      case HIGHER_OR_EQUAL:
        return logData.getLevel().intValue() >= passLevel;
      default:
        return false;
    }
  }

  @Override
  public Component getGUI() {
    return gui;
  }

  @Override
  public void init(Properties properties, LogDataTableModel collector) {
  }
}
