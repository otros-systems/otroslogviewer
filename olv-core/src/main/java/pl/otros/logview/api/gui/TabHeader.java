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
package pl.otros.logview.api.gui;

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
  private final JTabbedPane jTabbedPane;

  private final JButton iconButton;
  private final JLabel label;
  private final JTextField editor;
  private int len;
  private Dimension dim;

  public TabHeader(JTabbedPane pane, String name, String tooltip) {
    this(pane, name, null, tooltip);
  }

  public TabHeader(JTabbedPane pane, String name, Icon icon, String tooltip) {
    super(new FlowLayout(FlowLayout.LEFT, 3, 0));
    this.jTabbedPane = pane;
    this.setName("Tab.header");
    iconButton = new JButton(NORMAL_ICON);
    iconButton.setName("Tab.close");
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
    iconButton.addActionListener(e -> closeTab());
    label = new JLabel(name, icon, SwingConstants.LEFT);
    label.setName("Tab.name");
    label.setToolTipText(tooltip);
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
          TabHeader.this.jTabbedPane.setSelectedIndex(TabHeader.this.jTabbedPane.indexOfTabComponent(TabHeader.this));
          TabHeader.this.requestFocusInWindow();
        }
        Rectangle rect = jTabbedPane.getUI().getTabBounds(jTabbedPane, jTabbedPane.getSelectedIndex());
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          startEditing();
        } else if (rect != null && !rect.contains(e.getPoint())) {
          if (editor.isVisible()) {
            renameTabTitle();
          }
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
        if (editor.isVisible()) {
          renameTabTitle();
        }
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
          editor.setPreferredSize(editor.getText().length() > len ? null : dim);
          jTabbedPane.revalidate();
        }
      }
    });
    jTabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
    jTabbedPane.getActionMap().put("start-editing", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startEditing();

      }
    });
  }

  protected void closeTab() {
    int tabNumber = jTabbedPane.indexOfTabComponent(TabHeader.this);
    if (tabNumber != -1) {
      int showConfirmDialog = JOptionPane.showConfirmDialog(jTabbedPane, "Do you really want to close \"" + label.getText() + "\"?", "Are you sure?",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (showConfirmDialog == JOptionPane.OK_OPTION) {
        jTabbedPane.remove(tabNumber);
      }
    }

  }

  private void startEditing() {
    editor.setVisible(true);
    editor.setText(label.getText());
    editor.selectAll();
    len = editor.getText().length();
    dim = editor.getPreferredSize();
    editor.setMinimumSize(dim);

    remove(label);
    add(editor, 0);
    revalidate();

    editor.requestFocusInWindow();
  }

  private void cancelEditing() {
    if (editor.isVisible()) {
      remove(editor);
      add(label, 0);
      revalidate();
    }
  }

  private void renameTabTitle() {
    String title = editor.getText().trim();
    if (!title.isEmpty()) {
      label.setText(title);
    }
    cancelEditing();
  }

}
