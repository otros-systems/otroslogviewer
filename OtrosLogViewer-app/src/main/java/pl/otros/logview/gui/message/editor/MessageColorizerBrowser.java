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
package pl.otros.logview.gui.message.editor;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.logview.gui.actions.AbstractActionWithConfirmation;
import pl.otros.logview.gui.message.pattern.PropertyPatternMessageColorizer;
import pl.otros.logview.gui.util.DirectoryRestrictedFileSystemView;
import pl.otros.logview.pluginable.PluginableElementListModel;
import pl.otros.logview.pluginable.PluginableElementNameListRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

public class MessageColorizerBrowser extends JPanel {

  private static final String MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT = "MessageColorizerEditorDefaultContent.txt";
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageColorizerBrowser.class.getName());
  private final PluginableElementsContainer<MessageColorizer> container;
  private final OtrosApplication otrosApplication;
  private final JList jList;
  private final JPanel contentPanel;
  private final CardLayout cardLayout;
  private static final String CARD_LAYOUT_EDITOR = "editor";
  private static final String CARD_LAYOUT_NOT_EDITABLE = "notEditable";
  private static final String CARD_LAYOUT_NO_SELECTED = "noSelected";
  private final MessageColorizerEditor editor;
  private String defaultContent;
  private final JButton useButton;
  private final JButton saveButton;
  private final JButton saveAsButton;
  private final JButton deleteButton;

  private JFileChooser chooser;
  private final DeleteSelected deleteAction;

  public MessageColorizerBrowser(OtrosApplication otrosApplication) {
    super(new BorderLayout());
    this.container = otrosApplication.getAllPluginables().getMessageColorizers();
    this.otrosApplication = otrosApplication;

    JToolBar toolBar = new JToolBar();
    editor = new MessageColorizerEditor(otrosApplication.getStatusObserver());
    JLabel noEditable = new JLabel("Selected MessageColorizer is not editable.", SwingConstants.CENTER);
    JLabel nothingSelected = new JLabel("Nothing selected", SwingConstants.CENTER);

    PluginableElementListModel<MessageColorizer> listModel = new PluginableElementListModel<>(container);
    jList = new JList<>(listModel);
    jList.setCellRenderer(new PluginableElementNameListRenderer());
    cardLayout = new CardLayout();
    contentPanel = new JPanel(cardLayout);
    contentPanel.add(editor, CARD_LAYOUT_EDITOR);
    contentPanel.add(noEditable, CARD_LAYOUT_NOT_EDITABLE);
    contentPanel.add(nothingSelected, CARD_LAYOUT_NO_SELECTED);
    cardLayout.show(contentPanel, CARD_LAYOUT_NOT_EDITABLE);
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(jList), contentPanel);
    mainSplitPane.setDividerLocation(220);

    jList.getSelectionModel().addListSelectionListener(e -> {
      showSelected();
      enableDisableButtonsForSelectedColorizer();
    });

    jList.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE) {
          ActionEvent actionEvent = new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, "");
          deleteAction.actionPerformed(actionEvent);
        }
      }
    });


    saveButton = new JButton("Save and use", Icons.DISK);
    saveButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        try {
          PropertyPatternMessageColorizer mc = editor.createMessageColorizer();
          File selectedFile = null;
          String f = mc.getFile();
          if (StringUtils.isNotBlank(f)) {
            selectedFile = new File(mc.getFile());
          } else {
            int response = chooser.showSaveDialog(MessageColorizerBrowser.this);
            if (response != JFileChooser.APPROVE_OPTION) {
              return;
            }
            selectedFile = chooser.getSelectedFile();
            if (!selectedFile.getName().endsWith(".pattern")) {
              selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".pattern");
            }
          }
          removeMessageColorizerWithNullFile();
          applyMessageColorizer(selectedFile);
          saveMessageColorizer(selectedFile);
          jList.setSelectedValue(mc, true);
        } catch (ConfigurationException e1) {
          String errorMessage = String.format("Can't save message colorizer: %s", e1.getMessage());
          LOGGER.error(errorMessage);
          MessageColorizerBrowser.this.otrosApplication.getStatusObserver().updateStatus(errorMessage, StatusObserver.LEVEL_ERROR);
        }
      }
    });

    saveAsButton = new JButton("Save as", Icons.DISK_PLUS);
    saveAsButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          int response = chooser.showSaveDialog(MessageColorizerBrowser.this);
          if (response != JFileChooser.APPROVE_OPTION) {
            return;
          }
          File selectedFile = chooser.getSelectedFile();
          selectedFile = chooser.getSelectedFile();
          if (!selectedFile.getName().endsWith(".pattern")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".pattern");
          }
          removeMessageColorizerWithNullFile();
          applyMessageColorizer(selectedFile);
          saveMessageColorizer(selectedFile);
          jList.setSelectedValue(editor.createMessageColorizer(), true);
        } catch (ConfigurationException e1) {
          String errorMessage = String.format("Can't save message colorizer: %s", e1.getMessage());
          LOGGER.error(errorMessage);
          MessageColorizerBrowser.this.otrosApplication.getStatusObserver().updateStatus(errorMessage, StatusObserver.LEVEL_ERROR);
        }
      }
    });

    useButton = new JButton("Use without saving");
    useButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        try {
          removeMessageColorizerWithNullFile();
          applyMessageColorizer(File.createTempFile("messageColorizer", "pattern"));
        } catch (Exception e) {
          LOGGER.error("Cannot create message colorizer: " + e.getMessage());
        }

      }
    });

    deleteAction = new DeleteSelected(otrosApplication);
    deleteButton = new JButton(deleteAction);


    JButton createNew = new JButton("Create new", Icons.ADD);
    createNew.addActionListener(e -> {
      saveAsButton.setEnabled(false);
      createNew();
    });

    toolBar.setFloatable(false);
    toolBar.add(createNew);
    toolBar.add(saveButton);
    toolBar.add(saveAsButton);
    toolBar.add(useButton);
    toolBar.add(deleteButton);
    enableDisableButtonsForSelectedColorizer();
    initFileChooser();
    this.add(mainSplitPane);
    this.add(toolBar, BorderLayout.SOUTH);
  }

  protected void deleteSelected() {
    Object selectedValue = jList.getSelectedValue();
    if (selectedValue instanceof PropertyPatternMessageColorizer) {
      PropertyPatternMessageColorizer mc = (PropertyPatternMessageColorizer) selectedValue;
      container.removeElement(mc);
      File f = new File(mc.getFile());
      boolean deleted = f.delete();
      if (deleted) {
        otrosApplication.getStatusObserver().updateStatus(String.format("Message colorizer \"%s\" have been deleted [file %s]", mc.getName(), mc.getFile()));
      } else {
        otrosApplication.getStatusObserver().updateStatus(String.format("Message colorizer \"%s\" have been not deleted [file %s]", mc.getName(), mc.getFile()),
          StatusObserver.LEVEL_ERROR);
      }

    }
  }

  protected void removeMessageColorizerWithNullFile() {
    PropertyPatternMessageColorizer mc = new PropertyPatternMessageColorizer();
    mc.setFile("");
    container.removeElement(mc);
  }

  private void initFileChooser() {
    File rootDirectory = AllPluginables.USER_MESSAGE_FORMATTER_COLORZIERS;
    DirectoryRestrictedFileSystemView view = new DirectoryRestrictedFileSystemView(rootDirectory);
    chooser = new JFileChooser(rootDirectory, view);
    chooser.setFileFilter(new FileFilter() {

      @Override
      public String getDescription() {
        return "*.pattern";
      }

      @Override
      public boolean accept(File f) {
        return f.isFile() && f.getName().endsWith("pattern");
      }
    });

  }

  protected void saveMessageColorizer(File selectedFile) {
    FileOutputStream fout = null;
    try {
      PropertyPatternMessageColorizer mc = editor.createMessageColorizer();
      mc.setTestMessage(editor.getTextToColorize());
      fout = new FileOutputStream(selectedFile);
      mc.store(fout);
    } catch (ConfigurationException | FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(fout);
    }
  }

  protected void applyMessageColorizer(File f) throws ConfigurationException {
    PropertyPatternMessageColorizer mc = editor.createMessageColorizer();
    String name2 = mc.getName();
    while (StringUtils.isBlank(name2)) {
      name2 = JOptionPane.showInputDialog("Enter the message colorizer name.");
      mc.setName(name2);
    }
    mc.setFile(f.getAbsolutePath());
    mc.setTestMessage(editor.getTextToColorize());
    container.addElement(mc);
    jList.setSelectedValue(mc, true);
  }

  protected void createNew() {
    PropertyPatternMessageColorizer colorizer = new PropertyPatternMessageColorizer();
    try {
      colorizer.init(new ByteArrayInputStream(getDefaultContent().getBytes()));
    } catch (ConfigurationException e) {
      otrosApplication.getStatusObserver().updateStatus("Can't load message colorizer template: " + e.getMessage(), StatusObserver.LEVEL_WARNING);
    }
    colorizer.setFile("");
    container.addElement(colorizer);
    jList.setSelectedValue(colorizer, true);
    saveAsButton.setEnabled(false);
  }

  protected void showSelected() {
    MessageColorizer selectedValue = (MessageColorizer) jList.getSelectedValue();
    boolean actionEnabled = false;
    if (selectedValue == null) {
      cardLayout.show(contentPanel, CARD_LAYOUT_NO_SELECTED);
    } else if (selectedValue instanceof PropertyPatternMessageColorizer) {
      PropertyPatternMessageColorizer mc = (PropertyPatternMessageColorizer) selectedValue;
      try {
        editor.setMessageColorizer(mc);
        actionEnabled = true;
      } catch (ConfigurationException e) {
        otrosApplication.getStatusObserver().updateStatus("Can't edit message colorizer: " + e.getMessage(), StatusObserver.LEVEL_ERROR);
      }

      cardLayout.show(contentPanel, CARD_LAYOUT_EDITOR);
    } else {
      cardLayout.show(contentPanel, CARD_LAYOUT_NOT_EDITABLE);
    }
    useButton.setEnabled(actionEnabled);
    saveButton.setEnabled(actionEnabled);
    saveAsButton.setEnabled(actionEnabled);

  }

  protected String getDefaultContent() {
    if (defaultContent == null) {
      try {
        defaultContent = IOUtils.toString(this.getClass().getResourceAsStream(MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT));
      } catch (IOException e) {
        LOGGER.error(String.format("Can't load content of %s: %s", MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT, e.getMessage()));
      }
    }
    return defaultContent;
  }

  class DeleteSelected extends AbstractActionWithConfirmation {

    public DeleteSelected(OtrosApplication otrosApplication) {
      super(otrosApplication);
      putValue(NAME, "Delete");
      putValue(SHORT_DESCRIPTION, "Wiil delete selected message colorizer");
      putValue(SMALL_ICON, Icons.DELETE);
    }

    @Override
    public void actionWithConfirmationPerformedHook(ActionEvent e) {
      deleteSelected();
    }

    @Override
    public String getWarnningMessage() {
      return String.format("Do you want to delete message colorizer \"%s\"?", ((MessageColorizer) jList.getSelectedValue()).getName());
    }

  }

  private void enableDisableButtonsForSelectedColorizer() {
    Object selectedValue = jList.getSelectedValue();
    if (selectedValue != null && selectedValue instanceof PropertyPatternMessageColorizer) {
      saveButton.setEnabled(true);
      saveAsButton.setEnabled(true);
      useButton.setEnabled(true);
      deleteButton.setEnabled(true);
    } else {
      saveButton.setEnabled(false);
      saveAsButton.setEnabled(false);
      useButton.setEnabled(false);
      deleteButton.setEnabled(false);
    }
  }

}
