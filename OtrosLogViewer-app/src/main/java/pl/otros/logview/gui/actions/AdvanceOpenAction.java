package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.gui.AdvanceOpenPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AdvanceOpenAction extends OtrosAction {

  public AdvanceOpenAction(OtrosApplication otrosApplication) {
    super("Open log files", Icons.ARROW_JOIN, otrosApplication);
    this.putValue(Action.LONG_DESCRIPTION, "Open log files from local or remote file systems");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    getOtrosApplication().addClosableTab("Open log files", "Select files you want to open",
        null, new AdvanceOpenPanel(getOtrosApplication()), true);

  }
}
