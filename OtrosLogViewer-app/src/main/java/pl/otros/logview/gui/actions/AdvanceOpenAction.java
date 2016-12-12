package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.gui.AdvanceOpenPanel;

import java.awt.event.ActionEvent;

public class AdvanceOpenAction extends OtrosAction {

  public AdvanceOpenAction(OtrosApplication otrosApplication) {
    super("Open [advance]", otrosApplication);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    getOtrosApplication().addClosableTab("Add", "Adding new", null, new AdvanceOpenPanel(getOtrosApplication()), true);

  }
}
