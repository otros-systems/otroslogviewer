package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.gui.open.AdvanceOpenPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AdvanceOpenAction extends OtrosAction {

  public AdvanceOpenAction(OtrosApplication otrosApplication) {
    super("Merge log files", Icons.ARROW_JOIN, otrosApplication);
    this.putValue(Action.LONG_DESCRIPTION, "Merge log files from local or remote file systems");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    getOtrosApplication().addClosableTab("Merge log files", "Select files you want to merge",
        null, new AdvanceOpenPanel(getOtrosApplication()), true);

  }
}
