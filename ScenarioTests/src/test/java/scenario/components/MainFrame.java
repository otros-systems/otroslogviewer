package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import javax.swing.*;

public class MainFrame extends TestComponent<FrameFixture, MainFrame> {

  public MainFrame(Robot robot) {
    super(robot);
  }

  @Override
  public FrameFixture me() {
    return WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
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

  public MainFrame setSearchModeByString() {
    me().comboBox("MainFrame.searchMode").selectItem(".*contains.*");
    return this;
  }

  public MainFrame setSearchModeByRegex() {
    me().comboBox("MainFrame.searchMode").selectItem(".*Regex.*");
    return this;
  }

  public MainFrame setSearchModeByQuery() {
    me().comboBox("MainFrame.searchMode").selectItem(".*Query.*");
    return this;
  }

  public MainFrame enterSearchText(String query) {
    me().textBox("MainFrame.searchField").enterText(query);
    return this;
  }

  public MainFrame searchNext() {
    clickButton(me().button("MainFrame.searchNext"));
    return this;
  }

  public MainFrame searchPrevious() {
    clickButton(me().button("MainFrame.searchPrevious"));
    return this;
  }

  public void nextInfoOrHigher() {
    clickButton(me().button("MainFrame.NextInfo"));
  }

  public void nextWargingOrHigher() {
    clickButton(me().button("MainFrame.NextWarning"));
  }

  public void nextSevere() {
    clickButton(me().button("MainFrame.NextSevere"));
  }
  public void previousInfoOrHigher() {
    clickButton(me().button("MainFrame.PreviousInfo"));
  }

  public void previousWargingOrHigher() {
    clickButton(me().button("MainFrame.PreviousWarning"));
  }

  public void previousSevere() {
    clickButton(me().button("MainFrame.PreviousSevere"));
  }

}
