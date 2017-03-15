package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.gui.config.Appearance;
import pl.otros.logview.gui.config.GeneralConfigView;
import pl.otros.logview.gui.config.IdeIntegrationConfigView;
import pl.otros.logview.gui.config.LogTableFormatConfigView;
import pl.otros.logview.gui.config.SessionsConfig;
import pl.otros.logview.gui.config.VersionCheckConfigView;
import pl.otros.swing.config.ConfigComponent;
import pl.otros.swing.config.ConfigView;
import pl.otros.swing.config.provider.ConfigurationProvider;
import pl.otros.swing.config.provider.ConfigurationProviderImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

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
    ConfigView[] configViews = {
        new GeneralConfigView(),
        new Appearance(getOtrosApplication()),
        new LogTableFormatConfigView(getOtrosApplication()),
        new IdeIntegrationConfigView(getOtrosApplication()),
        new SessionsConfig(getOtrosApplication()),
        new VersionCheckConfigView()
    };
    Action actionAfterSave = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getOtrosApplication().getStatusObserver().updateStatus("Configuration saved");
        Arrays.stream(configViews).forEach(ConfigView::apply);
      }
    };
    if (configComponent == null) {
      configComponent = new ConfigComponent(configurationProvider, actionAfterSave, configViews);
    } else {
      configComponent.reload();
    }
    getOtrosApplication().addClosableTab("Preferences", "OtrosLogViewerPreferences", Icons.GEAR, configComponent, true);
  }
}
