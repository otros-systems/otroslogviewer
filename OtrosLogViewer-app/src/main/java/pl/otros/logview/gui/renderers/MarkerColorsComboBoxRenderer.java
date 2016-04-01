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
package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.gui.ColorIcon;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class MarkerColorsComboBoxRenderer extends BasicComboBoxRenderer {

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel component2 = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    MarkerColors selected = (MarkerColors) value;
    component2.setIcon(new ColorIcon(selected.getBackground(), selected.getForeground(), 16, 16));
    component2.setText(selected.name());
    return component2;
  }
}
