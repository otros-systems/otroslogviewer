package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;

import javax.swing.*;

import static org.assertj.swing.finder.WindowFinder.findFrame;

public class MainFrame extends TestComponent<FrameFixture, MainFrame>{

  public MainFrame(Robot robot) {
    super(robot);
  }

  @Override
  public FrameFixture me() {
    return findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      protected boolean isMatching(JFrame frame1) {
        return frame1.getTitle().startsWith("OtrosLogViewer") && frame1.isShowing();
      }
    }).using(robot);
  }

  public WelcomeScreen welcomeScreen() {
    return new WelcomeScreen(me(), robot);
  }

  public TabBar tabBar() {
    return new TabBar(me(), robot);
  }

}
