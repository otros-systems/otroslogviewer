package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JPanelFixture;

public class WelcomeScreen extends TestComponent<JPanelFixture, WelcomeScreen> {

  private final FrameFixture frame;

  public WelcomeScreen(FrameFixture frame, Robot robot) {
    super(robot);
    this.frame = frame;
  }

  public JPanelFixture me(){
    return frame.panel("WelcomeScreen");
  }

  public OpenPanel clickMergeLogs(){
    final JButtonFixture mergeLogs = me().button("Merge log files");
    mergeLogs.click();
    return new OpenPanel(frame,robot).waitFor();
  }

  public ParseClipboardDialog clickParseClipboard(){
    me().button("Parse clipboard").click();
    return new ParseClipboardDialog(robot);
  }

  public StartSocketListenerDialog clickStartSocketListener() {
    me().button("Start socket listener").click();
    return new StartSocketListenerDialog(robot);
  }


}
