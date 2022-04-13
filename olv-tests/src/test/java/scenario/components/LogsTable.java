package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTableFixture;

import java.util.Arrays;

import static org.awaitility.Awaitility.await;

public class LogsTable extends TestComponent<JTableFixture, LogsTable> {

  private final JPanelFixture logViewPanel;

  public LogsTable(JPanelFixture logViewPanel, Robot robot) {
    super(robot);
    this.logViewPanel = logViewPanel;
  }

  /**
   * Returns count of visible logs
   *
   * @return count of visible logs or -1 if can't read value
   */
  public int visibleLogsCount() {
    try {

      return me().rowCount();
    } catch (Exception e) {
      return -1;
    }
  }

  public LogsTable waitForSelectedRow(int number){
    await()
      .ignoreExceptions()
      .until(() -> me().requireSelectedRows(number));
    return this;
  }

  public LogsTable hasValueInRow(int row, String content){
    await()
      .ignoreExceptions()
      .until(() -> Arrays.stream(me().contents()[row]).anyMatch(c -> c.equals(content)));
    return this;
  }

  @Override
  public JTableFixture me() {
    return logViewPanel.table("LogViewPanel.log table");
  }
}
