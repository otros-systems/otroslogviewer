package scenario.components;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.assertj.swing.fixture.JFileChooserFixture;
import pl.otros.logview.gui.markers.editor.MarkerEditor;

import javax.swing.*;

public class NewMarkerDialog extends TestComponent<FrameFixture, NewMarkerDialog> {

  public NewMarkerDialog(Robot robot) {
    super(robot);
  }

  @Override
  public FrameFixture me() {
    return WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
      @Override
      protected boolean isMatching(JFrame component) {
        final String title = component.getTitle();
        return "Create new marker".equals(title) && component.isShowing();
      }
    }).using(robot);
  }

  public NewMarkerDialog setName(String name) {
    me().textBox(MarkerEditor.NAME).setText(name);
    return this;
  }

  public NewMarkerDialog setDescription(String description) {
    me().textBox(MarkerEditor.DESCRIPTION).setText(description);
    return this;
  }

  public NewMarkerDialog setGroups(String groups) {
    me().textBox(MarkerEditor.GROUPS).setText(groups);
    return this;
  }

  public NewMarkerDialog setType(String type) {
    me().comboBox(MarkerEditor.TYPE).selectItem(type);
    return this;
  }

  public NewMarkerDialog setIgnoreCase(boolean ignoreCase) {
    JCheckBoxFixture checkBox = me().checkBox(MarkerEditor.IGNORE_CASE);
    if (checkBox.target().isSelected() != ignoreCase) {
      checkBox.click();
    }
    return this;
  }

  public NewMarkerDialog setStringMatcherCondition(String condition) {
    me().textBox(MarkerEditor.STRING_MATCHER_CONDITION).setText(condition);
    return this;
  }

  public NewMarkerDialog setRegexMatcherCondition(String condition) {
    me().textBox(MarkerEditor.REGEX_MATCHER_CONDITION).setText(condition);
    return this;
  }

  public void save() {
    me().button(MarkerEditor.SAVE).click();
  }

  public JFileChooserFixture fileChooser() {
    return me().fileChooser(MarkerEditor.FILE_CHOOSER);
  }
}
