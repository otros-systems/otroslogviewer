package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;

import javax.swing.*;

public class StartSocketListenerDialog extends TestComponent<DialogFixture, StartSocketListenerDialog> {

  public StartSocketListenerDialog(Robot robot) {
    super(robot);
  }

  @Override
  public DialogFixture me() {
    return WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
      @Override
      protected boolean isMatching(JDialog component) {
        return component.getTitle().equals("Choose log importer and port");
      }
    }).using(robot);
  }

  public StartSocketListenerDialog setPort(int port) {
    me().spinner("StartSocketListenerDialog.port").select(Integer.valueOf(port));
    return this;
  }

  public StartSocketListenerDialog setLogParser(String logParser) {
    me().comboBox("StartSocketListenerDialog.importer").selectItem(logParser);
    return this;
  }


  public LogViewPanel startListening() {
    me().button(matcherForButtonWithText("OK")).click();
    return new LogViewPanel(new MainFrame(robot).me(),robot);
  }

  public JButtonFixture cancel() {
    return me().button(matcherForButtonWithText("Cancel"));
  }


}
