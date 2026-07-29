package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JPanelFixture;
import pl.otros.logview.gui.markers.editor.MarkersEditor;

public class MarkerListDialog extends TestComponent<JPanelFixture, MarkerListDialog> {

  private final FrameFixture frame;

  public MarkerListDialog(FrameFixture frame, Robot robot) {
    super(robot);
    this.frame = frame;
  }

  @Override
  public JPanelFixture me() {
    return frame.panel(MarkersEditor.class.getSimpleName());
  }

  public JButtonFixture newMarkerButton() {
    return me().button(MarkersEditor.NAME_MARKER_NEW);
  }

}
