package pl.otros.logview.gui.actions;

import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.config.GeneralConfigView;
import pl.otros.logview.gui.config.IdeIntegrationConfigView;
import pl.otros.logview.gui.config.LogTableFormatConfigView;
import pl.otros.logview.gui.config.VersionCheckConfigView;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.swing.config.ConfigComponent;
import pl.otros.swing.config.ConfigView;
import pl.otros.swing.config.provider.ConfigurationProvider;
import pl.otros.swing.config.provider.ConfigurationProviderImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class OpenPreferencesAction extends OtrosAction {
  private ConfigComponent configComponent;

  public OpenPreferencesAction(OtrosApplication otrosApplication) {
    super("Preferences", Icons.GEAR, otrosApplication);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_MASK));
    putValue(MNEMONIC_KEY, KeyEvent.VK_P);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ConfigurationProvider configurationProvider = new ConfigurationProviderImpl(getOtrosApplication().getConfiguration(), AllPluginables.USER_CONFIGURATION_DIRECTORY);
    Action actionAfterSave = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getOtrosApplication().getStatusObserver().updateStatus("Configuration saved");
      }
    };
    ConfigView[] configViews = {
        new GeneralConfigView(),
        new LogTableFormatConfigView(getOtrosApplication()),
        new IdeIntegrationConfigView(getOtrosApplication()),
        new VersionCheckConfigView()
    };
    if (configComponent == null) {
      configComponent = new ConfigComponent(configurationProvider, actionAfterSave, null, configViews);
    } else {
      configComponent.reload();
    }
    getOtrosApplication().addClosableTab("Preferences", "OtrosLogViewerPreferences", Icons.GEAR, configComponent, true);
  }
}
