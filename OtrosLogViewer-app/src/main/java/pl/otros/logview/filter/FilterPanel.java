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
import pl.otros.logview.api.pluginable.LogFilter;
import pl.otros.logview.api.pluginable.LogFilterValueChangeListener;

import javax.swing.*;
import java.awt.*;

public class FilterPanel extends JPanel {

  private final LogFilter logFilter;
  private final JCheckBox box;
  private final LogFilterValueChangeListener listener;

  public FilterPanel(LogFilter logFilter, LogFilterValueChangeListener listener) {
    super();
    this.setMinimumSize(new Dimension(200, 20));
    this.logFilter = logFilter;
    this.listener = listener;
    this.setLayout(new MigLayout("", "[][grow]", ""));
    this.setOpaque(true);
    logFilter.setValueChangeListener(listener);
    box = new JCheckBox(logFilter.getName());
    box.setSelected(logFilter.isEnable());
    box.addChangeListener(e -> {
      boolean selected = box.isSelected();
      // Ignore rest events like arm or isPressed
      if (selected != FilterPanel.this.logFilter.isEnable()) {
        update();
      }
    });

    this.add(box);
    this.add(new JSeparator(SwingConstants.HORIZONTAL), "growx, wrap, span");
    box.setSelected(logFilter.isEnable());

  }

  private void update() {
    boolean selected = box.isSelected();
    logFilter.setEnable(selected);

    Component gui = logFilter.getGUI();
    if (gui != null) {
      if (selected) {
        add(gui, "span, wrap, growx");
        gui.setEnabled(true);
      } else {
        remove(gui);
      }
      revalidate();
    }

    if (gui != null) {
      gui.setEnabled(box.isSelected());
    }
    listener.valueChanged();
  }

  public JCheckBox getEnableCheckBox() {
    return box;
  }

}
