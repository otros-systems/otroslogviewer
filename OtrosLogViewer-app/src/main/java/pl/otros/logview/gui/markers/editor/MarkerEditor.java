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
package pl.otros.logview.gui.markers.editor;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.pluginable.AutomaticMarker;
import pl.otros.logview.gui.markers.PropertyFileAbstractMarker;
import pl.otros.logview.gui.markers.RegexMarker;
import pl.otros.logview.gui.markers.StringMarker;
import pl.otros.logview.gui.renderers.MarkerColorsComboBoxRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

public class MarkerEditor extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarkerEditor.class.getName());

  private boolean changed = false;
  private final JTextField stringMatcherConditionM;
  private final JTextField file;
  private final JTextField regexPreCondition;
  private final JCheckBox ignoreCase;
  private final JCheckBox stringConditionInclude;
  private final JCheckBox regexPreConditionInclude;
  private final JTextField groups;
  private final JTextField name;
  private final JTextField description;
  private final JComboBox type;
  private final JComboBox colors;
  private final JLabel stringConditionIncludeLabel = new JLabel("Containging string:");
  private final JLabel regexPreConditionLabel = new JLabel("Precondition:");
  private final JLabel regexPreConditionIncludeLabel = new JLabel("Precondition matches:");
  private final JTextField regexMatcherCondition;
  private final JLabel stringMatcherConditionLabel = new JLabel("Condition:");
  private final JLabel regexMatcherConditionLabel = new JLabel("Regex condition:");

  private final JTextArea[] testStringTextArea;
  private final JLabel[] testResults;
  private final Collection<JComponent> regexMatcherComponents;
  private final Collection<JComponent> stringMatcherComponents;
  private final TestAfterChangeActionListener testAfterChangeActionListener = new TestAfterChangeActionListener();
  private final HashSet<ChangeListener> changeListeners;

  public MarkerEditor() {
    super(new MigLayout("wrap 2", "[20%] [grow]", ""));
    changeListeners = new HashSet<>();

    regexMatcherComponents = new ArrayList<>();
    stringMatcherComponents = new ArrayList<>();
    file = new JTextField(20);
    file.setEditable(false);
    type = new JComboBox(new String[]{"String matcher", "Regex matcher"});
    type.addActionListener(arg0 -> disbableUnessasaryComponents());
    type.addActionListener(testAfterChangeActionListener);

    ignoreCase = new JCheckBox();
    ignoreCase.addActionListener(testAfterChangeActionListener);
    stringConditionInclude = new JCheckBox();
    stringConditionInclude.addActionListener(testAfterChangeActionListener);
    regexPreConditionInclude = new JCheckBox();
    regexPreConditionInclude.addActionListener(testAfterChangeActionListener);
    name = new JTextField(20);
    colors = new JComboBox(MarkerColors.values());
    colors.setRenderer(new MarkerColorsComboBoxRenderer());

    stringMatcherConditionM = new JTextField(20);
    stringMatcherConditionM.getDocument().addDocumentListener(testAfterChangeActionListener);

    regexPreCondition = new JTextField(20);
    regexPreCondition.getDocument().addDocumentListener(testAfterChangeActionListener);
    regexMatcherCondition = new JTextField(20);
    regexMatcherCondition.getDocument().addDocumentListener(testAfterChangeActionListener);
    groups = new JTextField(20);
    description = new JTextField(20);

    testStringTextArea = new JTextArea[3];
    testResults = new JLabel[3];
    for (int i = 0; i < 3; i++) {
      testStringTextArea[i] = new JTextArea(4, 20);
      testStringTextArea[i].getDocument().addDocumentListener(testAfterChangeActionListener);
      testResults[i] = new JLabel("");
    }
    type.setSelectedIndex(0);
    addComponents();
    addTooltips();

    stringMatcherComponents.add(stringMatcherConditionLabel);
    stringMatcherComponents.add(stringMatcherConditionM);
    stringMatcherComponents.add(stringConditionInclude);
    stringMatcherComponents.add(stringConditionIncludeLabel);

    regexMatcherComponents.add(regexMatcherCondition);
    regexMatcherComponents.add(regexMatcherConditionLabel);
    regexMatcherComponents.add(regexPreCondition);
    regexMatcherComponents.add(regexPreConditionLabel);
    regexMatcherComponents.add(regexPreConditionIncludeLabel);
    regexMatcherComponents.add(regexPreConditionInclude);

    disbableUnessasaryComponents();
    addChangeListener();
  }

  protected void addChangeListener() {
    stringMatcherConditionM.getDocument().addDocumentListener(testAfterChangeActionListener);
    regexPreCondition.getDocument().addDocumentListener(testAfterChangeActionListener);
    ignoreCase.addActionListener(testAfterChangeActionListener);
    stringConditionInclude.addActionListener(testAfterChangeActionListener);
    regexPreConditionInclude.addActionListener(testAfterChangeActionListener);
    groups.getDocument().addDocumentListener(testAfterChangeActionListener);
    name.getDocument().addDocumentListener(testAfterChangeActionListener);
    description.getDocument().addDocumentListener(testAfterChangeActionListener);
    type.addActionListener(testAfterChangeActionListener);
    colors.addActionListener(testAfterChangeActionListener);
  }

  protected void disbableUnessasaryComponents() {
    boolean b = type.getSelectedIndex() == 0;
    enableComponentes(stringMatcherComponents, b);
    enableComponentes(regexMatcherComponents, !b);
  }

  protected void enableComponentes(Collection<JComponent> components, boolean enabled) {
    for (JComponent jc : components) {
      jc.setEnabled(enabled);
    }
  }

  private void addComponents() {

    add(new JLabel("File"));
    add(file);
    add(new JLabel("Type:"));
    add(type, "growx");
    add(new JLabel("Name:"));
    add(name);
    add(new JLabel("Description:"));
    add(description);
    add(new JLabel("Groups:"));
    add(groups);
    add(new JLabel("Marking color:"));
    add(colors, "growx");

    add(new JLabel("Ingore case"));
    add(ignoreCase);
    stringMatcherComponents.addAll(addSeparator(this, "String matcher options"));
    add(stringMatcherConditionLabel);
    add(stringMatcherConditionM);
    add(stringConditionIncludeLabel);
    add(stringConditionInclude);

    regexMatcherComponents.addAll(addSeparator(this, "Regex matcher options"));
    add(regexMatcherConditionLabel);
    add(regexMatcherCondition);
    add(regexPreConditionLabel);
    add(regexPreCondition);
    add(regexPreConditionIncludeLabel);
    add(regexPreConditionInclude);

    addSeparator(this, "Test marker");

    for (int i = 0; i < testStringTextArea.length; i++) {
      add(new JLabel("Test message " + (i + 1) + ":"), "span 1 4,top, left");
      add(new JScrollPane(testStringTextArea[i]), "span 1 4, growx");
      add(new JLabel("Test " + (i + 1) + " result: "), "top, left, gapbottom 25");
      add(testResults[i], "gapbottom 25");
    }
    clearValues();

  }

  private void addTooltips() {
    file.setToolTipText("File used to store this marker");
    name.setToolTipText("Name of this marker, will be displayed in menus, list etc..");
    type.setToolTipText("Choose if you want to mark events based on simple test for containing string or more sophisticated with regular expressions.");
    description.setToolTipText("Description will be used as tooltip for this marker.");
    groups.setToolTipText("Enter comma sperated groups name, markers will be grouped in menus.");
    colors.setToolTipText("Marker will highlight event with selected color.");
    ignoreCase.setToolTipText("String/regex matching will be case insensitive.");
    stringMatcherConditionM.setToolTipText("String to look for.");
    stringConditionInclude.setToolTipText("Will match events that message contains or does not contain this string.");
    regexMatcherCondition.setToolTipText("Regular expression to match.");
    regexPreCondition
      .setToolTipText("Part of string from regular expression for increase performance. For example if you regular exrepssion is \".*return: \\d+\" set precondition to \"return\".");
    regexPreConditionInclude.setToolTipText("Disable if you want to mark rows that does not contains precondition value.");
    for (JTextArea aTestStringTextArea : testStringTextArea) {
      aTestStringTextArea.setToolTipText("Enter part of log message to test if marker works as you want.");
    }

  }

  public Properties getMarkerPropertiesFromView() {
    Properties p = new Properties();
    p.put(PropertyFileAbstractMarker.FILE, file.getText());
    p.put(RegexMarker.NAME, name.getText());
    p.put(RegexMarker.DESCRIPTION, description.getText());
    p.put(RegexMarker.GROUPS, groups.getText());
    p.put(RegexMarker.IGNORE_CASE, Boolean.toString(ignoreCase.isSelected()));
    p.put(PropertyFileAbstractMarker.COLOR, ((MarkerColors) colors.getSelectedItem()).name());

    if (type.getSelectedIndex() == 0) {
      p.put(StringMarker.INCLUDE, Boolean.toString(stringConditionInclude.isSelected()));
      p.put(StringMarker.CONDITION, stringMatcherConditionM.getText());
      p.put(PropertyFileAbstractMarker.TYPE, PropertyFileAbstractMarker.TYPE_STRING);
    } else {
      p.put(RegexMarker.PRECONDITION_INCLUDE, Boolean.toString(regexPreConditionInclude.isSelected()));
      p.put(PropertyFileAbstractMarker.INCLUDE, Boolean.toString(regexPreConditionInclude.isSelected()));
      p.put(RegexMarker.PRECONDITION, regexPreCondition.getText());
      p.put(RegexMarker.CONDITION, regexMatcherCondition.getText());
      p.put(PropertyFileAbstractMarker.TYPE, PropertyFileAbstractMarker.TYPE_REGEX);
    }

    p.put(PropertyFileAbstractMarker.TEST_STRING_1, testStringTextArea[0].getText());
    p.put(PropertyFileAbstractMarker.TEST_STRING_2, testStringTextArea[1].getText());
    p.put(PropertyFileAbstractMarker.TEST_STRING_3, testStringTextArea[2].getText());

    return p;
  }

  public void setViewFromProperties(Properties p) {
    clearValues();
    String mType = p.getProperty(PropertyFileAbstractMarker.TYPE);
    if (PropertyFileAbstractMarker.TYPE_STRING.equals(mType) || file.getText().endsWith("stringMarker")) {
      type.setSelectedIndex(0);
      stringMatcherConditionM.setText(p.getProperty(RegexMarker.CONDITION, ""));
      stringConditionInclude.setSelected(Boolean.parseBoolean(p.getProperty(PropertyFileAbstractMarker.INCLUDE, "true")));
    } else {
      stringMatcherConditionM.setText("");
      regexMatcherCondition.setText(p.getProperty(PropertyFileAbstractMarker.CONDITION, ""));
      type.setSelectedIndex(1);
    }
    file.setText(p.getProperty(PropertyFileAbstractMarker.FILE, ""));
    file.setCaretPosition(0);
    name.setText(p.getProperty(RegexMarker.NAME, ""));
    name.setCaretPosition(0);
    description.setText(p.getProperty(RegexMarker.DESCRIPTION, ""));
    description.setCaretPosition(0);
    groups.setText(p.getProperty(RegexMarker.GROUPS, ""));
    groups.setCaretPosition(0);
    ignoreCase.setSelected(Boolean.parseBoolean(p.getProperty(RegexMarker.IGNORE_CASE, "false")));
    colors.getModel().setSelectedItem(MarkerColors.fromString(p.getProperty(PropertyFileAbstractMarker.COLOR, "")));

    regexPreCondition.setText(p.getProperty(RegexMarker.PRECONDITION, ""));
    regexPreCondition.setCaretPosition(0);
    regexPreConditionInclude.setSelected(Boolean.parseBoolean(p.getProperty(RegexMarker.PRECONDITION_INCLUDE, "true")));

    testStringTextArea[0].setText(p.getProperty(PropertyFileAbstractMarker.TEST_STRING_1, ""));
    testStringTextArea[0].setCaretPosition(0);
    testStringTextArea[1].setText(p.getProperty(PropertyFileAbstractMarker.TEST_STRING_2, ""));
    testStringTextArea[1].setCaretPosition(0);
    testStringTextArea[2].setText(p.getProperty(PropertyFileAbstractMarker.TEST_STRING_3, ""));
    testStringTextArea[2].setCaretPosition(0);
    setChanged(false);
  }

  public void clearValues() {
    file.setText("");
    name.setText("");
    description.setText("");
    groups.setText("");
    ignoreCase.setSelected(true);
    stringConditionInclude.setSelected(true);
    regexPreConditionInclude.setSelected(true);
    colors.setSelectedIndex(0);

    stringConditionInclude.setSelected(true);
    stringMatcherConditionM.setText("");

    regexMatcherCondition.setText("");
    regexPreCondition.setText("");
    regexPreConditionInclude.setSelected(true);
    type.setSelectedIndex(0);

    testStringTextArea[0].setText("");
    testStringTextArea[1].setText("");
    testStringTextArea[2].setText("");
    setChanged(false);
  }

  private Collection<JComponent> addSeparator(JPanel panel, String text) {
    ArrayList<JComponent> c = new ArrayList<>();
    JLabel l = new JLabel(text, SwingConstants.LEADING);
    panel.add(l, "gapbottom 1, span, split 2, aligny center");
    JSeparator s = new JSeparator();
    panel.add(s, "gapleft rel, growx");
    c.add(s);
    c.add(l);
    return c;
  }

  private class TestAfterChangeActionListener implements ActionListener, DocumentListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      testMarker();
      setChanged(true);
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {
      testMarker();
      setChanged(true);
    }

    @Override
    public void insertUpdate(DocumentEvent arg0) {
      testMarker();
      setChanged(true);
    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
      testMarker();
      setChanged(true);
    }
  }

  private void testMarker() {
    for (JLabel testResult : testResults) {
      testResult.setText("?");
    }
    try {
      AutomaticMarker marker = null;
      Properties p = getMarkerPropertiesFromView();

      if (type.getSelectedIndex() == 0) {
        marker = new StringMarker(p);
      } else {
        marker = new RegexMarker(p);
      }
      LogData ld = new LogData();
      for (int i = 0; i < testStringTextArea.length; i++) {
        String message = testStringTextArea[i].getText();
        ld.setMessage(message);
        boolean marked = marker.toMark(ld);
        testResults[i].setText(marked ? "Marked" : "Not marked");
      }
    } catch (Exception e) {
      LOGGER.info("Exception during checking pattern: " + e.getMessage());

    }
  }

  public boolean isChanged() {
    return changed;
  }

  public void setChanged(boolean changed) {
    if (changed != this.changed) {
      this.changed = changed;
      ChangeEvent e = new ChangeEvent(this);
      synchronized (changeListeners) {
        for (ChangeListener l : changeListeners) {
          l.stateChanged(e);
        }
      }
    }
  }

  public void addChangeListener(ChangeListener listener) {
    synchronized (changeListeners) {
      changeListeners.add(listener);
    }
  }

  public void removeChangeListener(ChangeListener listener) {
    synchronized (changeListeners) {
      changeListeners.remove(listener);
    }
  }

}
