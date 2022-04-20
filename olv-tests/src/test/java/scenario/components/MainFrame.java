package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class MainFrame extends TestComponent<FrameFixture, MainFrame> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

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
    LOGGER.info("Setting search method by string");
    me().comboBox("MainFrame.searchMode").selectItem(".*contains.*");
    return this;
  }

  public MainFrame setSearchModeByRegex() {
    LOGGER.info("Setting search method by regex");
    me().comboBox("MainFrame.searchMode").selectItem(".*Regex.*");
    return this;
  }

  public MainFrame setSearchModeByQuery() {
    LOGGER.info("Setting search method by query");
    me().comboBox("MainFrame.searchMode").selectItem(".*Query.*");
    return this;
  }

  public MainFrame enterSearchText(String query) {
    LOGGER.info("Enter search query: " + query);
    me().textBox("MainFrame.searchField").enterText(query);
    return this;
  }

  public MainFrame setSearchText(String query) {
    LOGGER.info("Set search query: " + query);
    me().textBox("MainFrame.searchField").setText(query);
    return this;
  }

  public MainFrame searchNext() {
    LOGGER.info("Searching next");
    clickButton(me().button("MainFrame.searchNext"));
    return this;
  }

  public MainFrame searchPrevious() {
    LOGGER.info("Searching previous");
    clickButton(me().button("MainFrame.searchPrevious"));
    return this;
  }

  public void nextInfoOrHigher() {
    clickButton(me().button("MainFrame.NextInfo"));
  }

  public void nextWarningOrHigher() {
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
