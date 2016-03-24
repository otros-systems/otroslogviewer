package pl.otros.logview.gui.actions;

import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.api.OtrosAction;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.Icons;
import pl.otros.logview.api.OtrosApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SwitchAutoJump extends OtrosAction {

  private final DataConfiguration configuration;

  public SwitchAutoJump(OtrosApplication otrosApplication) {
    super(otrosApplication);
    configuration = getOtrosApplication().getConfiguration();
    final boolean enabled = configuration.getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true);
    Icon icon = getIcon(enabled);
    putValue(SMALL_ICON, icon);
    putValue(SHORT_DESCRIPTION, "Turn on/off automatic jump to code during browsing logs.");
    configuration.addConfigurationListener(event -> {
      if (event.getPropertyName() != null && event.getPropertyName().equals(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED)) {
        putValue(SMALL_ICON, getIcon(configuration.getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true)));
      }
    });
  }

  private ImageIcon getIcon(boolean enabled) {
    return enabled ? Icons.ARROW_STEP_OVER : Icons.ARROW_STEP_OVER_GRAY;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final boolean newValue = !configuration.getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true);
    configuration.setProperty(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, newValue);
    putValue(SMALL_ICON, getIcon(newValue));
  }
}
