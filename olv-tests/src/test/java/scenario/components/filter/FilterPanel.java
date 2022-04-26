package scenario.components.filter;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.JPanelFixture;
import scenario.components.LogViewPanel;
import scenario.components.TestComponent;

public class FilterPanel extends TestComponent<JPanelFixture, LogViewPanel> {
  private final JPanelFixture frame;

  public FilterPanel(JPanelFixture frame, Robot robot) {
    super(robot);
    this.frame = frame;
  }

  public LevelFilterPanel levelFilterPanel() {
    return new LevelFilterPanel(me(), robot);
  }

  @Override
  public JPanelFixture me() {
    return frame.panel(pl.otros.logview.gui.LogViewPanel.NAME_SEARCH_FILTER_PANEL);
  }
}
