package pl.otros.logview.gui.actions;

import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;

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
  }

  private ImageIcon getIcon(boolean enabled) {
    return enabled ? Icons.ARROW_STEP_OVER : new ImageIcon(GrayFilter.createDisabledImage(Icons.ARROW_STEP_OVER.getImage()));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final boolean newValue = !configuration.getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true);
    configuration.setProperty(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, newValue);
    putValue(SMALL_ICON, getIcon(newValue));
  }
}
