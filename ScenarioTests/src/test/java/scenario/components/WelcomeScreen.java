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

  public OpenPanel clickOpenLogs(){
    final JButtonFixture openLogs = me().button("open log files");
    openLogs.click();
    return new OpenPanel(frame,robot).waitFor();
  }

}
