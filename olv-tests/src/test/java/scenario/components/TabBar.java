package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;

public class TabBar extends TestComponent<JTabbedPaneFixture,TabBar>{


  private final FrameFixture frame;

  public TabBar(FrameFixture frame, Robot robot) {
    super(robot);
    this.frame = frame;
  }

  public JTabbedPaneFixture me() {
    return frame.tabbedPane("MainFrame.tabbedPane");
  }

  //TODO select specifc tab to close
  public Tab tab() {
    return new Tab(frame.panel("Tab.header"), robot);
  }


  public void chooseTab(int index) {

    me().selectTab(index);
  }


}
