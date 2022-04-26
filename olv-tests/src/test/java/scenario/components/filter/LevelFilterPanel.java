package scenario.components.filter;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JPanelFixture;
import pl.otros.logview.filter.LevelFilter;
import scenario.components.LogViewPanel;
import scenario.components.TestComponent;

import java.util.logging.Level;

public class LevelFilterPanel extends TestComponent<JPanelFixture, LogViewPanel> {
  private final JPanelFixture frame;

  public LevelFilterPanel(JPanelFixture frame, Robot robot) {
    super(robot);
    this.frame = frame;
  }

  public JCheckBoxFixture checkBox() {
    return me().checkBox("Level filter");
  }

  public JComboBoxFixture levelComboBox() {
    return me().comboBox(LevelFilter.NAME_LEVEL_FILTER_LEVEL_COMBO);
  }

  public void selectLevel(Level level) {
    int index = -1;
    if (Level.FINEST.equals(level)) {
      index = 0;
    } else if (Level.FINER.equals(level)) {
      index = 1;
    } else if (Level.FINE.equals(level)) {
      index = 2;
    } else if (Level.CONFIG.equals(level)) {
      index = 3;
    } else if (Level.INFO.equals(level)) {
      index = 4;
    } else if (Level.WARNING.equals(level)) {
      index = 5;
    } else if (Level.SEVERE.equals(level)) {
      index = 6;
    }
    levelComboBox().selectItem(index);
  }

  @Override
  public JPanelFixture me() {
    return frame.panel("Level filter");
  }
}
