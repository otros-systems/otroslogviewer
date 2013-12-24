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
package pl.otros.logview.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TabHeader extends JPanel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static final Icon NORMAL_ICON = Icons.TAB_HEADER_NORMAL;
  private static final Icon HOVER_ICON = Icons.TAB_HEADER_HOVER;
  private JTabbedPane tabbedpane;

  private JButton iconButton;
  private JLabel label;
  private JTextField editor;
  private Component tabComponent;
  private int editing_idx;
  private int len;
  private Dimension dim;

  public TabHeader(JTabbedPane pane, String name, String tooltip) {
    this(pane, name, null, tooltip);
  }

  public TabHeader(JTabbedPane pane, String name, Icon icon, String tooltip) {
    super(new FlowLayout(FlowLayout.LEFT, 3, 0));
    this.tabbedpane = pane;

    this.tabbedpane = pane;
    iconButton = new JButton(NORMAL_ICON);
    iconButton.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent e) {
        iconButton.setIcon(HOVER_ICON);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        iconButton.setIcon(NORMAL_ICON);
      }

    });
    iconButton.setToolTipText("Close tab");
    iconButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        closeTab();
      }
    });
    label = new JLabel(name, icon, SwingConstants.LEFT);
    label.setToolTipText(tooltip);
    label.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
          TabHeader.this.tabbedpane.setSelectedIndex(TabHeader.this.tabbedpane.indexOfTabComponent(TabHeader.this));
        }
        Rectangle rect = tabbedpane.getUI().getTabBounds(tabbedpane, tabbedpane.getSelectedIndex());
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          startEditing();
        } else if (rect != null && !rect.contains(e.getPoint())) {
          renameTabTitle();
        }
      }

    });

    Dimension d = new Dimension(16, 16);
    iconButton.setPreferredSize(d);
    iconButton.setMinimumSize(d);
    iconButton.setMaximumSize(d);
    iconButton.setBorderPainted(false);
    add(label);
    add(iconButton);
    setOpaque(false);
    label.setOpaque(false);
    iconButton.setOpaque(false);

    editor = new JTextField();
    editor.setBorder(BorderFactory.createLineBorder(editor.getForeground()));
    editor.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        renameTabTitle();
      }
    });

    editor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          renameTabTitle();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          cancelEditing();
        } else {
          editor.setPreferredSize(editor.getText().length() > len ? null
              : dim);
          tabbedpane.revalidate();
        }
      }
    });
    tabbedpane.getInputMap(JComponent.WHEN_FOCUSED).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
    tabbedpane.getActionMap().put("start-editing", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startEditing();

      }
    });
  }

  protected void closeTab() {
    int tabNumber = tabbedpane.indexOfTabComponent(TabHeader.this);
    if (tabNumber != -1) {
      int showConfirmDialog = JOptionPane.showConfirmDialog(tabbedpane, "Do you really want to close \"" + label.getText() + "\"?", "Are you sure?",
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (showConfirmDialog == JOptionPane.OK_OPTION) {
        tabbedpane.remove(tabNumber);
      }
    }

  }

  private void startEditing() {
    editing_idx = tabbedpane.getSelectedIndex();
    tabComponent = tabbedpane.getTabComponentAt(editing_idx);
    tabbedpane.setTabComponentAt(editing_idx, editor);

    editor.setVisible(true);
    editor.setText(tabbedpane.getTitleAt(editing_idx));
    editor.selectAll();
    editor.requestFocusInWindow();
    len = editor.getText().length();
    dim = editor.getPreferredSize();
    editor.setMinimumSize(dim);
  }

  private void cancelEditing() {
    if (editing_idx >= 0) {
      tabbedpane.setTabComponentAt(editing_idx, tabComponent);
      editor.setVisible(false);
      editing_idx = -1;
      len = -1;
      tabComponent = null;
      editor.setPreferredSize(null);
      tabbedpane.requestFocusInWindow();
    }
  }

  private void renameTabTitle() {
    String title = editor.getText().trim();
    if (editing_idx >= 0 && !title.isEmpty()) {
      label.setText(title);
      tabbedpane.setTitleAt(editing_idx, title);
    }
    cancelEditing();
  }

}
