package pl.otros.logview.gui.actions;

import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.config.IdeIntegrationConfigView;
import pl.otros.logview.gui.config.LogDateFormatConfigView;
import pl.otros.logview.gui.config.VersionCheckConfigView;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.swing.config.ConfigComponent;
import pl.otros.swing.config.ConfigView;
import pl.otros.swing.config.provider.ConfigurationProvider;
import pl.otros.swing.config.provider.ConfigurationProviderImpl;
import pl.otros.swing.config.test.DateFormatView;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenConfigurationAction extends OtrosAction {

  public OpenConfigurationAction(OtrosApplication otrosApplication) {
    super("Preferences", Icons.GEAR, otrosApplication);
    putValue(ACCELERATOR_KEY ,KeyStroke.getKeyStroke('p'));
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
    ConfigView[] configViews = new ConfigView[]{
        new LogDateFormatConfigView(),
        new IdeIntegrationConfigView(getOtrosApplication()),
        new VersionCheckConfigView()

    }                                                      ;
    ConfigComponent configComponent = new ConfigComponent(configurationProvider, actionAfterSave,null,configViews);
    getOtrosApplication().addClosableTab("Preferences", "OtrosLogViewerPreferences", Icons.GEAR, configComponent, true);
  }
}
