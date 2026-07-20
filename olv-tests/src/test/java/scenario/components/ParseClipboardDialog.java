package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;

public class ParseClipboardDialog extends TestComponent<DialogFixture, ParseClipboardDialog> {

  public ParseClipboardDialog(Robot robot) {
    super(robot);
  }

  @Override
  public DialogFixture me() {
    return WindowFinder.findDialog("Import logs from clipboard").using(robot);
  }

  public JTextComponentFixture clipboardTextAreaContent() {
    return me().textBox("importClipboard.content");
  }

  public boolean isImportEnabled() {
    return me().button(matcherForButtonWithText("Import")).target().isEnabled();
  }

  public LogViewPanel importLogs() {
    me().button(matcherForButtonWithText("Import")).click();
    return new LogViewPanel(new MainFrame(robot).me(),robot);
  }

  public void pasteClipboard() {
    me().button("importClipboard.refresh").click();
  }

  public JButtonFixture cancel() {
    return me().button(matcherForButtonWithText("Cancel"));
  }

}
