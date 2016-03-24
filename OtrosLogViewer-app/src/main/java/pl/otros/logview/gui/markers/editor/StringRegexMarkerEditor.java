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

import org.apache.commons.io.IOUtils;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.MarkerColors;
import pl.otros.logview.api.AutomaticMarker;
import pl.otros.logview.gui.markers.PropertyFileAbstractMarker;
import pl.otros.logview.gui.markers.RegexMarker;
import pl.otros.logview.gui.markers.StringMarker;
import pl.otros.logview.gui.renderers.MarkerColorsComboBoxRenderer;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;

public class StringRegexMarkerEditor extends JPanel {

  private final JButton saveButton;
  private final JFileChooser chooser;
  private final MarkerFileFilter fileFilterString;

  private final JTextField condition;
  private final JTextField file;
  private final JTextField preCondition;
  private final JCheckBox ignoreCase;
  private final JCheckBox include;
  private final JTextField groups;
  private final JTextField name;
  private final JTextField description;
  private final JComboBox<String> type;
  private final JComboBox<MarkerColors> colors;
  private final JLabel preConditionLabel = new JLabel("Precondition:");

  private final JTextField[] testFields;

  private final JLabel[] testResults;
  private final PluginableElementsContainer<AutomaticMarker> markersContainer;

  public StringRegexMarkerEditor() {
    markersContainer = AllPluginables.getInstance().getMarkersContainser();
    SaveEnableListener saveEnableListener = new SaveEnableListener();
    chooser = new JFileChooser("./plugins/markers");
    chooser.setMultiSelectionEnabled(false);
    fileFilterString = new MarkerFileFilter();

    chooser.setFileFilter(fileFilterString);
    GridBagLayout bagLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    this.setLayout(bagLayout);
    TestAfterChangeActionListener testAfterChangeActionListener = new TestAfterChangeActionListener();

    JButton loadButton = new JButton("Load");
    loadButton.addActionListener(new LoadctionListener());
    saveButton = new JButton("Save");
    saveButton.addActionListener(new SaveActionListener());
    JButton saveAsButton = new JButton("Save as");
    saveAsButton.addActionListener(new SaveAsActionListener());
    JButton newButton = new JButton("New");
    newButton.addActionListener(new NewMarkerActionListener());

    type = new JComboBox<>(new String[]{"String matcher", "Regex matcher"});

    type.addActionListener(testAfterChangeActionListener);
    type.addActionListener(saveEnableListener);

    int testLines = 3;
    testFields = new JTextField[3];
    testResults = new JLabel[3];
    for (int i = 0; i < testLines; i++) {
      testFields[i] = new JTextField(20);
      testFields[i].getDocument().addDocumentListener(testAfterChangeActionListener);
      testResults[i] = new JLabel("?");
    }

    file = new JTextField(20);
    file.setEditable(false);
    file.getDocument().addDocumentListener(saveEnableListener);
    name = new JTextField(20);
    description = new JTextField(20);
    condition = new JTextField(20);
    condition.getDocument().addDocumentListener(testAfterChangeActionListener);
    preCondition = new JTextField(20);
    preCondition.getDocument().addDocumentListener(testAfterChangeActionListener);
    ignoreCase = new JCheckBox();
    ignoreCase.addActionListener(testAfterChangeActionListener);
    include = new JCheckBox();
    include.addActionListener(testAfterChangeActionListener);
    groups = new JTextField(20);

    colors = new JComboBox<>(MarkerColors.values());
    colors.setRenderer(new MarkerColorsComboBoxRenderer());


    type.addActionListener(arg0 -> {
      if (type.getSelectedIndex() == 0) {
        preConditionLabel.setEnabled(false);
        preCondition.setEnabled(false);
      } else {
        preConditionLabel.setEnabled(true);
        preCondition.setEnabled(true);
      }
    });

    c.gridwidth = 1;
    c.insets = new Insets(4, 4, 4, 4);
    this.add(saveButton, c);
    c.gridx = 1;
    this.add(saveAsButton, c);
    c.gridy++;
    c.gridy++;
    c.gridx = 0;
    this.add(newButton, c);
    c.gridx = 1;
    this.add(loadButton, c);
    c.gridy++;

    this.addFormLabelsLeftLong(new JLabel("Type:"), type, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("File:"), file, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("Name:"), name, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("Description:"), description, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("Groups:"), groups, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("Condition:"), condition, c);
    c.gridy++;
    this.addFormLabelsLeftLong(preConditionLabel, preCondition, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("Ignore case:"), ignoreCase, c);
    c.gridy++;
    JLabel includeLabel = new JLabel("(Pre)condition matches:");
    this.addFormLabelsLeftLong(includeLabel, include, c);
    c.gridy++;
    this.addFormLabelsLeftLong(new JLabel("Color"), colors, c);
    c.gridy++;

    this.addFormLabelsRightLong(new JLabel("Test lines"), new JLabel("Result"), c);
    c.gridy++;
    for (int i = 0; i < testLines; i++) {
      this.addFormLabelsRightLong(testFields[i], testResults[i], c);
      c.gridy++;
    }

    JLabel warningLabel = new JLabel("Warning, restart application to reload changes.");
    warningLabel.setForeground(Color.RED);
    warningLabel.setOpaque(false);
    c.gridx = 0;
    c.gridwidth = 3;
    this.add(warningLabel, c);
    type.setSelectedIndex(0);
    newMarker();
  }

