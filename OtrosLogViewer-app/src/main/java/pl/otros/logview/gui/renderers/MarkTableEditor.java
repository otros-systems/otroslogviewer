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
package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.model.MarkerColors;

import javax.swing.*;
import java.awt.*;

public class MarkTableEditor extends DefaultCellEditor {

  private final JCheckBox checkBox;
  private final OtrosApplication otrosApplication;

  public MarkTableEditor(OtrosApplication otrosApplication) {
    super(new JCheckBox());
    checkBox = (JCheckBox) getComponent();
    this.otrosApplication = otrosApplication;
  }

  @Override
  public Object getCellEditorValue() {
    if (checkBox.isSelected()) {
      return otrosApplication.getSelectedMarkColors();
    }
    return null;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    MarkerColors markerColors = (MarkerColors) value;
    checkBox.setSelected(null != markerColors);
    return c;
  }

}
