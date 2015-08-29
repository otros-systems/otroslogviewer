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
import pl.otros.logview.gui.util.DelayedSwingInvoke;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class AbstractStringBasedFilter extends AbstractLogFilter {

  protected static final int EDIT_FILTER_ACTION_DELAY = 1000;
  protected JTextField textField;
  protected String filteringText;
  protected Color editingColor = Color.YELLOW;
  protected Color normalColor = null;
  protected JPanel gui;
  protected JLabel label;
  protected MigLayout layout;
  protected boolean ignoreCase = true;
  protected JCheckBox caseInsensetiveCheckBox;
  private final DelayedSwingInvoke deleyedSwingInvoke;

  public AbstractStringBasedFilter(String name, String description, String labelText) {
    super(name, description);
    createGui(labelText);
    normalColor = textField.getBackground();
    deleyedSwingInvoke = new DelayedSwingInvoke() {

      @Override
      protected void performActionHook() {
        performFiltering();
      }
    };
    textField.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e) {
        textField.setBackground(editingColor);
        deleyedSwingInvoke.performAction();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        textField.setBackground(editingColor);
        deleyedSwingInvoke.performAction();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        textField.setBackground(editingColor);
        deleyedSwingInvoke.performAction();
      }
    });
    textField.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 10) {
          deleyedSwingInvoke.performActionNow();
        }
      }
    });

    caseInsensetiveCheckBox.addChangeListener(e -> {
      ignoreCase = caseInsensetiveCheckBox.isSelected();
      performFiltering();
    });

  }

  protected void createGui(String labelText) {
    label = new JLabel(labelText);
    textField = new JTextField();
    layout = new MigLayout();
    caseInsensetiveCheckBox = new JCheckBox("Case insensitive", ignoreCase);
    gui = new JPanel(layout);
    gui.add(label, "wrap");
    gui.add(textField, "grow, wrap");
    gui.add(caseInsensetiveCheckBox);

  }

  private void performFiltering() {
    textField.setBackground(normalColor);
    setFilteringText(textField.getText());
    setIgnoreCase(caseInsensetiveCheckBox.isSelected());
    performPreFiltering();
    listener.valueChanged();

  }

  protected abstract void performPreFiltering();

  @Override
  public Component getGUI() {
    return gui;
  }

  public String getFilteringText() {
    return filteringText;
  }

  public void setFilteringText(String filteringText) {
    this.filteringText = filteringText;
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

}
