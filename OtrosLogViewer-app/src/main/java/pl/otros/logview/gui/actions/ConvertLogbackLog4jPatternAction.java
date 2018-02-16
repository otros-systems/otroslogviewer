package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.gui.ConvertLogFormatPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Function;

public class ConvertLogbackLog4jPatternAction extends OtrosAction {

  public static final String NAME = "Convert logback/log4j configuration";

  public ConvertLogbackLog4jPatternAction(OtrosApplication otrosApplication) {
    super("Convert logback/log4j configuration", otrosApplication);
    putValue(Action.SMALL_ICON, Icons.DOCUMENT_CODE);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Function<JComponent, Void> closeFunction = component -> {
      getOtrosApplication().closeTab(component);
      return null;
    };

    final ConvertLogFormatPanel component = new ConvertLogFormatPanel(getOtrosApplication(), closeFunction);
    getOtrosApplication().addClosableTab(NAME, "", Icons.DOCUMENT_CODE, component, true);
  }
}
