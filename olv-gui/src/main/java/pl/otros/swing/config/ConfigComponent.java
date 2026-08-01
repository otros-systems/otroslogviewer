package pl.otros.swing.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jdesktop.swingx.JXList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.swing.config.provider.ConfigurationProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ConfigComponent extends JPanel {
  private final static Logger LOGGER = LoggerFactory.getLogger(ConfigComponent.class.getName());
  private final ConfigView[] configViews;
  private final ConfigurationProvider configurationProvider;
  private final Optional<Action> actionAfterSave;

  public ConfigComponent(final ConfigurationProvider configurationProvider, Action actionAfterSave, final ConfigView... configViews) {
    super();
    this.configurationProvider = configurationProvider;
    this.actionAfterSave = Optional.ofNullable(actionAfterSave);
    this.configViews = configViews;
    this.setLayout(new MigLayout("insets 0, gapy 2"));
    JPanel leftPanel = new JPanel(new MigLayout("insets 5, gapy 2"));
    JXList<ConfigView> list = new JXList<>(this.configViews);
    list.setCellRenderer(new ConfigViewListRenderer());
    leftPanel.add(list);
    JLabel upLabel = new JLabel(" ");
    upLabel.setBounds(10, 10, 10, 10);
    upLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    upLabel
      .setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(upLabel.getForeground()), BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    JPanel centralPanel = new JPanel(new BorderLayout());
    list.getSelectionModel().addListSelectionListener(e -> {
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
    });

    if (list.getModel().getSize() > 0) {
      list.setSelectedIndex(0);
    }
    final JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveConfig());
    southPanel.add(saveButton);
    final JButton reloadButton = new JButton();
    reloadButton.setAction(new AbstractAction("Reload") {
      @Override
      public void actionPerformed(ActionEvent e) {
        reload();
      }
    });
    southPanel.add(reloadButton);

    this.add(upLabel, "dock north");
    this.add(leftPanel, "dock west");
    this.add(new JScrollPane(centralPanel), "dock center, grow, push");
    this.add(southPanel, "dock south");
    list.requestFocus();
    reload();
  }

  public void reload() {
    for (ConfigView configView : configViews) {
      Configuration configurationForView;

      try {
        configurationForView = configurationProvider.getConfigurationForView(configView);
        configurationProvider.reload(configView);
        configView.loadConfiguration(configurationForView);
      } catch (ConfigurationException e1) {
        LOGGER.error("Can't load configuration for {} on reload", configView, e1);
      }
    }
  }

  public void saveConfig() {
    List<ValidationResult> validationErrors = new LinkedList<>();
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
        configurationProvider.save(configView, configurationForView);
      } catch (ConfigurationException e1) {
        LOGGER.error("Can't load configuration for {}", configView, e1);
      }
    }
    actionAfterSave.ifPresent(a -> a.actionPerformed(null));
  }
}