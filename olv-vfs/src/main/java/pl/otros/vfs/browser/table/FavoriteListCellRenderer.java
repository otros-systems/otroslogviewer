/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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

package pl.otros.vfs.browser.table;

import pl.otros.vfs.browser.util.VFSUtils;
import pl.otros.vfs.browser.favorit.Favorite;

import javax.swing.*;
import java.awt.*;

/**
 */
public class FavoriteListCellRenderer extends DefaultListCellRenderer {
  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component component = super.getListCellRendererComponent(list, value, index, isSelected & cellHasFocus, cellHasFocus);
    if (component instanceof JLabel) {
      JLabel label = (JLabel) component;
      Favorite f = (Favorite) value;
      label.setText(f.getName());
      label.setIcon(VFSUtils.getIconForFileSystem(f.getUrl()));
      label.setToolTipText(VFSUtils.getFriendlyName(f.getUrl()));
    }

    return component;
  }
}
