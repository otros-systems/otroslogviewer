package pl.otros.logview.api.plugins;

import pl.otros.logview.gui.LogViewPanel;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.OtrosAction;

import java.util.List;

/**
 * Returns actions for logs table context menu
 */
public interface MenuActionProvider {

  /**
   * Gets action for log view panel context menu
   * @param otrosApplication OtrosApplication
   * @param logViewPanel LogViewPanel
   * @return list of actions
   */
  List<OtrosAction> getActions(OtrosApplication otrosApplication,LogViewPanel logViewPanel);

}
