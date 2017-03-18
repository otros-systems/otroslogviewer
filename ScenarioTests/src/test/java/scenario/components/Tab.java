package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.JPanelFixture;

public class Tab {
  private final JPanelFixture parent;
  private final Robot robot;

  public Tab(JPanelFixture parent, Robot robot) {
    this.parent = parent;
    this.robot = robot;
  }

  public void close() {
    parent.button("Tab.close").click();
  }
}
