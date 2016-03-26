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

import pl.otros.logview.gui.ColorIcon;
import pl.otros.logview.api.pluginable.AutomaticMarker;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class AutomaticMarkerRenderer extends DefaultListCellRenderer implements TableCellRenderer {

  private static final JList list = new JList();

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    AutomaticMarker marker = (AutomaticMarker) value;
    listCellRendererComponent.setIcon(new ColorIcon(marker.getColors().getBackground(), marker.getColors().getForeground()));

    listCellRendererComponent.setText(marker.getName());
    return listCellRendererComponent;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return getListCellRendererComponent(list, value, row, isSelected, hasFocus);
  }

}
