package scenario.components;

import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;

import static org.awaitility.Awaitility.await;

public class ParseClipboardDialog extends TestComponent<DialogFixture, ParseClipboardDialog> {

  public ParseClipboardDialog(Robot robot) {
    super(robot);
  }

  @Override
  public DialogFixture me() {
    return WindowFinder.findDialog("Import logs from clipboard").using(robot);
  }

  public JTextComponentFixture processedContent() {
    return me().textBox("importClipboard.processedContent");
  }

  public JTextComponentFixture processingPattern() {
    return me().textBox("importClipboard.processingPattern");
  }

  public JTextComponentFixture clipboardTextAreaContent() {
    return me().textBox("importClipboard.content");
  }

  public JButtonFixture refresh() {
    return me().button("importClipboard.refresh");
  }

  public LogViewPanel importLogs() {
    me().button(matcherForButtonWithText("Import")).click();
    return new LogViewPanel(new MainFrame(robot).me(),robot);
  }

  public JButtonFixture cancel() {
    return me().button(matcherForButtonWithText("Cancel"));
  }

  public void waitForProcessedContent(String content) {
    await().until(() -> processedContent().text().equals(content));
  }
}
