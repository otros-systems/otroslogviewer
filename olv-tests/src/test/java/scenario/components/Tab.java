package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tab {
  private static final Logger LOGGER = LoggerFactory.getLogger(Tab.class);
  private final JPanelFixture parent;
  private final Robot robot;

  Tab(JPanelFixture parent, Robot robot) {
    this.parent = parent;
    this.robot = robot;
  }

  public void close() {
    LOGGER.info("Closing tab");
    final JButtonFixture button = parent.button("Tab.close");
    LOGGER.info("Tab name to close is " + parent.label("Tab.name"));
    button.click();
  }
}
