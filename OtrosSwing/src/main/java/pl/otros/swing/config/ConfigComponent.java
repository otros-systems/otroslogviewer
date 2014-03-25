package pl.otros.swing.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.jdesktop.swingx.JXList;
import pl.otros.swing.config.provider.ConfigurationProvider;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class ConfigComponent extends JPanel {
  private JLabel upLabel;
  private JPanel centralPanel;
  private ConfigView[] configViews;
  private JXList list;
  private ConfigurationProvider configurationProvider;
  private Action actionAfterSave;
  private Action actionAfterCancel;

  public ConfigComponent(final ConfigurationProvider configurationProvider, final ConfigView... configViews) {
    this(configurationProvider,null,null,configViews);
  }

  @SuppressWarnings("serial")
  public ConfigComponent(final ConfigurationProvider configurationProvider, Action actionAfterSave, Action actionAfterCancel, final ConfigView... configViews) {
    super();
    this.configurationProvider = configurationProvider;
    this.actionAfterSave = actionAfterSave;
    this.actionAfterCancel = actionAfterCancel;
    this.configViews = configViews;
    this.setLayout(new MigLayout());
    JPanel leftPanel = new JPanel(new BorderLayout());
    list = new JXList(this.configViews);
    list.setCellRenderer(new ConfigViewListRenderer());
    leftPanel.add(list);
    upLabel = new JLabel(" ");
    upLabel.setBounds(10, 10, 10, 10);
    upLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    upLabel
        .setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(upLabel.getForeground()), BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    centralPanel = new JPanel(new BorderLayout());
    list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }
        int firstIndex = list.getSelectedIndex();
        ConfigView configView = ConfigComponent.this.configViews[firstIndex];
        centralPanel.removeAll();
        centralPanel.add(configView.getView());
        upLabel.setText(configView.getName());
        upLabel.setToolTipText(configView.getDescription());
        centralPanel.revalidate();
        centralPanel.repaint();
      }
    });
    for (ConfigView configView : configViews) {
      try {
        Configuration configurationForView = configurationProvider.getConfigurationForView(configView);
        if (configurationForView instanceof FileConfiguration) {
          FileConfiguration fc = (FileConfiguration) configurationForView;
          try {
            fc.load();
          } catch (ConfigurationException e) {
            // TODO
          }
        }
        configView.loadConfiguration(configurationForView);
        centralPanel.add(configView.getView(), BorderLayout.CENTER);
        configView.getView().getComponents();
      } catch (ConfigurationException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    if (list.getModel().getSize() > 0) {
      list.setSelectedIndex(0);
    }
    JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveConfig();
      }
    });
    southPanel.add(saveButton);
    JButton reloadButton = new JButton();
    reloadButton.setAction(new AbstractAction("Reload") {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (ConfigView configView : configViews) {
          Configuration configurationForView;
          try {
            configurationForView = configurationProvider.getConfigurationForView(configView);
            if (configurationForView instanceof FileConfiguration) {
              FileConfiguration fc = (FileConfiguration) configurationForView;
              fc.reload();
              configView.loadConfiguration(fc);
            }
          } catch (ConfigurationException e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    southPanel.add(reloadButton);
    this.add(upLabel, "dock north");
    this.add(leftPanel, "dock west");
    this.add(new JScrollPane(centralPanel), "dock center, grow, push");
    this.add(southPanel, "dock south");
    list.requestFocus();
  }

  public void saveConfig() {
    List<ValidationResult> validationErrors = new LinkedList<ValidationResult>();
    StringBuilder errorsString = new StringBuilder("Following validation errors occurs:\n");
    for (ConfigView configView : configViews) {
      ValidationResult validate = configView.validate();
      if (!validate.isValidationIsCorrect()) {
        validationErrors.add(validate);
        errorsString.append(configView.getName()).append("\n");
        for (String string : validate.getErrorMessages()) {
          errorsString.append("\t - ").append(string).append("\n");
        }
        errorsString.append("\n");
      }
    }
    if (validationErrors.size() > 0) {
      JTextPane texPane = new JTextPane();
      texPane.setText(errorsString.toString());
      JOptionPane.showMessageDialog(this, texPane, "Validation Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    for (ConfigView configView : configViews) {
      Configuration configurationForView;
      try {
        configurationForView = configurationProvider.getConfigurationForView(configView);
        configView.saveConfiguration(configurationForView);
        if (configurationForView instanceof FileConfiguration) {
          FileConfiguration fc = (FileConfiguration) configurationForView;
          fc.save();
        }
      } catch (ConfigurationException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
    if (actionAfterSave != null) {
      actionAfterSave.actionPerformed(null);
    }
  }
}