  private void addFormLabelsRightLong(Component left, Component right, GridBagConstraints c) {
    c.gridx = 0;
    c.gridwidth = 2;
    this.add(left, c);
    c.gridx = 2;
    c.gridwidth = 1;
    this.add(right, c);
  }

  private void addFormLabelsLeftLong(Component left, Component right, GridBagConstraints c) {
    c.gridx = 0;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    this.add(left, c);
    c.gridx = 1;
    c.gridwidth = 2;
    this.add(right, c);
  }

  private void testMarker() {
    for (JLabel testResult : testResults) {
      testResult.setText("?");
    }
    try {
      AutomaticMarker marker = null;
      Properties p = new Properties();
      p.put(RegexMarker.NAME, name.getText());
      p.put(RegexMarker.DESCRIPTION, description.getText());
      p.put(RegexMarker.GROUPS, groups.getText());
      p.put(RegexMarker.IGNORE_CASE, Boolean.toString(ignoreCase.isSelected()));
      p.put(RegexMarker.PRECONDITION_INCLUDE, Boolean.toString(include.isSelected()));
      p.put(PropertyFileAbstractMarker.INCLUDE, Boolean.toString(include.isSelected()));
      p.put(RegexMarker.PRECONDITION, preCondition.getText());
      p.put(RegexMarker.CONDITION, condition.getText());
      if (type.getSelectedIndex() == 0) {
        marker = new StringMarker(p);
      } else {
        marker = new RegexMarker(p);
      }
      LogData ld = new LogData();
      for (int i = 0; i < testFields.length; i++) {
        String message = testFields[i].getText();
        ld.setMessage(message);
        boolean marked = marker.toMark(ld);
        testResults[i].setText(marked ? "Marked" : "Not marked");
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private class TestAfterChangeActionListener implements ActionListener, DocumentListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      testMarker();
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {
      testMarker();

    }

    @Override
    public void insertUpdate(DocumentEvent arg0) {
      testMarker();

    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
      testMarker();

    }
  }

  private class SaveAsActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (type.getSelectedIndex() == 0) {
        fileFilterString.switchToStringOnly();
      } else {
        fileFilterString.switchToRegexOnly();
      }
      int response = chooser.showSaveDialog(StringRegexMarkerEditor.this);
      if (response != JFileChooser.APPROVE_OPTION) {
        return;
      }
      String fileName = chooser.getSelectedFile().getAbsolutePath();
      if (type.getSelectedIndex() == 0 && !fileName.endsWith("stringMarker")) {
        fileName = fileName + ".stringMarker";
      }
      file.setText(fileName);
      try {
        save(fileName);
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(StringRegexMarkerEditor.this, "Cannot save marker: " + e1.getMessage(), "Error saving marker", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private class SaveActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        save(file.getText());
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(StringRegexMarkerEditor.this, "Cannot save marker: " + e1.getMessage(), "Error saving marker", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private class LoadctionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      fileFilterString.switchToBoth();
      int response = chooser.showSaveDialog(StringRegexMarkerEditor.this);
      if (response != JFileChooser.APPROVE_OPTION) {
        return;
      }
      file.setText(chooser.getSelectedFile().getAbsolutePath());
      Properties p = new Properties();
      FileInputStream fin;
      try {
        fin = new FileInputStream(chooser.getSelectedFile());
        p.load(fin);
        if (file.getText().endsWith("stringMarker")) {
          type.setSelectedIndex(0);
          include.setSelected(Boolean.parseBoolean(p.getProperty(PropertyFileAbstractMarker.INCLUDE, "false")));
        } else {
          type.setSelectedIndex(1);
          include.setSelected(Boolean.parseBoolean(p.getProperty(RegexMarker.PRECONDITION_INCLUDE, "true")));
        }
        name.setText(p.getProperty(RegexMarker.NAME, "?"));
        description.setText(p.getProperty(RegexMarker.DESCRIPTION, "?"));
        groups.setText(p.getProperty(RegexMarker.GROUPS, ""));
        ignoreCase.setSelected(Boolean.parseBoolean(p.getProperty(RegexMarker.IGNORE_CASE, "false")));
        preCondition.setText(p.getProperty(RegexMarker.PRECONDITION, ""));
        condition.setText(p.getProperty(RegexMarker.CONDITION, ""));
        colors.getModel().setSelectedItem(MarkerColors.fromString(p.getProperty(PropertyFileAbstractMarker.COLOR, "")));
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  private class MarkerFileFilter extends FileFilter {

    private String description;
    private String suffix;

    private void switchToStringOnly() {
      suffix = ".stringMarker";
      description = "String marker (*.stringMarker)";
    }

    private void switchToRegexOnly() {
      suffix = ".regexMarker";
      description = "Regular expression marker (*.regexMarker)";
    }

    private void switchToBoth() {
      suffix = "Marker";
      description = "String or regular expression marker (*.stringMarker, *.regexMarker)";
    }

    public MarkerFileFilter() {
      switchToStringOnly();
    }

    @Override
    public boolean accept(File f) {
      return f.isDirectory() || f.getName().endsWith(suffix);
    }

    @Override
    public String getDescription() {
      return description;
    }
  }

  private class SaveEnableListener implements ActionListener, DocumentListener {

    private void checkIfSaveEnable() {
      boolean enable = true;
      if (file.getText().length() == 0) {
        enable = false;
      } else if (file.getText().endsWith("stringMarker") && type.getSelectedIndex() != 0) {
        enable = false;
      } else if (file.getText().endsWith("regexMarker") && type.getSelectedIndex() != 1) {
        enable = false;
      }
      saveButton.setEnabled(enable);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      checkIfSaveEnable();
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {
      checkIfSaveEnable();

    }

    @Override
    public void insertUpdate(DocumentEvent arg0) {
      checkIfSaveEnable();

    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
      checkIfSaveEnable();

    }

  }

  private void save(String file) throws Exception {
    Properties p = getMarkerProperties();
    FileOutputStream fout = new FileOutputStream(file);
    p.store(fout, "File generated at " + new Date());
    IOUtils.closeQuietly(fout);
    AutomaticMarker newMarker = null;
    if (type.getSelectedIndex() == 0) {
      newMarker = new StringMarker(p);
    } else {
      newMarker = new RegexMarker(p);
    }

    markersContainer.addElement(newMarker);
  }

  private Properties getMarkerProperties() {
    Properties p = new Properties();
    p.put(RegexMarker.NAME, name.getText());
    p.put(RegexMarker.DESCRIPTION, description.getText());
    p.put(RegexMarker.GROUPS, groups.getText());
    p.put(RegexMarker.IGNORE_CASE, Boolean.toString(ignoreCase.isSelected()));
    p.put(RegexMarker.PRECONDITION_INCLUDE, Boolean.toString(include.isSelected()));
    p.put(PropertyFileAbstractMarker.INCLUDE, Boolean.toString(include.isSelected()));
    p.put(RegexMarker.PRECONDITION, preCondition.getText());
    p.put(RegexMarker.CONDITION, condition.getText());
    p.put(PropertyFileAbstractMarker.COLOR, ((MarkerColors) colors.getSelectedItem()).name());
    return p;
  }

  private void newMarker() {
    name.setText("");
    description.setText("");
    file.setText("");
    groups.setText("");
    preCondition.setText("");
    condition.setText("");
    ignoreCase.setSelected(true);
    include.setSelected(true);

  }

  private class NewMarkerActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      newMarker();
    }

  }

  public static void main(String[] args) {

    JFrame f = new JFrame();
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(new StringRegexMarkerEditor());
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.pack();
    f.setVisible(true);
  }
}
