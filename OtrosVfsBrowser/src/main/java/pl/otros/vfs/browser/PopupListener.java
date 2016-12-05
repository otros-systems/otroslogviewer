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

package pl.otros.vfs.browser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class PopupListener extends MouseAdapter implements KeyListener {

  private JPopupMenu popupMenu;

  public PopupListener(JPopupMenu popupMenu) {
    super();
    this.popupMenu = popupMenu;
  }

  public void mousePressed(MouseEvent e) {
    checkPopup(e);
  }

  public void mouseClicked(MouseEvent e) {
    checkPopup(e);
  }

  public void mouseReleased(MouseEvent e) {
    checkPopup(e);
  }

  private void checkPopup(MouseEvent e) {
    if (e.isPopupTrigger()) {
      show((Component) e.getSource(), e.getX(), e.getY());
    }
  }


  public void show(Component invoker, int x, int y) {
    popupMenu.show(invoker, x, y);
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    Point p = new Point(e.getComponent().getLocation());

    if (e.getKeyCode() == 525) {
      if (e.getComponent() instanceof JTable) {
        JTable table = (JTable) e.getComponent();
        int selectedRow = table.getSelectedRow();
        Rectangle cellRect = table.getCellRect(selectedRow, 0, true);
        p.setLocation(cellRect.getCenterX(), cellRect.getCenterY());
      } else if (e.getComponent() instanceof JList) {
        JList list = (JList) e.getComponent();
        int selectedIndex = list.getSelectedIndex();
        Rectangle cellRect = list.getCellBounds(selectedIndex, selectedIndex);
        p.setLocation(cellRect.getCenterX(), cellRect.getCenterY());
      }
      show(e.getComponent(), (int) p.getX(), (int) p.getY());
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }
}