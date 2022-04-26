package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import scenario.components.filter.FilterPanel;

public class LogViewPanel extends TestComponent<JPanelFixture, LogViewPanel> {

  private final FrameFixture frame;


  public LogViewPanel(FrameFixture frame, Robot robot) {
    super(robot);
    this.frame = frame;
  }

  public JPanelFixture me() {
    return frame.panel("LogViewPanel.panel");
  }

  public LogsTable logsTable() {
    return new LogsTable(me(), robot);
  }

  public FilterPanel filterPanel() {
    return new FilterPanel(me(), robot);
  }

}
